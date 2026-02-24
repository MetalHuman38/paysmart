import { beforeEach, describe, expect, it, vi } from "vitest";
const applyWebhook = vi.fn();
vi.mock("../infrastructure/di/authContainer.js", () => ({
    authContainer: () => ({
        addMoney: {
            applyWebhook,
        },
    }),
}));
import { stripeWebhookHandler } from "./stripeWebhook.js";
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
describe("stripeWebhookHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 400 when payload is missing", async () => {
        const req = { headers: {}, body: null };
        const res = createResponseRecorder();
        await stripeWebhookHandler(req, res);
        expect(res.statusCode).toBe(400);
        expect(res.payload).toEqual({ error: "Missing payload" });
    });
    it("returns 200 when webhook is handled", async () => {
        applyWebhook.mockResolvedValue({
            handled: true,
            sessionId: "cs_test_1",
            uid: "uid-1",
            status: "succeeded",
        });
        const req = {
            headers: { "stripe-signature": "t=1,v1=abc" },
            body: Buffer.from(JSON.stringify({ id: "evt_1", type: "checkout.session.completed" })),
        };
        const res = createResponseRecorder();
        await stripeWebhookHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(applyWebhook).toHaveBeenCalledWith(JSON.stringify({ id: "evt_1", type: "checkout.session.completed" }), "t=1,v1=abc");
        expect(res.payload).toEqual(expect.objectContaining({
            ok: true,
            handled: true,
            sessionId: "cs_test_1",
        }));
    });
});
//# sourceMappingURL=stripeWebhook.test.js.map