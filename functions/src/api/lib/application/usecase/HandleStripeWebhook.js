export class HandleStripeWebhook {
    addMoney;
    constructor(addMoney) {
        this.addMoney = addMoney;
    }
    async execute(rawPayload, signatureHeader) {
        return this.addMoney.applyWebhook(rawPayload, signatureHeader);
    }
}
//# sourceMappingURL=HandleStripeWebhook.js.map