import { FieldValue } from "firebase-admin/firestore";
const E164_REGEX = /^\+[1-9]\d{7,14}$/;
export class UpsertVerifiedPhoneUser {
    securityRepo;
    userRepo;
    constructor(securityRepo, userRepo) {
        this.securityRepo = securityRepo;
        this.userRepo = userRepo;
    }
    async execute(input) {
        const normalizedPhone = this.normalizeVerifiedPhoneNumber(input.user.phoneNumber);
        const expectedPhoneNumber = input.expectedPhoneNumber?.trim();
        if (expectedPhoneNumber) {
            if (!E164_REGEX.test(expectedPhoneNumber)) {
                throw new Error("Invalid phone number format");
            }
            if (expectedPhoneNumber !== normalizedPhone) {
                throw new Error("Verified phone number does not match requested phone number");
            }
        }
        const providerIds = Array.from(new Set((input.user.providerIds ?? []).map((provider) => provider.trim()).filter(Boolean)));
        if (!providerIds.includes("phone")) {
            providerIds.push("phone");
        }
        await this.securityRepo.createIfMissing(input.user.uid);
        await Promise.all([
            this.userRepo.upsertVerifiedPhoneSignup({
                uid: input.user.uid,
                email: input.user.email,
                phoneNumber: normalizedPhone,
                isAnonymous: Boolean(input.user.isAnonymous),
                providerIds,
                tenantId: input.user.tenantId ?? null,
                photoURL: input.user.photoURL,
                displayName: input.user.displayName,
            }),
            this.securityRepo.update(input.user.uid, {
                updatedAt: FieldValue.serverTimestamp(),
            }),
            this.userRepo.logAuditEvent({
                uid: input.user.uid,
                event: input.auditEvent,
                phoneNumber: normalizedPhone,
            }),
        ]);
    }
    normalizeVerifiedPhoneNumber(phoneNumber) {
        const normalizedPhone = phoneNumber?.trim();
        if (!normalizedPhone || !E164_REGEX.test(normalizedPhone)) {
            throw new Error("Verified phone number is unavailable");
        }
        return normalizedPhone;
    }
}
//# sourceMappingURL=UpsertVerifiedPhoneUser.js.map