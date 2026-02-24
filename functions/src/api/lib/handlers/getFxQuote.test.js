import { describe, expect, it, vi } from "vitest";
import { createGetFxQuoteHandler } from "./getFxQuote.js";
function createResponseRecorder() {
    return {
        statusCode: 200,
        payload: undefined,
        headers: {},
        setHeader(name, value) {
            this.headers[name] = value;
            return this;
        },
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
describe("getFxQuoteHandler", () => {
    it("returns quote payload for valid query", async () => {
        const provider = {
            getRate: vi.fn().mockResolvedValue({
                rate: 0.85,
                source: "upstream",
            }),
        };
        const handler = createGetFxQuoteHandler(provider);
        const req = {
            query: {
                source: "USD",
                target: "EUR",
                amount: "100",
                method: "wire",
            },
        };
        const res = createResponseRecorder();
        await handler(req, res);
        expect(res.statusCode).toBe(200);
        expect(res.headers["X-Rate-Source"]).toBe("upstream");
        expect(res.payload).toEqual(expect.objectContaining({
            sourceCurrency: "USD",
            targetCurrency: "EUR",
            sourceAmount: 100,
            rate: 0.85,
        }));
    });
    it("returns 400 for invalid amount", async () => {
        const provider = {
            getRate: vi.fn(),
        };
        const handler = createGetFxQuoteHandler(provider);
        const req = {
            query: {
                source: "USD",
                target: "EUR",
                amount: "-1",
            },
        };
        const res = createResponseRecorder();
        await handler(req, res);
        expect(res.statusCode).toBe(400);
        expect(res.payload).toEqual({ error: "invalid_amount" });
    });
});
//# sourceMappingURL=getFxQuote.test.js.map