import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { UserRepository } from "../../domain/repository/UserRepository.js";

const E164_REGEX = /^\+[1-9]\d{7,14}$/;

export class ConfirmPhoneChanged {
  constructor(
    private readonly securityRepo: SecuritySettingsRepository,
    private readonly userRepo: UserRepository
  ) {}

  async execute(uid: string, phoneNumber: string): Promise<void> {
    const normalizedPhone = phoneNumber.trim();
    if (!E164_REGEX.test(normalizedPhone)) {
      throw new Error("Invalid phone number format");
    }

    await this.securityRepo.createIfMissing(uid);

    await Promise.all([
      this.userRepo.updatePhoneNumber(uid, normalizedPhone),
      this.securityRepo.update(uid, {
        updatedAt: FieldValue.serverTimestamp() as any,
      }),
      this.userRepo.logAuditEvent({
        uid,
        event: "phone_changed_confirmed",
        phoneNumber: normalizedPhone,
      }),
    ]);
  }
}
