import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class PasswordEnabled {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      passwordEnabled: true,
      localPasswordSetAt: FieldValue.serverTimestamp() as any,
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}
