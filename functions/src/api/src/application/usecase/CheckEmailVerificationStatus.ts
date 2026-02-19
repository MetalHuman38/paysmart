import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class CheckEmailVerificationStatus {
  constructor(
    private readonly securityRepo: SecuritySettingsRepository
  ) {}

  async execute(uid: string): Promise<{ verified: boolean }> {
    const sec = await this.securityRepo.get(uid);
    return {
      verified: Boolean(sec?.hasVerifiedEmail),
    };
  }
}
