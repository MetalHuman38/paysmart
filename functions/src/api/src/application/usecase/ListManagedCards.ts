import { ManagedCardsListResult } from "../../domain/model/managedCard.js";
import { ManagedCardRepository } from "../../domain/repository/ManagedCardRepository.js";

export class ListManagedCards {
  constructor(private readonly managedCards: ManagedCardRepository) {}

  async execute(uid: string): Promise<ManagedCardsListResult> {
    return this.managedCards.list(uid);
  }
}
