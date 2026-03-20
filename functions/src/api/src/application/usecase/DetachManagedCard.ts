import { ManagedCardsListResult } from "../../domain/model/managedCard.js";
import { ManagedCardRepository } from "../../domain/repository/ManagedCardRepository.js";

export class DetachManagedCard {
  constructor(private readonly managedCards: ManagedCardRepository) {}

  async execute(uid: string, paymentMethodId: string): Promise<ManagedCardsListResult> {
    return this.managedCards.detach(uid, paymentMethodId);
  }
}
