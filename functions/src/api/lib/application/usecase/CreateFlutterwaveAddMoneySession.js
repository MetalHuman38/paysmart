export class CreateFlutterwaveAddMoneySession {
    addMoney;
    constructor(addMoney) {
        this.addMoney = addMoney;
    }
    async execute(uid, input) {
        return this.addMoney.createSession(uid, input);
    }
}
//# sourceMappingURL=CreateFlutterwaveAddMoneySession.js.map