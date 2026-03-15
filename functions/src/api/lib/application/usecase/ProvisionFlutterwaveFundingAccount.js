export class ProvisionFlutterwaveFundingAccount {
    fundingAccounts;
    constructor(fundingAccounts) {
        this.fundingAccounts = fundingAccounts;
    }
    async execute(uid, input) {
        return this.fundingAccounts.provision(uid, input);
    }
}
//# sourceMappingURL=ProvisionFlutterwaveFundingAccount.js.map