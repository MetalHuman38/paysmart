export const COOLDOWN_SECONDS = 60;
export const MAX_DAILY_ATTEMPTS = 5;
export function evaluateEmailVerificationPolicy(sec, now) {
    if (sec.hasVerifiedEmail === true) {
        return { allowed: false };
    }
    const sentAt = sec.emailVerificationSentAt;
    const attempts = Number(sec.emailVerificationAttemptsToday ?? 0);
    if (sentAt) {
        const elapsed = now.seconds - sentAt.seconds;
        if (elapsed < COOLDOWN_SECONDS) {
            return {
                allowed: false,
                retryAfter: COOLDOWN_SECONDS - elapsed,
            };
        }
    }
    if (attempts >= MAX_DAILY_ATTEMPTS) {
        return { allowed: false };
    }
    return { allowed: true };
}
//# sourceMappingURL=EmailVerificationPolicy.js.map