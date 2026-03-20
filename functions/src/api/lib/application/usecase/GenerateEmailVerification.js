import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { evaluateEmailVerificationPolicy, getEmailVerificationAttemptCountForCurrentDay, } from "../../domain/service/EmailVerificationPolicy.js";
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
        const authUser = await this.authService.getUser(uid);
        /* ---------- User ---------- */
        const user = await this.userRepo.getById(uid);
        const tenantId = String(user?.tenantId || authUser.tenantId || "").toLowerCase();
        if (this.config.allowedTenants.size &&
            !this.config.allowedTenants.has(tenantId)) {
            throw new Error("Tenant not allowed");
        }
        /* ---------- Security ---------- */
        await this.securityRepo.createIfMissing(uid);
        const sec = await this.securityRepo.get(uid);
        if (!sec)
            throw new Error("security/settings missing after ensure");
        const now = Timestamp.now();
        const decision = evaluateEmailVerificationPolicy(sec, now);
        if (!decision.allowed) {
            return {
                sent: false,
                reason: decision.reason,
                retryAfter: decision.retryAfter,
            };
        }
        const attemptsToday = getEmailVerificationAttemptCountForCurrentDay(sec, now);
        /* ---------- Bind email to current auth owner ---------- */
        await this.authService.updateUserEmail(uid, email);
        /* ---------- Persist attempt ---------- */
        await this.securityRepo.update(uid, {
            emailToVerify: email,
            emailVerificationSentAt: now,
            emailVerificationAttemptsToday: attemptsToday + 1,
            updatedAt: FieldValue.serverTimestamp(),
        });
        /* ---------- Generate link ---------- */
        const link = await this.authService.generateEmailVerificationLink(email, this.config.getVerifyUrl());
        if (this.config.shouldSendRealEmails()) {
            await this.mailer.sendVerificationEmail({ to: email, link });
        }
        else {
            const emailDomain = email.includes("@") ? email.split("@").pop() : "unknown";
            const linkHost = safeHost(link);
            console.log(`[DEV] Email verification link generated (domain=${emailDomain}, host=${linkHost})`);
        }
        return { sent: true };
    }
}
function safeHost(url) {
    try {
        return new URL(url).host;
    }
    catch {
        return "invalid-url";
    }
}
//# sourceMappingURL=GenerateEmailVerification.js.map