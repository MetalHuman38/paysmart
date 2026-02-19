import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class EnableBiometrics {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string) {
    await this.repo.createIfMissing(uid);

    await this.repo.update(uid, {
      biometricsRequired: false,
      biometricsEnabled: true,
      biometricsEnabledAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });
  }
}
