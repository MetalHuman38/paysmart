import { describe, expect, it, vi } from "vitest";
import { EnsureSecuritySettings } from "./EnsureSecuritySettings.js";
import { SeedSecuritySettingsOnUserCreate } from "./SeedSecuritySettings.js";
const repo = {
    get: vi.fn(),
    createIfMissing: vi.fn(),
    update: vi.fn(),
};
describe("Security settings use cases", () => {
    it("EnsureSecuritySettings delegates createIfMissing", async () => {
        const usecase = new EnsureSecuritySettings(repo);
        await usecase.execute("uid-1");
        expect(repo.createIfMissing).toHaveBeenCalledWith("uid-1");
    });
    it("SeedSecuritySettingsOnUserCreate validates uid", async () => {
        const usecase = new SeedSecuritySettingsOnUserCreate(repo);
        await expect(usecase.execute("")).rejects.toThrow("UID is required");
    });
});
//# sourceMappingURL=SecuritySettingsUsecases.test.js.map