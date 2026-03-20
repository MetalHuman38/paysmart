import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { UserRepository } from "../../domain/repository/UserRepository.js";
import { UpsertVerifiedPhoneUser } from "./UpsertVerifiedPhoneUser.js";

export type FinalizePhoneSignupAuthUser = {
  uid: string;
  email?: string;
  phoneNumber?: string;
  isAnonymous?: boolean;
  providerIds?: string[];
  tenantId?: string | null;
  photoURL?: string;
  displayName?: string;
};

export class FinalizePhoneSignup {
  constructor(
    private readonly securityRepo: SecuritySettingsRepository,
    private readonly userRepo: UserRepository
  ) {}

  async execute(user: FinalizePhoneSignupAuthUser): Promise<void> {
    const upserter = new UpsertVerifiedPhoneUser(this.securityRepo, this.userRepo);
    await upserter.execute({
      user,
      auditEvent: "phone_signup_finalized",
    });
  }
}
