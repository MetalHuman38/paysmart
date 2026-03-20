export type AddMoneySessionStatus =
  | "created"
  | "pending"
  | "succeeded"
  | "failed"
  | "expired";

export interface CreateAddMoneySessionInput {
  amountMinor: number;
  currency: string;
  idempotencyKey?: string;
}

export interface AddMoneySession {
  sessionId: string;
  checkoutUrl?: string;
  amountMinor: number;
  currency: string;
  status: AddMoneySessionStatus;
  expiresAtMs: number;
  paymentIntentId?: string;
  paymentIntentClientSecret?: string;
  publishableKey?: string;
  customerId?: string;
  customerEphemeralKeySecret?: string;
  defaultPaymentMethodId?: string;
}

export interface AddMoneySessionStatusResult extends AddMoneySession {
  paymentIntentId?: string;
  failureCode?: string;
  failureMessage?: string;
  updatedAtMs?: number;
}

export interface StripeWebhookApplyResult {
  handled: boolean;
  sessionId?: string;
  uid?: string;
  status?: AddMoneySessionStatus;
}
