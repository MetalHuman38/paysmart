import { describe, expect, it, vi } from "vitest";
import type { Messaging } from "firebase-admin/messaging";
import type {
  FirestoreNotificationRepository,
  NotificationPushInstallation,
} from "../../infrastructure/firestore/FirestoreNotificationRepository.js";
import { NotificationDeliveryService } from "../../services/notificationDeliveryService.js";

describe("NotificationDeliveryService", () => {
  it("writes the inbox item, sends push, and removes invalid tokens", async () => {
    const createInboxNotification = vi.fn().mockResolvedValue("notification-1");
    const listPushInstallations = vi.fn<() => Promise<NotificationPushInstallation[]>>()
      .mockResolvedValue([
        {
          installationId: "install-1",
          fcmToken: "token-1",
          platform: "android",
        },
        {
          installationId: "install-2",
          fcmToken: "token-2",
          platform: "android",
        },
      ]);
    const deleteInstallations = vi.fn().mockResolvedValue(undefined);

    const notifications = {
      createInboxNotification,
      listPushInstallations,
      deleteInstallations,
    } as unknown as FirestoreNotificationRepository;

    const sendEachForMulticast = vi.fn().mockResolvedValue({
      successCount: 1,
      failureCount: 1,
      responses: [
        { success: true },
        {
          success: false,
          error: { code: "messaging/registration-token-not-registered" },
        },
      ],
    });
    const messaging = {
      sendEachForMulticast,
    } as unknown as Messaging;

    const service = new NotificationDeliveryService(notifications, messaging);
    const result = await service.deliverToUser("uid-1", {
      type: "wallet_top_up_succeeded",
      channel: "account_updates",
      title: "Money added to your wallet",
      body: "Your top up is now available.",
    });

    expect(createInboxNotification).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        type: "wallet_top_up_succeeded",
      })
    );
    expect(sendEachForMulticast).toHaveBeenCalledWith(
      expect.objectContaining({
        tokens: ["token-1", "token-2"],
        data: expect.objectContaining({
          notificationId: "notification-1",
          channel: "account_updates",
        }),
      })
    );
    expect(deleteInstallations).toHaveBeenCalledWith("uid-1", ["install-2"]);
    expect(result).toEqual({
      notificationId: "notification-1",
      pushEligibleInstallations: 2,
      pushDelivered: 1,
      pushFailed: 1,
    });
  });

  it("deduplicates repeated tokens before sending", async () => {
    const notifications = {
      createInboxNotification: vi.fn().mockResolvedValue("notification-2"),
      listPushInstallations: vi.fn().mockResolvedValue([
        {
          installationId: "install-1",
          fcmToken: "token-1",
          platform: "android",
        },
        {
          installationId: "install-2",
          fcmToken: "token-1",
          platform: "android",
        },
      ]),
      deleteInstallations: vi.fn().mockResolvedValue(undefined),
    } as unknown as FirestoreNotificationRepository;

    const sendEachForMulticast = vi.fn().mockResolvedValue({
      successCount: 1,
      failureCount: 0,
      responses: [{ success: true }],
    });
    const messaging = {
      sendEachForMulticast,
    } as unknown as Messaging;

    const service = new NotificationDeliveryService(notifications, messaging);
    await service.deliverToUser("uid-2", {
      type: "identity_verified",
      channel: "account_updates",
      title: "Identity verified",
      body: "Your account is ready.",
    });

    expect(sendEachForMulticast).toHaveBeenCalledWith(
      expect.objectContaining({
        tokens: ["token-1"],
      })
    );
  });
});
