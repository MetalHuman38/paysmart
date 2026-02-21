import { FieldValue } from "firebase-admin/firestore";
const E164_REGEX = /^\+[1-9]\d{7,14}$/;
export class ConfirmPhoneChanged {
    securityRepo;
    userRepo;
    constructor(securityRepo, userRepo) {
        this.securityRepo = securityRepo;
        this.userRepo = userRepo;
    }
    async execute(uid, phoneNumber) {
        const normalizedPhone = phoneNumber.trim();
        if (!E164_REGEX.test(normalizedPhone)) {
            throw new Error("Invalid phone number format");
        }
        await this.securityRepo.createIfMissing(uid);
        await Promise.all([
            this.userRepo.updatePhoneNumber(uid, normalizedPhone),
            this.securityRepo.update(uid, {
                updatedAt: FieldValue.serverTimestamp(),
            }),
            this.userRepo.logAuditEvent({
                uid,
                event: "phone_changed_confirmed",
                phoneNumber: normalizedPhone,
            }),
        ]);
    }
}
//# sourceMappingURL=ConfirmPhoneChanged.js.map