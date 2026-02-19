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
      getUser(uid: string): Promise<{ email?: string }>;
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

    /* ---------- User ---------- */
    const user = await this.userRepo.getById(uid);
    if (!user) throw new Error("User profile missing");

    const tenantId = String(user.tenantId || "").toLowerCase();
    if (
      this.config.allowedTenants.size &&
      !this.config.allowedTenants.has(tenantId)
    ) {
      throw new Error("Tenant not allowed");
    }

    /* ---------- Security ---------- */
    const sec = await this.securityRepo.get(uid);
    if (!sec) throw new Error("security/settings missing");

    const now = Timestamp.now();
    const decision = evaluateEmailVerificationPolicy(sec, now);

    if (!decision.allowed) {
      return { retryAfter: decision.retryAfter ?? 0 };
    }

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
      console.log(`[DEV] Email verification link for ${email}: ${link}`);
    }

    return { sent: true };
  }
}
