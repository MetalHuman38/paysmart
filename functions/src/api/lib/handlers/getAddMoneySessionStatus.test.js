import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const getSessionStatus = vi.fn();
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
            getSessionStatus,
        },
    }),
}));
import { getAddMoneySessionStatusHandler } from "./getAddMoneySessionStatus.js";
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
describe("getAddMoneySessionStatusHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 401 when token is missing", async () => {
        const req = { headers: {}, params: { sessionId: "cs_test_1" } };
        const res = createResponseRecorder();
        await getAddMoneySessionStatusHandler(req, res);
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Missing token" });
    });
    it("returns 200 and session status payload", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        getSessionStatus.mockResolvedValue({
            sessionId: "cs_test_1",
            checkoutUrl: "https://checkout.stripe.com/pay/cs_test_1",
            amountMinor: 1500,
            currency: "GBP",
            status: "succeeded",
            expiresAtMs: 1710000000000,
            paymentIntentId: "pi_123",
            updatedAtMs: 1710000000999,
        });
        const req = {
            headers: { authorization: "Bearer token-1" },
            params: { sessionId: "cs_test_1" },
        };
        const res = createResponseRecorder();
        await getAddMoneySessionStatusHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(verifyIdToken).toHaveBeenCalledWith("token-1");
        expect(getSessionStatus).toHaveBeenCalledWith("uid-1", "cs_test_1");
        expect(res.payload).toEqual(expect.objectContaining({
            sessionId: "cs_test_1",
            status: "succeeded",
        }));
    });
});
//# sourceMappingURL=getAddMoneySessionStatus.test.js.map