export class GetFlutterwaveFundingAccount {
    fundingAccounts;
    constructor(fundingAccounts) {
        this.fundingAccounts = fundingAccounts;
    }
    async execute(uid) {
        return this.fundingAccounts.getCurrent(uid);
    }
}
//# sourceMappingURL=GetFlutterwaveFundingAccount.js.map