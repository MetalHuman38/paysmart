export class SetDefaultManagedCard {
    managedCards;
    constructor(managedCards) {
        this.managedCards = managedCards;
    }
    async execute(uid, paymentMethodId) {
        return this.managedCards.setDefault(uid, paymentMethodId);
    }
}
//# sourceMappingURL=SetDefaultManagedCard.js.map