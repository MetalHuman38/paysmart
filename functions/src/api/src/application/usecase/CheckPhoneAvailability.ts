import {
  normalizePhone,
  validateE164,
} from "../../domain/policy/PhoneNumberPolicy.js";
import { AuthService } from "../../infrastructure/auth/FirebaseAuthService.js";

export class CheckPhoneAvailability {
  constructor(private readonly authRepo: AuthService) {}

  async execute(rawPhone: string): Promise<{ available: boolean }> {
    const phone = normalizePhone(rawPhone);
    validateE164(phone);

    const existingUser = await this.authRepo.getUserByPhone(phone);
    return { available: !existingUser };
  }
}
