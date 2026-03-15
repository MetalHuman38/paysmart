import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const getSessionStatus = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    addMoneyFlutterwave: {
      getSessionStatus,
    },
  }),
}));

import { getFlutterwaveAddMoneySessionStatusHandler } from "../../handlers/getFlutterwaveAddMoneySessionStatus.js";

type TestReq = {
  headers: {
    authorization?: string;
  };
  params?: Record<string, string>;
};

function createResponseRecorder() {
  return {
    statusCode: 200,
    payload: undefined as unknown,
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

describe("getFlutterwaveAddMoneySessionStatusHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = {
      headers: {},
      params: { sessionId: "flw_session_123" },
    } as TestReq;
    const res = createResponseRecorder();

    await getFlutterwaveAddMoneySessionStatusHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns 200 and the Flutterwave session status payload", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getSessionStatus.mockResolvedValue({
      sessionId: "flw_session_123",
      provider: "flutterwave",
      amountMinor: 250000,
      currency: "NGN",
      status: "pending",
      expiresAtMs: 1710000000000,
      checkoutUrl: "https://checkout.flutterwave.cloud/flw_session_123",
      flutterwaveTransactionId: "flw_virtual_account_123",
      updatedAtMs: 1710000000999,
    });

    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { sessionId: "flw_session_123" },
    } as TestReq;
    const res = createResponseRecorder();

    await getFlutterwaveAddMoneySessionStatusHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(getSessionStatus).toHaveBeenCalledWith("uid-1", "flw_session_123");
    expect(res.payload).toEqual(
      expect.objectContaining({
        sessionId: "flw_session_123",
        provider: "flutterwave",
        status: "pending",
      })
    );
  });

  it("returns 503 when Flutterwave auth is missing during a status refresh", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getSessionStatus.mockRejectedValue(new Error("FLUTTERWAVE_SECRET_KEY is not configured"));

    const req = {
      headers: { authorization: "Bearer token-1" },
      params: { sessionId: "flw_session_123" },
    } as TestReq;
    const res = createResponseRecorder();

    await getFlutterwaveAddMoneySessionStatusHandler(req as any, res as any);

    expect(res.statusCode).toBe(503);
    expect(res.payload).toEqual({
      error: "Payments service is not configured",
      code: "MISSING_FLUTTERWAVE_SECRET_KEY",
    });
  });
});
