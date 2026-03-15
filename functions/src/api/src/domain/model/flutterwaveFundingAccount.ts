export type FlutterwaveFundingAccountStatus =
  | "active"
  | "pending"
  | "disabled"
  | "failed";

export interface FlutterwaveFundingAccount {
  accountId: string;
  provider: "flutterwave";
  currency: "NGN";
  accountNumber: string;
  bankName: string;
  accountName: string;
  reference: string;
  status: FlutterwaveFundingAccountStatus;
  providerStatus: string;
  customerId: string;
  note?: string;
  createdAtMs: number;
  updatedAtMs: number;
}

export interface FlutterwaveFundingAccountKyc {
  bvn?: string;
  nin?: string;
}

export interface ProvisionFlutterwaveFundingAccountInput {
  idempotencyKey?: string;
  kyc?: FlutterwaveFundingAccountKyc;
}

export interface ProvisionFlutterwaveFundingAccountResult
  extends FlutterwaveFundingAccount {
  provisioningResult: "created" | "existing";
}
