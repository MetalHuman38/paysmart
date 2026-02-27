import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const beginRegistration = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    passkeys: {
      beginRegistration,
    },
  }),
}));

import { passkeyRegisterOptionsHandler } from "../../handlers/passkeyRegisterOptions.js";

type TestReq = {
  headers: {
    authorization?: string;
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

describe("passkeyRegisterOptionsHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = {
      headers: {},
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyRegisterOptionsHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns registration options", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    beginRegistration.mockResolvedValue({
      challenge: "challenge-1",
      rp: { id: "pay-smart.net", name: "PaySmart" },
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        userName: "user@example.com",
        userDisplayName: "User Example",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyRegisterOptionsHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(beginRegistration).toHaveBeenCalledWith("uid-1", {
      userName: "user@example.com",
      userDisplayName: "User Example",
    });
    expect(res.payload).toEqual(
      expect.objectContaining({
        options: expect.objectContaining({
          challenge: "challenge-1",
        }),
      })
    );
  });
});
