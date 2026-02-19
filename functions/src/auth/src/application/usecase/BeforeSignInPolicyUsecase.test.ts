import { beforeEach, describe, expect, it, vi } from "vitest";
import { BeforeSignInPolicyUsecase } from "./BeforeSignInPolicyUsecase.js";
import type { SecuritySettingsRepository } from "../../domain/Interface/SecuritySettingsInterface.js";
import type { AuditLogRepository } from "../../domain/Interface/AuditLogRepository.js";
import type { FirebaseAuthServiceInterface } from "../../Infra/auth/FirebaseAuthServiceInterface.js";
import type { AuthSessionRepository } from "../../domain/Interface/AuthSessionRepository.js";

const securityRepo: SecuritySettingsRepository = {
  get: vi.fn(),
  createIfMissing: vi.fn(),
  update: vi.fn(),
  persistProviders: vi.fn(),
  markEmailAsVerified: vi.fn(),
  upsertUserSignInProfile: vi.fn(),
};

const auditLogRepo: AuditLogRepository = {
  log: vi.fn(),
};

const authService: FirebaseAuthServiceInterface = {
  getUserByPhone: vi.fn(),
  getUserByEmail: vi.fn(),
  getUserByUid: vi.fn(),
};

const authSessionRepo: AuthSessionRepository = {
  recordSignInSession: vi.fn(),
};

function makeSecurity(overrides: Record<string, unknown> = {}) {
  return {
    allowFederatedLinking: true,
    providerIds: [],
    passcodeEnabled: true,
    passwordEnabled: true,
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
    localPassCodeSetAt: null,
    localPasswordSetAt: null,
    updatedAt: {} as any,
    ...overrides,
  } as any;
}

