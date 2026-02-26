import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class CheckEmailVerificationStatus {
  constructor(
    private readonly securityRepo: SecuritySettingsRepository,
    private readonly authService?: {
      getUser(uid: string): Promise<{ emailVerified?: boolean }>;
    }
  ) {}

  async execute(uid: string): Promise<{ verified: boolean }> {
    const sec = await this.securityRepo.get(uid);
    if (sec?.hasVerifiedEmail) {
      return { verified: true };
    }

    if (this.authService) {
      const user = await this.authService.getUser(uid);
      if (user.emailVerified) {
        await this.securityRepo.update(uid, {
          hasVerifiedEmail: true,
          emailToVerify: null,
          updatedAt: FieldValue.serverTimestamp() as any,
        });
        return { verified: true };
      }
    }

    return {
      verified: false,
    };
  }
}
