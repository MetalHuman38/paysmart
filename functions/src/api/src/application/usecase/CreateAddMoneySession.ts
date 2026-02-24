import { AddMoneyRepository } from "../../domain/repository/AddMoneyRepository.js";
import {
  AddMoneySession,
  CreateAddMoneySessionInput,
} from "../../domain/model/addMoney.js";

export class CreateAddMoneySession {
  constructor(private readonly addMoney: AddMoneyRepository) {}

  async execute(
    uid: string,
    input: CreateAddMoneySessionInput
  ): Promise<AddMoneySession> {
    return this.addMoney.createSession(uid, input);
  }
}
