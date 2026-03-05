import {
  CreateFlutterwaveAddMoneySessionInput,
  FlutterwaveAddMoneySession,
  FlutterwaveAddMoneySessionStatusResult,
  FlutterwaveWebhookApplyResult,
} from "../model/flutterwaveAddMoney.js";

export interface FlutterwaveAddMoneyRepository {
  createSession(
    uid: string,
    input: CreateFlutterwaveAddMoneySessionInput
  ): Promise<FlutterwaveAddMoneySession>;

  getSessionStatus(
    uid: string,
    sessionId: string
  ): Promise<FlutterwaveAddMoneySessionStatusResult>;

  applyWebhook(
    rawPayload: string,
    signatureHeader?: string,
    signatureName?: string
  ): Promise<FlutterwaveWebhookApplyResult>;
}
