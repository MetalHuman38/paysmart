export class HandleFlutterwaveWebhook {
    addMoney;
    constructor(addMoney) {
        this.addMoney = addMoney;
    }
    async execute(rawPayload, signatureHeader, signatureName) {
        return this.addMoney.applyWebhook(rawPayload, signatureHeader, signatureName);
    }
}
//# sourceMappingURL=HandleFlutterwaveWebhook.js.map