export type FlutterwaveAddMoneySessionStatus =
  | "created"
  | "pending"
  | "succeeded"
  | "failed"
  | "expired";

export interface CreateFlutterwaveAddMoneySessionInput {
  amountMinor: number;
  currency: string;
  idempotencyKey?: string;
}

export interface FlutterwaveVirtualAccountDetails {
  accountNumber: string;
  bankName: string;
  accountName?: string;
  reference: string;
  note?: string;
}

export interface FlutterwaveAddMoneySession {
  sessionId: string;
  provider: "flutterwave";
  checkoutUrl?: string;
  amountMinor: number;
  currency: string;
  status: FlutterwaveAddMoneySessionStatus;
  expiresAtMs: number;
  flutterwaveTransactionId?: string;
  virtualAccount?: FlutterwaveVirtualAccountDetails;
  publicKey?: string;
}

export interface FlutterwaveAddMoneySessionStatusResult
  extends FlutterwaveAddMoneySession {
  failureCode?: string;
  failureMessage?: string;
  updatedAtMs?: number;
}

export interface FlutterwaveWebhookApplyResult {
  handled: boolean;
  sessionId?: string;
  uid?: string;
  status?: FlutterwaveAddMoneySessionStatus;
  eventType?: string;
}
