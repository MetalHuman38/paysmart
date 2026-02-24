export class CreateAddMoneySession {
    addMoney;
    constructor(addMoney) {
        this.addMoney = addMoney;
    }
    async execute(uid, input) {
        return this.addMoney.createSession(uid, input);
    }
}
//# sourceMappingURL=CreateAddMoneySession.js.map