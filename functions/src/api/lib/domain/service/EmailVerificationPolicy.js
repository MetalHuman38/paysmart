export const COOLDOWN_SECONDS = 60;
export const MAX_DAILY_ATTEMPTS = 5;
export function evaluateEmailVerificationPolicy(sec, now) {
    if (sec.hasVerifiedEmail === true) {
        return { allowed: false, reason: "already_verified" };
    }
    const sentAt = sec.emailVerificationSentAt;
    const attempts = getEmailVerificationAttemptCountForCurrentDay(sec, now);
    if (sentAt) {
        const elapsed = now.seconds - sentAt.seconds;
        if (elapsed < COOLDOWN_SECONDS) {
            return {
                allowed: false,
                reason: "cooldown",
                retryAfter: COOLDOWN_SECONDS - elapsed,
            };
        }
    }
    if (attempts >= MAX_DAILY_ATTEMPTS) {
        return {
            allowed: false,
            reason: "daily_limit",
            retryAfter: secondsUntilNextUtcDay(now),
        };
    }
    return { allowed: true };
}
export function getEmailVerificationAttemptCountForCurrentDay(sec, now) {
    const sentAt = sec.emailVerificationSentAt;
    if (!sentAt) {
        return 0;
    }
    if (!isSameUtcDay(sentAt, now)) {
        return 0;
    }
    return Number(sec.emailVerificationAttemptsToday ?? 0);
}
function isSameUtcDay(left, right) {
    const leftDate = new Date(left.toMillis());
    const rightDate = new Date(right.toMillis());
    return leftDate.getUTCFullYear() === rightDate.getUTCFullYear() &&
        leftDate.getUTCMonth() === rightDate.getUTCMonth() &&
        leftDate.getUTCDate() === rightDate.getUTCDate();
}
function secondsUntilNextUtcDay(now) {
    const date = new Date(now.toMillis());
    const nextMidnightUtc = Date.UTC(date.getUTCFullYear(), date.getUTCMonth(), date.getUTCDate() + 1);
    return Math.max(1, Math.ceil((nextMidnightUtc - date.getTime()) / 1000));
}
//# sourceMappingURL=EmailVerificationPolicy.js.map