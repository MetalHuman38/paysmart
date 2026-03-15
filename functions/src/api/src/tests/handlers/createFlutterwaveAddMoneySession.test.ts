import { beforeEach, describe, expect, it, vi } from "vitest";
import { FlutterwaveProviderRequestError } from "../../services/flutterwavePaymentsService.js";

const verifyIdToken = vi.fn();
const createSession = vi.fn();

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
      createSession,
    },
  }),
}));

import { createFlutterwaveAddMoneySessionHandler } from "../../handlers/createFlutterwaveAddMoneySession.js";

type TestReq = {
  headers: {
    authorization?: string;
    "idempotency-key"?: string;
  };
  body?: Record<string, unknown>;
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

describe("createFlutterwaveAddMoneySessionHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {}, body: {} } as TestReq;
    const res = createResponseRecorder();

    await createFlutterwaveAddMoneySessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 and creates a Flutterwave add money session", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createSession.mockResolvedValue({
      sessionId: "flw_session_123",
      provider: "flutterwave",
      amountMinor: 250000,
      currency: "NGN",
      status: "pending",
      expiresAtMs: 1710000000000,
      flutterwaveTransactionId: "flw_session_123",
      publicKey: "FLWPUBK_TEST-123",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
        "idempotency-key": "flw-topup-1",
      },
      body: {
        amountMinor: 250000,
        currency: "NGN",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await createFlutterwaveAddMoneySessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(createSession).toHaveBeenCalledWith("uid-1", {
      amountMinor: 250000,
      currency: "NGN",
      idempotencyKey: "flw-topup-1",
    });
    expect(res.payload).toEqual(
      expect.objectContaining({
        sessionId: "flw_session_123",
        provider: "flutterwave",
        status: "pending",
      })
    );
  });

  it("returns 503 with a specific error code when Flutterwave auth is missing", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createSession.mockRejectedValue(new Error("FLUTTERWAVE_SECRET_KEY is not configured"));

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        amountMinor: 100000,
        currency: "NGN",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await createFlutterwaveAddMoneySessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(503);
    expect(res.payload).toEqual({
      error: "Payments service is not configured",
      code: "MISSING_FLUTTERWAVE_SECRET_KEY",
    });
  });

  it("returns 400 with provider validation details for invalid Flutterwave requests", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createSession.mockRejectedValue(
      new FlutterwaveProviderRequestError(
        "Request is not valid: reference: Reference must be unique",
        400,
        "REQUEST_NOT_VALID",
        ["reference: Reference must be unique"]
      )
    );

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        amountMinor: 100000,
        currency: "NGN",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await createFlutterwaveAddMoneySessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({
      error: "Request is not valid: reference: Reference must be unique",
      code: "REQUEST_NOT_VALID",
      details: ["reference: Reference must be unique"],
    });
  });

  it("returns 409 for unresolved Flutterwave provider conflicts", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createSession.mockRejectedValue(
      new FlutterwaveProviderRequestError(
        "Customer already exists",
        409,
        "10409",
        [],
        "RESOURCE_CONFLICT"
      )
    );

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        amountMinor: 100000,
        currency: "NGN",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await createFlutterwaveAddMoneySessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(409);
    expect(res.payload).toEqual({
      error: "Customer already exists",
      code: "10409",
      details: undefined,
    });
  });
});
