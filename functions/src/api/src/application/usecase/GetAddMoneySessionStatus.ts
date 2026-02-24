import { AddMoneyRepository } from "../../domain/repository/AddMoneyRepository.js";
import { AddMoneySessionStatusResult } from "../../domain/model/addMoney.js";

export class GetAddMoneySessionStatus {
  constructor(private readonly addMoney: AddMoneyRepository) {}

  async execute(uid: string, sessionId: string): Promise<AddMoneySessionStatusResult> {
    return this.addMoney.getSessionStatus(uid, sessionId);
  }
}
