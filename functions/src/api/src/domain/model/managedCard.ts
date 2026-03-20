export type ManagedCardStatus = "active" | "detached";

export interface ManagedCard {
  id: string;
  provider: "stripe";
  brand: string;
  last4: string;
  expMonth: number;
  expYear: number;
  funding?: string;
  country?: string;
  fingerprint?: string;
  isDefault: boolean;
  status: ManagedCardStatus;
  createdAtMs: number;
  updatedAtMs: number;
}

export interface ManagedCardsListResult {
  cards: ManagedCard[];
  stripeCustomerId?: string;
  defaultPaymentMethodId?: string;
  updatedAtMs: number;
}

export interface PaymentSheetCustomerConfig {
  customerId: string;
  ephemeralKeySecret: string;
  defaultPaymentMethodId?: string;
}
