import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const resumeSession = vi.fn();
vi.mock("../dependencies.js", () => ({
    initDeps: () => ({
        auth: {
            verifyIdToken,
        },
    }),
}));
vi.mock("../infrastructure/di/authContainer.js", () => ({
    authContainer: () => ({
        identityProvider: {
            resumeSession,
        },
    }),
}));
import { identityProviderResumeHandler } from "./identityProviderResume.js";
function createResponseRecorder() {
    return {
        statusCode: 200,
        payload: undefined,
        status(code) {
            this.statusCode = code;
            return this;
        },
        json(body) {
            this.payload = body;
            return this;
        },
    };
}
describe("identityProviderResumeHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 400 when sessionId is missing", async () => {
        const req = {
            headers: { authorization: "Bearer token-1" },
            body: {},
        };
        const res = createResponseRecorder();
        await identityProviderResumeHandler(req, res);
        expect(res.statusCode).toBe(400);
        expect(res.payload).toEqual({ error: "Missing sessionId" });
        expect(verifyIdToken).not.toHaveBeenCalled();
    });
    it("returns 200 and resumes provider session", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        resumeSession.mockResolvedValue({
            sessionId: "provider-1",
            provider: "mock",
            status: "in_progress",
            updatedAtMs: 123456789,
        });
        const req = {
            headers: {
                authorization: "Bearer token-1",
            },
            body: {
                sessionId: "provider-1",
            },
        };
        const res = createResponseRecorder();
        await identityProviderResumeHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(verifyIdToken).toHaveBeenCalledWith("token-1");
        expect(resumeSession).toHaveBeenCalledWith("uid-1", {
            sessionId: "provider-1",
        });
    });
});
//# sourceMappingURL=identityProviderResume.test.js.map