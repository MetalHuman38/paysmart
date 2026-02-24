import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class SetHomeAddressVerified {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      hasAddedHomeAddress: true,
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}
