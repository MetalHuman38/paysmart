import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const getCurrent = vi.fn();

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
      getCurrent,
    },
  }),
}));

import { getFlutterwaveFundingAccountHandler } from "../../handlers/getFlutterwaveFundingAccount.js";

type TestReq = {
  headers: {
    authorization?: string;
  };
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

describe("getFlutterwaveFundingAccountHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {} } as TestReq;
    const res = createResponseRecorder();

    await getFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns 404 when the funding account has not been provisioned", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getCurrent.mockResolvedValue(null);

    const req = {
      headers: { authorization: "Bearer token-1" },
    } as TestReq;
    const res = createResponseRecorder();

    await getFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(404);
    expect(res.payload).toEqual({
      error: "Funding account not found",
      code: "FLUTTERWAVE_FUNDING_ACCOUNT_NOT_FOUND",
    });
  });

  it("returns 200 and the persisted funding account", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getCurrent.mockResolvedValue({
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
    });

    const req = {
      headers: { authorization: "Bearer token-1" },
    } as TestReq;
    const res = createResponseRecorder();

    await getFlutterwaveFundingAccountHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(getCurrent).toHaveBeenCalledWith("uid-1");
    expect(res.payload).toEqual(
      expect.objectContaining({
        accountId: "van-123",
        provider: "flutterwave",
        status: "active",
      })
    );
  });
});
