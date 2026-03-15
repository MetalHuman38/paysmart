import {
  ProvisionFlutterwaveFundingAccountInput,
  ProvisionFlutterwaveFundingAccountResult,
} from "../../domain/model/flutterwaveFundingAccount.js";
import { FlutterwaveFundingAccountRepository } from "../../domain/repository/FlutterwaveFundingAccountRepository.js";

export class ProvisionFlutterwaveFundingAccount {
  constructor(
    private readonly fundingAccounts: FlutterwaveFundingAccountRepository
  ) {}

  async execute(
    uid: string,
    input: ProvisionFlutterwaveFundingAccountInput
  ): Promise<ProvisionFlutterwaveFundingAccountResult> {
    return this.fundingAccounts.provision(uid, input);
  }
}
