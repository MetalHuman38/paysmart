export class CheckEmailVerificationStatus {
    securityRepo;
    constructor(securityRepo) {
        this.securityRepo = securityRepo;
    }
    async execute(uid) {
        const sec = await this.securityRepo.get(uid);
        return {
            verified: Boolean(sec?.hasVerifiedEmail),
        };
    }
}
//# sourceMappingURL=CheckEmailVerificationStatus.js.map