import { HttpsError } from "firebase-functions/v2/https";
import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import { FirebaseAuthServiceInterface } from "../../Infra/auth/FirebaseAuthServiceInterface.js";
import { SecuritySettingsRepository } from "../../domain/Interface/SecuritySettingsInterface.js";

export class BeforeCreatePolicyUsecase {
  constructor(
    private readonly authService: FirebaseAuthServiceInterface,
    private readonly securityRepo: SecuritySettingsRepository
  ) {}

  async execute(event: AuthBlockingEvent) {
    const user = event.data;
    const resolvedPhone = await this.resolvePhoneNumberFromAuth(user?.uid, user?.phoneNumber);

    if (!resolvedPhone) {
      // Exception for credential-linking intent:
      // allow create path only when an existing Firebase Auth account can be found
      // by email and that account already has a verified phone number.
      if (!user?.email) {
        throw new HttpsError(
          "permission-denied",
          "You must sign up with a verified phone number first."
        );
      }

      try {
        const existingByEmail = await this.authService.getUserByEmail(user.email);
        if (!existingByEmail?.phoneNumber) {
          throw new HttpsError(
            "permission-denied",
            "You must sign up with a verified phone number first."
          );
        }
      } catch (err: any) {
        if (err instanceof HttpsError) {
          throw err;
        }
        throw new HttpsError("internal", "Account lookup failed");
      }
    } else {
      try {
        const existingUser = await this.authService.getUserByPhone(resolvedPhone);
        if (existingUser && existingUser.uid !== user?.uid) {
          throw new HttpsError("already-exists", "Phone number already registered");
        }
      } catch (err: any) {
        if (err instanceof HttpsError) {
          throw err;
        }
        throw new HttpsError("internal", "Phone lookup failed");
      }
    }

    // Seed security settings on auth create path as well.
    if (user?.uid) {
      await this.securityRepo.createIfMissing(user.uid);
    }
  }

  private async resolvePhoneNumberFromAuth(
    uid?: string,
    phoneNumber?: string | null
  ): Promise<string | null> {
    if (phoneNumber) {
      return phoneNumber;
    }

    if (!uid) {
      return null;
    }

    try {
      const existing = await this.authService.getUserByUid(uid);
      return existing?.phoneNumber ?? null;
    } catch {
      throw new HttpsError("internal", "Account lookup failed");
    }
  }
}
