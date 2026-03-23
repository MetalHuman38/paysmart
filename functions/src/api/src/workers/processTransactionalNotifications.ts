import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import { APP } from "../config/globals.js";
import { notificationContainer } from "../infrastructure/di/notificationContainer.js";
import type { TransactionalNotificationRequest } from "../services/notificationDeliveryService.js";

type IdentityUploadNotificationDoc = {
  status?: string | null;
  reviewDecisionReason?: string | null;
};

type WalletTransactionNotificationDoc = {
  type?: string | null;
  status?: string | null;
  provider?: string | null;
  amountMinor?: number | null;
  currency?: string | null;
};

type SecuritySettingsNotificationDoc = {
  passcodeEnabled?: boolean | null;
  passwordEnabled?: boolean | null;
  passkeyEnabled?: boolean | null;
  biometricsEnabled?: boolean | null;
  hasEnrolledMfaFactor?: boolean | null;
};

export const processIdentityReviewNotifications = onDocumentUpdated(
  {
    region: APP.region,
    document: "users/{uid}/identityUploads/{sessionId}",
    retry: false,
  },
  async (event) => {
    const before = event.data?.before.data() as IdentityUploadNotificationDoc | undefined;
    const after = event.data?.after.data() as IdentityUploadNotificationDoc | undefined;
    if (!after) {
      return;
    }

    const request = buildIdentityReviewNotificationRequest(
      before,
      after,
      event.params.sessionId as string
    );
    if (!request) {
      return;
    }

    const { notificationDelivery } = notificationContainer();
    await notificationDelivery.deliverToUser(event.params.uid as string, request);
  }
);

export const processWalletTransactionNotifications = onDocumentCreated(
  {
    region: APP.region,
    document: "users/{uid}/walletTransactions/{transactionId}",
    retry: false,
  },
  async (event) => {
    const doc = event.data?.data() as WalletTransactionNotificationDoc | undefined;
    if (!doc) {
      return;
    }

    const request = buildWalletTransactionNotificationRequest(
      doc,
      event.params.transactionId as string
    );
    if (!request) {
      return;
    }

    const { notificationDelivery } = notificationContainer();
    await notificationDelivery.deliverToUser(event.params.uid as string, request);
  }
);

export const processSecuritySettingNotifications = onDocumentUpdated(
  {
    region: APP.region,
    document: "users/{uid}/security/settings",
    retry: false,
  },
  async (event) => {
    const before = event.data?.before.data() as SecuritySettingsNotificationDoc | undefined;
    const after = event.data?.after.data() as SecuritySettingsNotificationDoc | undefined;
    if (!before || !after) {
      return;
    }

    const requests = collectSecurityNotificationRequests(before, after, event.id);
    if (requests.length === 0) {
      return;
    }

    const { notificationDelivery } = notificationContainer();
    for (const request of requests) {
      await notificationDelivery.deliverToUser(event.params.uid as string, request);
    }
  }
);

export function buildIdentityReviewNotificationRequest(
  before: IdentityUploadNotificationDoc | undefined,
  after: IdentityUploadNotificationDoc,
  sessionId: string
): TransactionalNotificationRequest | null {
  const status = normalizeString(after.status);
  if (status !== "verified" && status !== "rejected") {
    return null;
  }
  if (status === normalizeString(before?.status)) {
    return null;
  }

  if (status === "verified") {
    return {
      notificationId: `identity_review_verified_${sessionId}`,
      type: "identity_verified",
      channel: "account_updates",
      title: "Identity verified",
      body: "Your identity check is complete and your account is ready to use.",
      metadata: {
        sessionId,
        status,
      },
    };
  }

  return {
    notificationId: `identity_review_rejected_${sessionId}`,
    type: "identity_review_rejected",
    channel: "account_updates",
    title: "Identity check needs attention",
    body: buildIdentityRejectedBody(after.reviewDecisionReason),
    metadata: {
      sessionId,
      status,
      reviewDecisionReason: normalizeString(after.reviewDecisionReason) || null,
    },
  };
}

