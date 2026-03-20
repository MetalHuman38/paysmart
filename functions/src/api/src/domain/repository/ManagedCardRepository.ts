import {
  ManagedCardsListResult,
  PaymentSheetCustomerConfig,
} from "../model/managedCard.js";

export interface ManagedCardRepository {
  preparePaymentSheetCustomer(uid: string): Promise<PaymentSheetCustomerConfig>;
  list(uid: string): Promise<ManagedCardsListResult>;
  detach(uid: string, paymentMethodId: string): Promise<ManagedCardsListResult>;
  setDefault(uid: string, paymentMethodId: string): Promise<ManagedCardsListResult>;
  syncFromProvider(uid: string): Promise<ManagedCardsListResult>;
}
