import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const startSession = vi.fn();
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
            startSession,
        },
    }),
}));
import { identityProviderStartSessionHandler } from "./identityProviderStartSession.js";
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
describe("identityProviderStartSessionHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 401 when token is missing", async () => {
        const req = {
            headers: {},
            body: {},
        };
        const res = createResponseRecorder();
        await identityProviderStartSessionHandler(req, res);
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Missing token" });
    });
    it("returns 200 and starts a provider session", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        startSession.mockResolvedValue({
            sessionId: "provider-1",
            provider: "mock",
            status: "session_created",
            launchUrl: "https://provider.test/session",
            expiresAtMs: 123456789,
        });
        const req = {
            headers: {
                authorization: "Bearer token-1",
            },
            body: {
                countryIso2: "GB",
                documentType: "passport",
            },
        };
        const res = createResponseRecorder();
        await identityProviderStartSessionHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(verifyIdToken).toHaveBeenCalledWith("token-1");
        expect(startSession).toHaveBeenCalledWith("uid-1", {
            countryIso2: "GB",
            documentType: "passport",
        });
        expect(res.payload).toEqual(expect.objectContaining({
            sessionId: "provider-1",
            status: "session_created",
        }));
    });
});
//# sourceMappingURL=identityProviderStartSession.test.js.map