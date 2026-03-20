export class ListManagedCards {
    managedCards;
    constructor(managedCards) {
        this.managedCards = managedCards;
    }
    async execute(uid) {
        return this.managedCards.list(uid);
    }
}
//# sourceMappingURL=ListManagedCards.js.map