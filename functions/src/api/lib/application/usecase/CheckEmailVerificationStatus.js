import { FieldValue } from "firebase-admin/firestore";
export class CheckEmailVerificationStatus {
    securityRepo;
    authService;
    constructor(securityRepo, authService) {
        this.securityRepo = securityRepo;
        this.authService = authService;
    }
    async execute(uid) {
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
                    updatedAt: FieldValue.serverTimestamp(),
                });
                return { verified: true };
            }
        }
        return {
            verified: false,
        };
    }
}
//# sourceMappingURL=CheckEmailVerificationStatus.js.map