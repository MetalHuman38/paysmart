import { FlutterwaveFundingAccount } from "../../domain/model/flutterwaveFundingAccount.js";
import { FlutterwaveFundingAccountRepository } from "../../domain/repository/FlutterwaveFundingAccountRepository.js";

export class GetFlutterwaveFundingAccount {
  constructor(
    private readonly fundingAccounts: FlutterwaveFundingAccountRepository
  ) {}

  async execute(uid: string): Promise<FlutterwaveFundingAccount | null> {
    return this.fundingAccounts.getCurrent(uid);
  }
}
