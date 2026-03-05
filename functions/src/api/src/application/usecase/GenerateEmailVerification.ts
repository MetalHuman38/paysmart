import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { UserRepository } from "../../domain/repository/UserRepository.js";
import { evaluateEmailVerificationPolicy } from "../../domain/service/EmailVerificationPolicy.js";

export type GenerateEmailVerificationResult =
  | { sent: true }
  | { retryAfter: number };

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
      sendVerificationEmail(input: { to: string; link: string }): Promise<void>;
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
      return { retryAfter: decision.retryAfter ?? 0 };
    }

    /* ---------- Bind email to current auth owner ---------- */
    await this.authService.updateUserEmail(uid, email);

    /* ---------- Persist attempt ---------- */
    await this.securityRepo.update(uid, {
      emailToVerify: email,
      emailVerificationSentAt: now,
      emailVerificationAttemptsToday: FieldValue.increment(1) as any,
      updatedAt: FieldValue.serverTimestamp(),
    });

    /* ---------- Generate link ---------- */
    const link = await this.authService.generateEmailVerificationLink(
      email,
      this.config.getVerifyUrl()
    );

    if (this.config.shouldSendRealEmails()) {
      await this.mailer.sendVerificationEmail({ to: email, link });
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
