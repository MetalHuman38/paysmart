import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const createSession = vi.fn();
vi.mock("../dependencies.js", () => ({
    initDeps: () => ({
        auth: {
            verifyIdToken,
        },
    }),
}));
vi.mock("../infrastructure/di/authContainer.js", () => ({
    authContainer: () => ({
        addMoney: {
            createSession,
        },
    }),
}));
import { createAddMoneySessionHandler } from "./createAddMoneySession.js";
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
describe("createAddMoneySessionHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 401 when token is missing", async () => {
        const req = { headers: {}, body: {} };
        const res = createResponseRecorder();
        await createAddMoneySessionHandler(req, res);
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Missing token" });
        expect(verifyIdToken).not.toHaveBeenCalled();
    });
    it("returns 200 and creates add money session", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        createSession.mockResolvedValue({
            sessionId: "pi_test_123",
            amountMinor: 1500,
            currency: "GBP",
            status: "pending",
            expiresAtMs: 1710000000000,
            paymentIntentId: "pi_test_123",
            paymentIntentClientSecret: "pi_test_123_secret_456",
            publishableKey: "pk_test_123",
        });
        const req = {
            headers: {
                authorization: "Bearer token-1",
                "idempotency-key": "topup-1",
            },
            body: {
                amountMinor: 1500,
                currency: "GBP",
            },
        };
        const res = createResponseRecorder();
        await createAddMoneySessionHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(verifyIdToken).toHaveBeenCalledWith("token-1");
        expect(createSession).toHaveBeenCalledWith("uid-1", {
            amountMinor: 1500,
            currency: "GBP",
            idempotencyKey: "topup-1",
        });
        expect(res.payload).toEqual(expect.objectContaining({
            sessionId: "pi_test_123",
            status: "pending",
        }));
    });
});
//# sourceMappingURL=createAddMoneySession.test.js.map