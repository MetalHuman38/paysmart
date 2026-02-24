import { AddMoneyRepository } from "../../domain/repository/AddMoneyRepository.js";
import { StripeWebhookApplyResult } from "../../domain/model/addMoney.js";

export class HandleStripeWebhook {
  constructor(private readonly addMoney: AddMoneyRepository) {}

  async execute(
    rawPayload: string,
    signatureHeader?: string
  ): Promise<StripeWebhookApplyResult> {
    return this.addMoney.applyWebhook(rawPayload, signatureHeader);
  }
}