export function buildWalletTransactionNotificationRequest(
  doc: WalletTransactionNotificationDoc,
  transactionId: string
): TransactionalNotificationRequest | null {
  if (normalizeString(doc.type) !== "top_up") {
    return null;
  }
  if (normalizeString(doc.status) !== "succeeded") {
    return null;
  }

  const amountMinor =
    typeof doc.amountMinor === "number" && Number.isFinite(doc.amountMinor) ?
      Math.round(doc.amountMinor) :
      0;
  const currency = normalizeCurrency(doc.currency);
  const provider = normalizeString(doc.provider) || "wallet";

  return {
    notificationId: `wallet_top_up_succeeded_${transactionId}`,
    type: "wallet_top_up_succeeded",
    channel: "account_updates",
    title: "Money added to your wallet",
    body: `Your ${formatMoney(amountMinor, currency)} top up is now available in PaySmart.`,
    metadata: {
      transactionId,
      provider,
      currency,
      amountMinor,
    },
  };
}

export function collectSecurityNotificationRequests(
  before: SecuritySettingsNotificationDoc,
  after: SecuritySettingsNotificationDoc,
  eventId: string
): TransactionalNotificationRequest[] {
  const requests: TransactionalNotificationRequest[] = [];

  if (!isEnabled(before.passcodeEnabled) && isEnabled(after.passcodeEnabled)) {
    requests.push(
      buildSecurityNotificationRequest(eventId, "passcode_enabled", "Passcode enabled", "A new passcode is now protecting your PaySmart account.")
    );
  }

  if (!isEnabled(before.passwordEnabled) && isEnabled(after.passwordEnabled)) {
    requests.push(
      buildSecurityNotificationRequest(eventId, "password_enabled", "Password protection enabled", "Your PaySmart account now has password protection enabled.")
    );
  }

  if (!isEnabled(before.biometricsEnabled) && isEnabled(after.biometricsEnabled)) {
    requests.push(
      buildSecurityNotificationRequest(eventId, "biometrics_enabled", "Biometric unlock enabled", "Biometric unlock is now enabled for your PaySmart account.")
    );
  }

  if (!isEnabled(before.passkeyEnabled) && isEnabled(after.passkeyEnabled)) {
    requests.push(
      buildSecurityNotificationRequest(eventId, "passkey_enabled", "Passkey enabled", "A passkey was added to your PaySmart sign-in methods.")
    );
  }

  if (isEnabled(before.passkeyEnabled) && !isEnabled(after.passkeyEnabled)) {
    requests.push(
      buildSecurityNotificationRequest(eventId, "passkey_disabled", "Passkey removed", "A passkey was removed from your PaySmart account. Review your security settings if this was not you.")
    );
  }

  if (!isEnabled(before.hasEnrolledMfaFactor) && isEnabled(after.hasEnrolledMfaFactor)) {
    requests.push(
      buildSecurityNotificationRequest(eventId, "mfa_enabled", "Two-step verification enabled", "Two-step verification is now active on your PaySmart account.")
    );
  }

  return requests;
}

function buildSecurityNotificationRequest(
  eventId: string,
  type: string,
  title: string,
  body: string
): TransactionalNotificationRequest {
  return {
    notificationId: `security_${type}_${eventId}`,
    type,
    channel: "account_updates",
    title,
    body,
    metadata: {
      securityEvent: type,
    },
  };
}

function buildIdentityRejectedBody(reason: string | null | undefined): string {
  switch (normalizeString(reason)) {
    case "name_mismatch":
      return "We could not match your ID details to your PaySmart profile. Open the app and try again.";
    case "ocr_processing_failed":
    case "name_extraction_missing":
      return "We could not read your document clearly enough. Open the app to upload a clearer image.";
    default:
      return "We could not complete your identity check. Open the app to review the next steps.";
  }
}

function normalizeString(value: string | null | undefined): string {
  return value?.trim().toLowerCase() || "";
}

function normalizeCurrency(value: string | null | undefined): string {
  const currency = value?.trim().toUpperCase() || "GBP";
  return currency.length === 3 ? currency : "GBP";
}

function formatMoney(amountMinor: number, currency: string): string {
  const amountMajor = amountMinor / 100;
  try {
    return new Intl.NumberFormat("en-GB", {
      style: "currency",
      currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(amountMajor);
  } catch {
    return `${currency} ${amountMajor.toFixed(2)}`;
  }
}

function isEnabled(value: boolean | null | undefined): boolean {
  return value === true;
}
