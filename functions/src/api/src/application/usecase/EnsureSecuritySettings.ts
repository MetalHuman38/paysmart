import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { FieldValue } from "firebase-admin/firestore";

export class EnsureSecuritySettings {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}
  async execute(uid: string) {
    await this.repo.createIfMissing(uid);

    // Update the updatedAt field to trigger any listeners that depend on the security settings document
    await this.repo.update(uid, {
    updatedAt: FieldValue.serverTimestamp(),
    });
  }
}