import { beforeEach, describe, expect, it, vi } from "vitest";
import { Timestamp } from "firebase-admin/firestore";
import { GenerateEmailVerification } from "../../../application/usecase/GenerateEmailVerification.js";

const baseSecuritySettings = {
  allowFederatedLinking: false,
  passcodeEnabled: false,
  passwordEnabled: false,
  passkeyEnabled: false,
  biometricsRequired: false,
  biometricsEnabled: false,
  biometricsEnabledAt: null,
  lockAfterMinutes: 5,
  onboardingRequired: {},
  onboardingCompleted: {},
  emailToVerify: null,
  emailVerificationSentAt: null,
  emailVerificationAttemptsToday: 0,
  hasVerifiedEmail: false,
  hasAddedHomeAddress: false,
  hasVerifiedIdentity: false,
  hasSkippedMfaEnrollmentPrompt: false,
  hasSkippedPasskeyEnrollmentPrompt: true,
  hasEnrolledMfaFactor: false,
  mfaEnrolledAt: null,
  kycStatus: null,
  localPassCodeSetAt: null,
  localPasswordSetAt: null,
};

describe("GenerateEmailVerification", () => {
  const securityRepo = {
    get: vi.fn(),
    createIfMissing: vi.fn(),
    update: vi.fn(),
  };
  const userRepo = {
    getById: vi.fn(),
    updatePhoneNumber: vi.fn(),
    logAuditEvent: vi.fn(),
  };
  const authService = {
    getUser: vi.fn(),
    updateUserEmail: vi.fn(),
    generateEmailVerificationLink: vi.fn(),
  };
  const mailer = {
    sendVerificationEmail: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();

    securityRepo.createIfMissing.mockResolvedValue(undefined);
    securityRepo.get.mockResolvedValue({
      ...baseSecuritySettings,
      emailVerificationSentAt: null,
      emailVerificationAttemptsToday: 0,
      hasVerifiedEmail: false,
    });
    securityRepo.update.mockResolvedValue(undefined);

    userRepo.getById.mockResolvedValue({
      uid: "uid-1",
      tenantId: "production",
    });

    authService.getUser.mockResolvedValue({
      uid: "uid-1",
      email: "tester@example.com",
      tenantId: "production",
    });
    authService.updateUserEmail.mockResolvedValue(undefined);
    authService.generateEmailVerificationLink.mockResolvedValue(
      "https://pay-smart.net/verify?token=test"
    );

    mailer.sendVerificationEmail.mockResolvedValue(undefined);
  });

  it("sends verification email after ensuring security settings", async () => {
    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["production"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => true,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
    });

    expect(result).toEqual({ sent: true });
    expect(authService.getUser).toHaveBeenCalledWith("uid-1");
    expect(securityRepo.createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(authService.updateUserEmail).toHaveBeenCalledWith(
      "uid-1",
      "tester@example.com"
    );
    expect(authService.generateEmailVerificationLink).toHaveBeenCalledWith(
      "tester@example.com",
      "https://pay-smart.net/verify"
    );
    expect(securityRepo.update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        emailToVerify: "tester@example.com",
        emailVerificationSentAt: expect.any(Timestamp),
      })
    );
    expect(mailer.sendVerificationEmail).toHaveBeenCalled();
  });

  it("appends the return route to the continue url when provided", async () => {
    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["production"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => true,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
      returnRoute: "profile/mfa_nudge",
    });

    expect(result).toEqual({ sent: true });
    expect(authService.generateEmailVerificationLink).toHaveBeenCalledWith(
      "tester@example.com",
      "https://pay-smart.net/verify?returnRoute=profile%2Fmfa_nudge"
    );
  });

  it("does not fail when firestore user profile is missing and falls back to auth tenant", async () => {
    userRepo.getById.mockResolvedValue(null);
    authService.getUser.mockResolvedValue({
      uid: "uid-1",
      email: "tester@example.com",
      tenantId: "development",
    });

    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["development"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => false,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
    });

    expect(result).toEqual({ sent: true });
    expect(securityRepo.createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(mailer.sendVerificationEmail).not.toHaveBeenCalled();
  });

  it("returns cooldown metadata instead of sending again during the resend window", async () => {
    const now = Timestamp.fromMillis(Date.UTC(2026, 2, 19, 12, 0, 30));
    vi.spyOn(Timestamp, "now").mockReturnValue(now);
    securityRepo.get.mockResolvedValue({
      ...baseSecuritySettings,
      emailVerificationSentAt: Timestamp.fromMillis(Date.UTC(2026, 2, 19, 12, 0, 0)),
      emailVerificationAttemptsToday: 1,
      hasVerifiedEmail: false,
    });

    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["production"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => true,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
    });

    expect(result).toEqual({
      sent: false,
      reason: "cooldown",
      retryAfter: 30,
    });
    expect(authService.updateUserEmail).not.toHaveBeenCalled();
    expect(mailer.sendVerificationEmail).not.toHaveBeenCalled();
  });

  it("resets the daily attempt count when the last send happened on a previous utc day", async () => {
    const now = Timestamp.fromMillis(Date.UTC(2026, 2, 19, 8, 0, 0));
    vi.spyOn(Timestamp, "now").mockReturnValue(now);
    securityRepo.get.mockResolvedValue({
      ...baseSecuritySettings,
      emailVerificationSentAt: Timestamp.fromMillis(Date.UTC(2026, 2, 18, 23, 0, 0)),
      emailVerificationAttemptsToday: 5,
      hasVerifiedEmail: false,
    });

    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["production"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => true,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
    });

    expect(result).toEqual({ sent: true });
    expect(securityRepo.update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        emailVerificationAttemptsToday: 1,
      })
    );
    expect(mailer.sendVerificationEmail).toHaveBeenCalled();
  });

  it("returns a daily limit response when the limit is reached on the same utc day", async () => {
    const now = Timestamp.fromMillis(Date.UTC(2026, 2, 19, 22, 0, 0));
    vi.spyOn(Timestamp, "now").mockReturnValue(now);
    securityRepo.get.mockResolvedValue({
      ...baseSecuritySettings,
      emailVerificationSentAt: Timestamp.fromMillis(Date.UTC(2026, 2, 19, 10, 0, 0)),
      emailVerificationAttemptsToday: 5,
      hasVerifiedEmail: false,
    });

    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["production"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => true,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
    });

    expect(result).toEqual({
      sent: false,
      reason: "daily_limit",
      retryAfter: 7200,
    });
    expect(authService.updateUserEmail).not.toHaveBeenCalled();
    expect(mailer.sendVerificationEmail).not.toHaveBeenCalled();
  });

  it("returns already verified when the account email is already confirmed", async () => {
    securityRepo.get.mockResolvedValue({
      ...baseSecuritySettings,
      hasVerifiedEmail: true,
    });

    const usecase = new GenerateEmailVerification(
      securityRepo,
      userRepo,
      authService,
      mailer,
      {
        allowedTenants: new Set(["production"]),
        getVerifyUrl: () => "https://pay-smart.net/verify",
        shouldSendRealEmails: () => true,
      }
    );

    const result = await usecase.execute({
      uid: "uid-1",
      email: "tester@example.com",
    });

    expect(result).toEqual({
      sent: false,
      reason: "already_verified",
      retryAfter: undefined,
    });
    expect(authService.updateUserEmail).not.toHaveBeenCalled();
    expect(mailer.sendVerificationEmail).not.toHaveBeenCalled();
  });
});
