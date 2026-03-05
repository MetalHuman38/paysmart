import { FlutterwaveAddMoneyRepository } from "../../domain/repository/FlutterwaveAddMoneyRepository.js";
import { FlutterwaveWebhookApplyResult } from "../../domain/model/flutterwaveAddMoney.js";

export class HandleFlutterwaveWebhook {
  constructor(private readonly addMoney: FlutterwaveAddMoneyRepository) {}

  async execute(
    rawPayload: string,
    signatureHeader?: string,
    signatureName?: string
  ): Promise<FlutterwaveWebhookApplyResult> {
    return this.addMoney.applyWebhook(rawPayload, signatureHeader, signatureName);
  }
}
