import { beforeEach, describe, expect, it, vi } from "vitest";
import { ProductUpdateCampaignService } from "../../services/productUpdateCampaignService.js";

describe("ProductUpdateCampaignService", () => {
  const campaignRepository = {
    createCampaign: vi.fn(),
    dispatchCampaign: vi.fn(),
    claimReadyCampaign: vi.fn(),
    getDeliverySnapshot: vi.fn(),
    markNotificationDelivered: vi.fn(),
    markEmailDelivered: vi.fn(),
    markEmailFailed: vi.fn(),
    markSent: vi.fn(),
    markFailed: vi.fn(),
  };
  const preferencesRepository = {
    listPushProductUpdateRecipients: vi.fn(),
    listEmailProductUpdateRecipients: vi.fn(),
  };
  const notificationDelivery = {
    deliverToUser: vi.fn(),
  };
  const mailer = {
    send: vi.fn(),
    sendVerificationEmail: vi.fn(),
  };
  const subscriptionService = {
    buildUnsubscribeUrl: vi.fn(),
  };
  const userRepository = {
    getById: vi.fn(),
  };
  const auth = {
    getUser: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    campaignRepository.createCampaign.mockResolvedValue("campaign-1");
    campaignRepository.dispatchCampaign.mockResolvedValue(undefined);
    campaignRepository.getDeliverySnapshot.mockResolvedValue({});
    campaignRepository.markNotificationDelivered.mockResolvedValue(undefined);
    campaignRepository.markEmailDelivered.mockResolvedValue(undefined);
    campaignRepository.markEmailFailed.mockResolvedValue(undefined);
    campaignRepository.markSent.mockResolvedValue(undefined);
    campaignRepository.markFailed.mockResolvedValue(undefined);
    preferencesRepository.listPushProductUpdateRecipients.mockResolvedValue([]);
    preferencesRepository.listEmailProductUpdateRecipients.mockResolvedValue([]);
    notificationDelivery.deliverToUser.mockResolvedValue({
      notificationId: "product_update_campaign-1",
      pushEligibleInstallations: 1,
      pushDelivered: 1,
      pushFailed: 0,
    });
    subscriptionService.buildUnsubscribeUrl.mockReturnValue(
      "https://pay-smart.net/email/unsubscribe?token=abc"
    );
    userRepository.getById.mockResolvedValue({
      uid: "uid-1",
      email: "user@example.com",
      displayName: "Ada Tester",
    });
    auth.getUser.mockResolvedValue({
      email: "auth@example.com",
      displayName: "Auth User",
    });
  });

  it("creates a draft campaign", async () => {
    const service = createService();

    const campaignId = await service.createCampaign({
      title: "PaySmart FX refresh",
      summary: "New FX screens are now live.",
    });

    expect(campaignRepository.createCampaign).toHaveBeenCalledWith(
      expect.objectContaining({
        title: "PaySmart FX refresh",
        summary: "New FX screens are now live.",
      })
    );
    expect(campaignId).toBe("campaign-1");
  });

  it("processes push and email deliveries for a ready campaign", async () => {
    campaignRepository.claimReadyCampaign.mockResolvedValue({
      campaignId: "campaign-1",
      status: "processing",
      title: "PaySmart FX refresh",
      subject: "PaySmart FX refresh",
      summary: "New FX screens are now live.",
      body: "We cleaned up the experience and tightened rate visibility.",
      area: "FX",
      releaseStatus: "Shipped",
      highlights: ["Larger rate cards", "Improved flag rendering"],
      ctaLabel: "Read the update",
      ctaUrl: "https://pay-smart.net/updates/",
      sendPush: true,
      sendEmail: true,
    });
    preferencesRepository.listPushProductUpdateRecipients.mockResolvedValue([
      { uid: "uid-1", preferredLocale: "en-GB" },
      { uid: "uid-1", preferredLocale: "en-GB" },
    ]);
    preferencesRepository.listEmailProductUpdateRecipients.mockResolvedValue([
      { uid: "uid-1", preferredLocale: "en-GB" },
      { uid: "uid-2", preferredLocale: "en-GB" },
    ]);
    userRepository.getById
      .mockResolvedValueOnce({
        uid: "uid-1",
        email: "user@example.com",
        displayName: "Ada Tester",
      })
      .mockResolvedValueOnce({
        uid: "uid-2",
      });

    const service = createService();
    await service.processReadyCampaign("campaign-1");

    expect(notificationDelivery.deliverToUser).toHaveBeenCalledTimes(1);
    expect(notificationDelivery.deliverToUser).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        type: "product_update_campaign",
        channel: "product_updates",
      }),
      { sendPush: true }
    );
    expect(mailer.send).toHaveBeenCalledTimes(2);
    expect(mailer.send).toHaveBeenCalledWith(
      expect.objectContaining({
        kind: "product_update",
        to: "user@example.com",
        recipientName: "Ada Tester",
      })
    );
    expect(mailer.send).toHaveBeenCalledWith(
      expect.objectContaining({
        kind: "product_update",
        to: "auth@example.com",
      })
    );
    expect(campaignRepository.markSent).toHaveBeenCalledWith(
      "campaign-1",
      expect.objectContaining({
        inboxRecipients: 1,
        emailRecipients: 2,
        emailDelivered: 2,
        pushDelivered: 1,
      })
    );
  });

  it("records failures when an email recipient has no email", async () => {
    campaignRepository.claimReadyCampaign.mockResolvedValue({
      campaignId: "campaign-2",
      status: "processing",
      title: "Identity improvements",
      subject: "Identity improvements",
      summary: "Identity review has been upgraded.",
      highlights: [],
      sendPush: false,
      sendEmail: true,
    });
    preferencesRepository.listEmailProductUpdateRecipients.mockResolvedValue([
      { uid: "uid-3", preferredLocale: null },
    ]);
    userRepository.getById.mockResolvedValue({ uid: "uid-3" });
    auth.getUser.mockRejectedValue(new Error("missing user"));

    const service = createService();
    await service.processReadyCampaign("campaign-2");

    expect(campaignRepository.markEmailFailed).toHaveBeenCalledWith(
      "campaign-2",
      "uid-3",
      "Missing recipient email"
    );
    expect(campaignRepository.markSent).toHaveBeenCalledWith(
      "campaign-2",
      expect.objectContaining({
        emailFailed: 1,
        emailDelivered: 0,
      })
    );
  });

  function createService() {
    return new ProductUpdateCampaignService(
      campaignRepository as any,
      preferencesRepository as any,
      notificationDelivery as any,
      mailer as any,
      subscriptionService as any,
      userRepository as any,
      auth as any,
      "https://pay-smart.net/updates/"
    );
  }
});
