import { describe, expect, it, vi } from "vitest";
import { createGetFxQuoteHandler, QuoteRateProvider } from "./getFxQuote.js";

function createResponseRecorder() {
  return {
    statusCode: 200,
    payload: undefined as unknown,
    headers: {} as Record<string, string>,
    setHeader(name: string, value: string) {
      this.headers[name] = value;
      return this;
    },
    status(code: number) {
      this.statusCode = code;
      return this;
    },
    json(body: unknown) {
      this.payload = body;
      return this;
    },
  };
}

describe("getFxQuoteHandler", () => {
  it("returns quote payload for valid query", async () => {
    const provider: QuoteRateProvider = {
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

    await handler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(res.headers["X-Rate-Source"]).toBe("upstream");
    expect(res.payload).toEqual(
      expect.objectContaining({
        sourceCurrency: "USD",
        targetCurrency: "EUR",
        sourceAmount: 100,
        rate: 0.85,
      })
    );
  });

  it("returns 400 for invalid amount", async () => {
    const provider: QuoteRateProvider = {
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

    await handler(req as any, res as any);
    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "invalid_amount" });
  });
});
