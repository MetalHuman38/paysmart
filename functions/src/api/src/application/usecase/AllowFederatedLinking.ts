import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { FieldValue } from "firebase-admin/firestore";

export class AllowFederatedLinking {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      allowFederatedLinking: true,
      updatedAt: FieldValue.serverTimestamp(),
    });
  }
}