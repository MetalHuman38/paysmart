import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { UserRepository } from "../../domain/repository/UserRepository.js";
import {
  evaluateEmailVerificationPolicy,
  getEmailVerificationAttemptCountForCurrentDay,
} from "../../domain/service/EmailVerificationPolicy.js";

export type GenerateEmailVerificationResult =
  | { sent: true }
  | {
      sent: false;
      reason: "already_verified" | "cooldown" | "daily_limit";
      retryAfter?: number;
    };

export class GenerateEmailVerification {
  constructor(
    private readonly securityRepo: SecuritySettingsRepository,
    private readonly userRepo: UserRepository,
    private readonly authService: {
      getUser(
        uid: string
      ): Promise<{ email?: string; tenantId?: string | null }>;
      updateUserEmail(uid: string, email: string): Promise<void>;
      generateEmailVerificationLink(
        email: string,
        continueUrl: string
      ): Promise<string>;
    },
    private readonly mailer: {
      sendVerificationEmail(input: {
        to: string;
        verificationLink: string;
        locale?: string;
      }): Promise<void>;
    },
    private readonly config: {
      allowedTenants: Set<string>;
      getVerifyUrl(): string;
      shouldSendRealEmails(): boolean;
    }
  ) {}

  async execute(input: {
    uid: string;
    email: string;
    returnRoute?: string | null;
    provider?: string | null;
    ip?: string;
  }): Promise<GenerateEmailVerificationResult> {
    const { uid, email } = input;

    const authUser = await this.authService.getUser(uid);

    /* ---------- User ---------- */
    const user = await this.userRepo.getById(uid);
    const tenantId = String(user?.tenantId || authUser.tenantId || "").toLowerCase();
    if (
      this.config.allowedTenants.size &&
      !this.config.allowedTenants.has(tenantId)
    ) {
      throw new Error("Tenant not allowed");
    }

    /* ---------- Security ---------- */
    await this.securityRepo.createIfMissing(uid);
    const sec = await this.securityRepo.get(uid);
    if (!sec) throw new Error("security/settings missing after ensure");

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
    const link = await this.authService.generateEmailVerificationLink(
      email,
      buildEmailVerificationContinueUrl(
        this.config.getVerifyUrl(),
        input.returnRoute
      )
    );

    if (this.config.shouldSendRealEmails()) {
      await this.mailer.sendVerificationEmail({
        to: email,
        verificationLink: link,
      });
    } else {
      const emailDomain = email.includes("@") ? email.split("@").pop() : "unknown";
      const linkHost = safeHost(link);
      console.log(
        `[DEV] Email verification link generated (domain=${emailDomain}, host=${linkHost})`
      );
    }

    return { sent: true };
  }
}

function safeHost(url: string): string {
  try {
    return new URL(url).host;
  } catch {
    return "invalid-url";
  }
}

function buildEmailVerificationContinueUrl(
  baseUrl: string,
  returnRoute?: string | null
): string {
  const url = new URL(baseUrl);
  const normalizedRoute = normalizeReturnRoute(returnRoute);
  if (normalizedRoute) {
    url.searchParams.set("returnRoute", normalizedRoute);
  }
  return url.toString();
}

function normalizeReturnRoute(value?: string | null): string | null {
  if (typeof value !== "string") {
    return null;
  }

  const normalized = value.trim();
  if (!normalized) {
    return null;
  }

  if (normalized.includes("://") || normalized.startsWith("//")) {
    return null;
  }

  return normalized.slice(0, 512);
}
