import { FlutterwaveAddMoneyRepository } from "../../domain/repository/FlutterwaveAddMoneyRepository.js";
import { FlutterwaveAddMoneySessionStatusResult } from "../../domain/model/flutterwaveAddMoney.js";

export class GetFlutterwaveAddMoneySessionStatus {
  constructor(private readonly addMoney: FlutterwaveAddMoneyRepository) {}

  async execute(
    uid: string,
    sessionId: string
  ): Promise<FlutterwaveAddMoneySessionStatusResult> {
    return this.addMoney.getSessionStatus(uid, sessionId);
  }
}
