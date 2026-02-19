import { SecuritySettingsRepository } from "../repository/SecuritySettingsRepository.js";

export class EnsureSecuritySettings {
  constructor(
    private readonly repo: SecuritySettingsRepository
  ) {}

  async execute(uid: string): Promise<void> {
    if (!uid) {
      throw new Error("UID is required");
    }

    await this.repo.createIfMissing(uid);
  }
}
