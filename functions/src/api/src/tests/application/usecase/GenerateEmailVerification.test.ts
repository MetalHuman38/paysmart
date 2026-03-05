import { beforeEach, describe, expect, it, vi } from "vitest";
import { Timestamp } from "firebase-admin/firestore";
import { getDefaultSecuritySettings } from "../../../constants/index.js";
import { GenerateEmailVerification } from "../../../application/usecase/GenerateEmailVerification.js";

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
      ...getDefaultSecuritySettings(),
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
    expect(securityRepo.update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        emailToVerify: "tester@example.com",
        emailVerificationSentAt: expect.any(Timestamp),
      })
    );
    expect(mailer.sendVerificationEmail).toHaveBeenCalled();
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
});
