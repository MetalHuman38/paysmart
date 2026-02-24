export class GetAddMoneySessionStatus {
    addMoney;
    constructor(addMoney) {
        this.addMoney = addMoney;
    }
    async execute(uid, sessionId) {
        return this.addMoney.getSessionStatus(uid, sessionId);
    }
}
//# sourceMappingURL=GetAddMoneySessionStatus.js.map