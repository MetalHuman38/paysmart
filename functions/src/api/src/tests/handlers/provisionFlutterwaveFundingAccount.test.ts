import { beforeEach, describe, expect, it, vi } from "vitest";
import { FlutterwaveProviderRequestError } from "../../services/flutterwavePaymentsService.js";

const verifyIdToken = vi.fn();
const provision = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    flutterwaveFundingAccounts: {
      provision,
    },
  }),
}));

import { provisionFlutterwaveFundingAccountHandler } from "../../handlers/provisionFlutterwaveFundingAccount.js";

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

describe("provisionFlutterwaveFundingAccountHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {}, body: {} } as TestReq;
    const res = createResponseRecorder();

    await provisionFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns 201 when it creates a new permanent funding account", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    provision.mockResolvedValue({
      accountId: "van-123",
      provider: "flutterwave",
      currency: "NGN",
      accountNumber: "1234567890",
      bankName: "Wema Bank",
      accountName: "Ada Lovelace",
      reference: "funding-ref-123",
      status: "active",
      providerStatus: "active",
      customerId: "customer-123",
      createdAtMs: 1710000000000,
      updatedAtMs: 1710000005000,
      provisioningResult: "created",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
        "idempotency-key": "funding-provision-1",
      },
      body: {
        kyc: {
          bvn: "12345678901",
          nin: "10987654321",
        },
      },
    } as TestReq;
    const res = createResponseRecorder();

    await provisionFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(201);
    expect(provision).toHaveBeenCalledWith("uid-1", {
      idempotencyKey: "funding-provision-1",
      kyc: {
        bvn: "12345678901",
        nin: "10987654321",
      },
    });
    expect(res.payload).toEqual(
      expect.objectContaining({
        accountId: "van-123",
        provisioningResult: "created",
      })
    );
  });

  it("returns 422 when BVN or NIN is unavailable", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    provision.mockRejectedValue(new Error("FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED"));

    const req = {
      headers: { authorization: "Bearer token-1" },
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await provisionFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(422);
    expect(res.payload).toEqual({
      error: "Verified BVN or NIN is required before provisioning a permanent NGN funding account",
      code: "FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED",
    });
  });

  it("returns 409 when Flutterwave reports a funding-account conflict", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    provision.mockRejectedValue(
      new FlutterwaveProviderRequestError(
        "Customer already exists",
        409,
        "10409",
        [],
        "RESOURCE_CONFLICT"
      )
    );

    const req = {
      headers: { authorization: "Bearer token-1" },
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await provisionFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(409);
    expect(res.payload).toEqual({
      error: "Flutterwave funding account conflict",
      code: "FLUTTERWAVE_FUNDING_ACCOUNT_CONFLICT",
      details: [],
    });
  });
});
