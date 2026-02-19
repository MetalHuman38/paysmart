import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { evaluateEmailVerificationPolicy } from "../../domain/service/EmailVerificationPolicy.js";
export class GenerateEmailVerification {
    securityRepo;
    userRepo;
    authService;
    mailer;
    config;
    constructor(securityRepo, userRepo, authService, mailer, config) {
        this.securityRepo = securityRepo;
        this.userRepo = userRepo;
        this.authService = authService;
        this.mailer = mailer;
        this.config = config;
    }
    async execute(input) {
        const { uid, email } = input;
        /* ---------- User ---------- */
        const user = await this.userRepo.getById(uid);
        if (!user)
            throw new Error("User profile missing");
        const tenantId = String(user.tenantId || "").toLowerCase();
        if (this.config.allowedTenants.size &&
            !this.config.allowedTenants.has(tenantId)) {
            throw new Error("Tenant not allowed");
        }
        /* ---------- Security ---------- */
        const sec = await this.securityRepo.get(uid);
        if (!sec)
            throw new Error("security/settings missing");
        const now = Timestamp.now();
        const decision = evaluateEmailVerificationPolicy(sec, now);
        if (!decision.allowed) {
            return { retryAfter: decision.retryAfter ?? 0 };
        }
        /* ---------- Persist attempt ---------- */
        await this.securityRepo.update(uid, {
            emailToVerify: email,
            emailVerificationSentAt: now,
            emailVerificationAttemptsToday: FieldValue.increment(1),
            updatedAt: FieldValue.serverTimestamp(),
        });
        /* ---------- Generate link ---------- */
        const link = await this.authService.generateEmailVerificationLink(email, this.config.getVerifyUrl());
        if (this.config.shouldSendRealEmails()) {
            await this.mailer.sendVerificationEmail({ to: email, link });
        }
        else {
            console.log(`[DEV] Email verification link for ${email}: ${link}`);
        }
        return { sent: true };
    }
}
//# sourceMappingURL=GenerateEmailVerification.js.map