import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class SetPasskeyEnabled {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string, passkeyEnabled: boolean) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      passkeyEnabled,
      hasSkippedPasskeyEnrollmentPrompt: !passkeyEnabled,
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}