describe("BeforeSignInPolicyUsecase", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    (authSessionRepo.recordSignInSession as any).mockResolvedValue({
      sid: "sid-test",
      sv: 1,
    });
  });

  it("allows linked federated login and syncs baseline providers without consuming link grant", async () => {
    (securityRepo.createIfMissing as any).mockResolvedValue(
      makeSecurity({
        allowFederatedLinking: false,
        providerIds: [],
      })
    );
    (authService.getUserByUid as any).mockResolvedValue({
      uid: "uid-1",
      phoneNumber: "+14155550001",
      providerIds: ["phone", "google.com"],
    });
    (securityRepo.persistProviders as any).mockResolvedValue(undefined);
    (auditLogRepo.log as any).mockResolvedValue(undefined);

    const usecase = new BeforeSignInPolicyUsecase(
      securityRepo,
      auditLogRepo,
      authService,
      authSessionRepo
    );
    const event = {
      data: {
        uid: "uid-1",
        phoneNumber: null,
        providerData: [{ providerId: "google.com" }],
      },
    } as any;

    await expect(usecase.execute(event)).resolves.toEqual({
      sessionClaims: {
        ts: expect.any(Number),
        sid: expect.any(String),
        sv: 1,
        emailVerified: false,
      },
    });
    expect(authService.getUserByUid).toHaveBeenCalledWith("uid-1");
    expect(securityRepo.persistProviders).toHaveBeenCalledWith(
      "uid-1",
      expect.arrayContaining(["phone", "google.com"]),
      { consumeLinkingGrant: false }
    );
    expect(auditLogRepo.log).not.toHaveBeenCalledWith(
      "uid-1",
      "federated_link_confirmed",
      expect.anything()
    );
  });

  it("denies federated sign-in when both event phone and auth phone are missing", async () => {
    (securityRepo.createIfMissing as any).mockResolvedValue(makeSecurity());
    (authService.getUserByUid as any).mockResolvedValue({
      uid: "uid-2",
      phoneNumber: null,
    });

    const usecase = new BeforeSignInPolicyUsecase(
      securityRepo,
      auditLogRepo,
      authService,
      authSessionRepo
    );
    const event = {
      data: {
        uid: "uid-2",
        phoneNumber: null,
        providerData: [{ providerId: "google.com" }],
      },
    } as any;

    await expect(usecase.execute(event)).rejects.toThrow(
      "Federated login requires verified phone number"
    );
    expect(authService.getUserByUid).toHaveBeenCalledWith("uid-2");
  });

  it("denies new federated provider when linking flag is not enabled and baseline exists", async () => {
    (securityRepo.createIfMissing as any).mockResolvedValue(
      makeSecurity({
        allowFederatedLinking: false,
        providerIds: ["phone"],
      })
    );
    (authService.getUserByUid as any).mockResolvedValue({
      uid: "uid-3",
      phoneNumber: "+14155550003",
      providerIds: ["phone", "google.com"],
    });

    const usecase = new BeforeSignInPolicyUsecase(
      securityRepo,
      auditLogRepo,
      authService,
      authSessionRepo
    );
    const event = {
      data: {
        uid: "uid-3",
        phoneNumber: null,
        providerData: [{ providerId: "google.com" }],
      },
    } as any;

    await expect(usecase.execute(event)).rejects.toThrow(
      "Federated account linking not authorized"
    );
    expect(securityRepo.persistProviders).not.toHaveBeenCalled();
  });

  it("allows new federated provider when linking flag is enabled and consumes grant", async () => {
    (securityRepo.createIfMissing as any).mockResolvedValue(
      makeSecurity({
        allowFederatedLinking: true,
        providerIds: ["phone"],
      })
    );
    (authService.getUserByUid as any).mockResolvedValue({
      uid: "uid-4",
      phoneNumber: "+14155550004",
      providerIds: ["phone", "google.com"],
    });
    (securityRepo.persistProviders as any).mockResolvedValue(undefined);
    (auditLogRepo.log as any).mockResolvedValue(undefined);

    const usecase = new BeforeSignInPolicyUsecase(
      securityRepo,
      auditLogRepo,
      authService,
      authSessionRepo
    );
    const event = {
      data: {
        uid: "uid-4",
        phoneNumber: null,
        providerData: [{ providerId: "google.com" }],
      },
    } as any;

    await expect(usecase.execute(event)).resolves.toEqual({
      sessionClaims: {
        ts: expect.any(Number),
        sid: expect.any(String),
        sv: 1,
        emailVerified: false,
      },
    });
    expect(securityRepo.persistProviders).toHaveBeenCalledWith(
      "uid-4",
      expect.arrayContaining(["phone", "google.com"]),
      { consumeLinkingGrant: true }
    );
    expect(auditLogRepo.log).toHaveBeenCalledWith(
      "uid-4",
      "federated_link_confirmed",
      { providerIds: expect.arrayContaining(["phone", "google.com"]) }
    );
  });

  it("does not block sign-in when session logging fails", async () => {
    (securityRepo.createIfMissing as any).mockResolvedValue(
      makeSecurity({
        allowFederatedLinking: true,
        providerIds: ["phone", "google.com"],
      })
    );
    (authService.getUserByUid as any).mockResolvedValue({
      uid: "uid-5",
      phoneNumber: "+14155550005",
      providerIds: ["phone", "google.com"],
    });
    (authSessionRepo.recordSignInSession as any).mockRejectedValue(new Error("write failed"));

    const usecase = new BeforeSignInPolicyUsecase(
      securityRepo,
      auditLogRepo,
      authService,
      authSessionRepo
    );
    const event = {
      ipAddress: "203.0.113.10",
      userAgent: "agent/1.0",
      data: {
        uid: "uid-5",
        phoneNumber: "+14155550005",
        providerData: [{ providerId: "google.com" }],
      },
    } as any;

    await expect(usecase.execute(event)).resolves.toEqual({
      sessionClaims: {
        ts: expect.any(Number),
        sid: expect.any(String),
        sv: 1,
        emailVerified: false,
      },
    });
    expect(auditLogRepo.log).toHaveBeenCalledWith(
      "uid-5",
      "session_log_failed",
      expect.objectContaining({
        provider: "google.com",
      })
    );
  });
});
