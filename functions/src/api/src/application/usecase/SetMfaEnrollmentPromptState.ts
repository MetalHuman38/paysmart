import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class SetMfaEnrollmentPromptState {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string, hasSkippedMfaEnrollmentPrompt: boolean) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      hasSkippedMfaEnrollmentPrompt,
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}
