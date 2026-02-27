import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const completeAuthentication = vi.fn();

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
      completeAuthentication,
    },
  }),
}));

import { passkeyAuthenticateVerifyHandler } from "../../handlers/passkeyAuthenticateVerify.js";

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

describe("passkeyAuthenticateVerifyHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 400 when credential is missing", async () => {
    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyAuthenticateVerifyHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Missing credentialJson" });
  });

  it("verifies authentication and returns success", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    completeAuthentication.mockResolvedValue({
      verified: true,
      credentialId: "cred-1",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        credentialJson: "{\"id\":\"cred-1\"}",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyAuthenticateVerifyHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(completeAuthentication).toHaveBeenCalledWith("uid-1", "{\"id\":\"cred-1\"}");
    expect(res.payload).toEqual({
      verified: true,
      credentialId: "cred-1",
    });
  });
});
