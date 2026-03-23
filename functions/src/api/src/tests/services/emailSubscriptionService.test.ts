import { describe, expect, it } from "vitest";
import { EmailSubscriptionService } from "../../services/emailSubscriptionService.js";

describe("EmailSubscriptionService", () => {
  it("creates and verifies a product update unsubscribe token", () => {
    const service = new EmailSubscriptionService(
      "test-secret",
      "https://pay-smart.net/email/unsubscribe"
    );

    const url = service.buildUnsubscribeUrl({
      uid: "uid-1",
      topic: "product_updates",
      email: "user@example.com",
      campaignId: "campaign-1",
    });

    const token = new URL(url).searchParams.get("token");
    expect(token).toBeTruthy();
    expect(service.verifyToken(token || "")).toEqual({
      uid: "uid-1",
      topic: "product_updates",
      email: "user@example.com",
      campaignId: "campaign-1",
    });
  });

  it("rejects invalid signatures", () => {
    const service = new EmailSubscriptionService(
      "test-secret",
      "https://pay-smart.net/email/unsubscribe"
    );

    expect(() => service.verifyToken("invalid.token")).toThrow(
      "Invalid unsubscribe token"
    );
  });
});
