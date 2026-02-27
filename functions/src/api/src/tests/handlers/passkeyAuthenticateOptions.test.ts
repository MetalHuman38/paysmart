import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const beginAuthentication = vi.fn();

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
      beginAuthentication,
    },
  }),
}));

import { passkeyAuthenticateOptionsHandler } from "../../handlers/passkeyAuthenticateOptions.js";

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

describe("passkeyAuthenticateOptionsHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = { headers: {} } as TestReq;
    const res = createResponseRecorder();

    await passkeyAuthenticateOptionsHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns options when passkey auth can start", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    beginAuthentication.mockResolvedValue({
      challenge: "challenge-1",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyAuthenticateOptionsHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(beginAuthentication).toHaveBeenCalledWith("uid-1");
    expect(res.payload).toEqual({
      options: {
        challenge: "challenge-1",
      },
    });
  });
});
