import { describe, expect, it } from "vitest";
import {
  buildIdentityReviewNotificationRequest,
  buildWalletTransactionNotificationRequest,
  collectSecurityNotificationRequests,
} from "../../workers/processTransactionalNotifications.js";

describe("buildIdentityReviewNotificationRequest", () => {
  it("creates a verified identity notification when status changes", () => {
    const notification = buildIdentityReviewNotificationRequest(
      { status: "review_processing" },
      { status: "verified" },
      "session-1"
    );

    expect(notification).toEqual(
      expect.objectContaining({
        notificationId: "identity_review_verified_session-1",
        type: "identity_verified",
        title: "Identity verified",
      })
    );
  });

  it("creates a rejected identity notification with a reason-specific body", () => {
    const notification = buildIdentityReviewNotificationRequest(
      { status: "review_processing" },
      { status: "rejected", reviewDecisionReason: "name_mismatch" },
      "session-2"
    );

    expect(notification).toEqual(
      expect.objectContaining({
        notificationId: "identity_review_rejected_session-2",
        type: "identity_review_rejected",
      })
    );
    expect(notification?.body).toContain("match your ID details");
  });

  it("does not create a notification when the status does not change", () => {
    const notification = buildIdentityReviewNotificationRequest(
      { status: "verified" },
      { status: "verified" },
      "session-3"
    );

    expect(notification).toBeNull();
  });
});

describe("buildWalletTransactionNotificationRequest", () => {
  it("creates a wallet top-up notification for settled funds", () => {
    const notification = buildWalletTransactionNotificationRequest(
      {
        type: "top_up",
        status: "succeeded",
        provider: "stripe",
        amountMinor: 2599,
        currency: "GBP",
      },
      "tx-1"
    );

    expect(notification).toEqual(
      expect.objectContaining({
        notificationId: "wallet_top_up_succeeded_tx-1",
        type: "wallet_top_up_succeeded",
        title: "Money added to your wallet",
      })
    );
    expect(notification?.body).toContain("25.99");
  });

  it("ignores non top-up wallet transactions", () => {
    const notification = buildWalletTransactionNotificationRequest(
      {
        type: "transfer",
        status: "succeeded",
        amountMinor: 2599,
        currency: "GBP",
      },
      "tx-2"
    );

    expect(notification).toBeNull();
  });
});

describe("collectSecurityNotificationRequests", () => {
  it("detects new security controls and passkey removal", () => {
    const notifications = collectSecurityNotificationRequests(
      {
        passcodeEnabled: false,
        passwordEnabled: false,
        passkeyEnabled: true,
        biometricsEnabled: false,
        hasEnrolledMfaFactor: false,
      },
      {
        passcodeEnabled: true,
        passwordEnabled: true,
        passkeyEnabled: false,
        biometricsEnabled: true,
        hasEnrolledMfaFactor: true,
      },
      "event-1"
    );

    expect(notifications.map((item) => item.type)).toEqual([
      "passcode_enabled",
      "password_enabled",
      "biometrics_enabled",
      "passkey_disabled",
      "mfa_enabled",
    ]);
  });

  it("returns an empty list when nothing relevant changes", () => {
    const notifications = collectSecurityNotificationRequests(
      {
        passcodeEnabled: true,
        passwordEnabled: true,
        passkeyEnabled: true,
        biometricsEnabled: true,
        hasEnrolledMfaFactor: true,
      },
      {
        passcodeEnabled: true,
        passwordEnabled: true,
        passkeyEnabled: true,
        biometricsEnabled: true,
        hasEnrolledMfaFactor: true,
      },
      "event-2"
    );

    expect(notifications).toEqual([]);
  });
});
