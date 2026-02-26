import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const submitCallback = vi.fn();
const createIfMissing = vi.fn();
const update = vi.fn();
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
            submitCallback,
        },
        securitySettings: {
            createIfMissing,
            update,
        },
    }),
}));
import { identityProviderCallbackHandler } from "./identityProviderCallback.js";
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
describe("identityProviderCallbackHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 400 when event is missing", async () => {
        const req = {
            headers: {
                authorization: "Bearer token-1",
            },
            body: {},
        };
        const res = createResponseRecorder();
        await identityProviderCallbackHandler(req, res);
        expect(res.statusCode).toBe(400);
        expect(res.payload).toEqual({ error: "Missing event" });
        expect(verifyIdToken).not.toHaveBeenCalled();
    });
    it("returns 200 and handles provider callback", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        submitCallback.mockResolvedValue({
            sessionId: "provider-1",
            provider: "mock",
            status: "pending_review",
            reason: null,
            updatedAtMs: 123456789,
        });
        createIfMissing.mockResolvedValue(undefined);
        update.mockResolvedValue(undefined);
        const req = {
            headers: {
                authorization: "Bearer token-1",
            },
            body: {
                event: "submitted",
                sessionId: "provider-1",
                providerRef: "provider-ref-1",
            },
        };
        const res = createResponseRecorder();
        await identityProviderCallbackHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(verifyIdToken).toHaveBeenCalledWith("token-1");
        expect(submitCallback).toHaveBeenCalledWith("uid-1", {
            event: "submitted",
            sessionId: "provider-1",
            providerRef: "provider-ref-1",
            rawDeepLink: undefined,
        });
        expect(createIfMissing).toHaveBeenCalledWith("uid-1");
        expect(update).toHaveBeenCalledWith("uid-1", expect.objectContaining({
            hasVerifiedIdentity: false,
            kycStatus: "pending_review",
        }));
    });
});
//# sourceMappingURL=identityProviderCallback.test.js.map