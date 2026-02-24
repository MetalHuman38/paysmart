import {
  AddMoneySession,
  AddMoneySessionStatusResult,
  CreateAddMoneySessionInput,
  StripeWebhookApplyResult,
} from "../model/addMoney.js";

export interface AddMoneyRepository {
  createSession(
    uid: string,
    input: CreateAddMoneySessionInput
  ): Promise<AddMoneySession>;

  getSessionStatus(
    uid: string,
    sessionId: string
  ): Promise<AddMoneySessionStatusResult>;

  applyWebhook(
    rawPayload: string,
    signatureHeader?: string
  ): Promise<StripeWebhookApplyResult>;
}
