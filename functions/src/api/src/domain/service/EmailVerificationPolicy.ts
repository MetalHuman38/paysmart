import { Timestamp } from "firebase-admin/firestore";
import { SecuritySettingsModel } from "../model/securitySettings.js";

export const COOLDOWN_SECONDS = 60;
export const MAX_DAILY_ATTEMPTS = 5;

export type VerificationDecision =
  | { allowed: true }
  | { allowed: false; retryAfter?: number };

export function evaluateEmailVerificationPolicy(
  sec: SecuritySettingsModel,
  now: Timestamp
): VerificationDecision {
  if (sec.hasVerifiedEmail === true) {
    return { allowed: false };
  }

  const sentAt = sec.emailVerificationSentAt as Timestamp | null;
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
