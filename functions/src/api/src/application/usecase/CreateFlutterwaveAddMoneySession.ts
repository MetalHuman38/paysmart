import { FlutterwaveAddMoneyRepository } from "../../domain/repository/FlutterwaveAddMoneyRepository.js";
import {
  CreateFlutterwaveAddMoneySessionInput,
  FlutterwaveAddMoneySession,
} from "../../domain/model/flutterwaveAddMoney.js";

export class CreateFlutterwaveAddMoneySession {
  constructor(private readonly addMoney: FlutterwaveAddMoneyRepository) {}

  async execute(
    uid: string,
    input: CreateFlutterwaveAddMoneySessionInput
  ): Promise<FlutterwaveAddMoneySession> {
    return this.addMoney.createSession(uid, input);
  }
}
