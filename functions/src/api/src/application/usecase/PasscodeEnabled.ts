import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class PasscodeEnabled {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      passcodeEnabled: true,
      localPassCodeSetAt: FieldValue.serverTimestamp() as any,
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}
