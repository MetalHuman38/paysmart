import { AuthService } from "../../infrastructure/auth/FirebaseAuthService.js";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { UserRepository } from "../../domain/repository/UserRepository.js";
import { UpsertVerifiedPhoneUser } from "./UpsertVerifiedPhoneUser.js";

export class ConfirmPhoneChanged {
  constructor(
    private readonly authService: AuthService,
    private readonly securityRepo: SecuritySettingsRepository,
    private readonly userRepo: UserRepository
  ) {}

  async execute(uid: string, phoneNumber: string): Promise<void> {
    const authUser = await this.authService.getUser(uid);
    const upserter = new UpsertVerifiedPhoneUser(this.securityRepo, this.userRepo);
    await upserter.execute({
      user: authUser,
      expectedPhoneNumber: phoneNumber.trim(),
      auditEvent: "phone_changed_confirmed",
    });
  }
}
