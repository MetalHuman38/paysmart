import { beforeEach, describe, expect, it, vi } from "vitest";
import { BeforeCreatePolicyUsecase } from "./BeforeCreatePolicyUsecase.js";
const authService = {
    getUserByPhone: vi.fn(),
    getUserByEmail: vi.fn(),
    getUserByUid: vi.fn(),
};
const securityRepo = {
    get: vi.fn(),
    createIfMissing: vi.fn(),
    update: vi.fn(),
    persistProviders: vi.fn(),
    markEmailAsVerified: vi.fn(),
    upsertUserSignInProfile: vi.fn(),
};
describe("BeforeCreatePolicyUsecase", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("denies create when phone and email are both missing", async () => {
        const usecase = new BeforeCreatePolicyUsecase(authService, securityRepo);
        const event = { data: { uid: "u1" } };
        await expect(usecase.execute(event)).rejects.toThrow("You must sign up with a verified phone number first.");
        expect(authService.getUserByEmail).not.toHaveBeenCalled();
    });
    it("allows no-phone create when email maps to existing auth user with phone", async () => {
        const usecase = new BeforeCreatePolicyUsecase(authService, securityRepo);
        authService.getUserByEmail.mockResolvedValue({
            uid: "existing",
            phoneNumber: "+14155551234",
        });
        securityRepo.createIfMissing.mockResolvedValue({});
        const event = { data: { uid: "u2", email: "user@example.com" } };
        await expect(usecase.execute(event)).resolves.toBeUndefined();
        expect(authService.getUserByEmail).toHaveBeenCalledWith("user@example.com");
        expect(securityRepo.createIfMissing).toHaveBeenCalledWith("u2");
    });
    it("denies create when incoming phone already exists in auth", async () => {
        const usecase = new BeforeCreatePolicyUsecase(authService, securityRepo);
        authService.getUserByPhone.mockResolvedValue({ uid: "existing" });
        const event = { data: { uid: "u3", phoneNumber: "+14155550000" } };
        await expect(usecase.execute(event)).rejects.toThrow("Phone number already registered");
    });
    it("denies federated create when incoming payload has no phone and no email", async () => {
        const usecase = new BeforeCreatePolicyUsecase(authService, securityRepo);
        const event = {
            data: {
                uid: "u4",
                phoneNumber: null,
                providerData: [{ providerId: "google.com" }],
            },
        };
        await expect(usecase.execute(event)).rejects.toThrow("You must sign up with a verified phone number first.");
        expect(authService.getUserByPhone).not.toHaveBeenCalled();
        expect(authService.getUserByEmail).not.toHaveBeenCalled();
        expect(authService.getUserByUid).toHaveBeenCalledWith("u4");
    });
    it("denies federated create when email exists but matched auth user has no phone", async () => {
        const usecase = new BeforeCreatePolicyUsecase(authService, securityRepo);
        authService.getUserByEmail.mockResolvedValue({
            uid: "existing-no-phone",
            phoneNumber: null,
        });
        const event = {
            data: {
                uid: "u5",
                phoneNumber: null,
                email: "nopone@example.com",
                providerData: [{ providerId: "google.com" }],
            },
        };
        await expect(usecase.execute(event)).rejects.toThrow("You must sign up with a verified phone number first.");
        expect(authService.getUserByEmail).toHaveBeenCalledWith("nopone@example.com");
        expect(authService.getUserByPhone).not.toHaveBeenCalled();
    });
    it("allows create when event has no phone but auth user-by-uid has phone", async () => {
        const usecase = new BeforeCreatePolicyUsecase(authService, securityRepo);
        authService.getUserByUid.mockResolvedValue({
            uid: "u6",
            phoneNumber: "+14155559999",
        });
        authService.getUserByPhone.mockResolvedValue({ uid: "u6" });
        securityRepo.createIfMissing.mockResolvedValue({});
        const event = { data: { uid: "u6", phoneNumber: null } };
        await expect(usecase.execute(event)).resolves.toBeUndefined();
        expect(authService.getUserByUid).toHaveBeenCalledWith("u6");
        expect(authService.getUserByPhone).toHaveBeenCalledWith("+14155559999");
        expect(securityRepo.createIfMissing).toHaveBeenCalledWith("u6");
    });
});
//# sourceMappingURL=BeforeCreatePolicyUsecase.test.js.map