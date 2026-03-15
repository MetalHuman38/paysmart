import {
  FlutterwaveFundingAccount,
  ProvisionFlutterwaveFundingAccountInput,
  ProvisionFlutterwaveFundingAccountResult,
} from "../model/flutterwaveFundingAccount.js";

export interface FlutterwaveFundingAccountRepository {
  getCurrent(uid: string): Promise<FlutterwaveFundingAccount | null>;

  provision(
    uid: string,
    input: ProvisionFlutterwaveFundingAccountInput
  ): Promise<ProvisionFlutterwaveFundingAccountResult>;
}
