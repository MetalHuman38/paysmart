export class DetachManagedCard {
    managedCards;
    constructor(managedCards) {
        this.managedCards = managedCards;
    }
    async execute(uid, paymentMethodId) {
        return this.managedCards.detach(uid, paymentMethodId);
    }
}
//# sourceMappingURL=DetachManagedCard.js.map