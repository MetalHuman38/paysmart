import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const revokeCredential = vi.fn();

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
      revokeCredential,
    },
  }),
}));

import { passkeyRevokeCredentialHandler } from "../../handlers/passkeyRevokeCredential.js";

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

describe("passkeyRevokeCredentialHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 400 when credentialId is missing", async () => {
    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyRevokeCredentialHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "credentialId is required" });
  });

  it("revokes credential and returns success", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    revokeCredential.mockResolvedValue({
      revoked: true,
      credentialId: "cred-1",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        credentialId: "cred-1",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyRevokeCredentialHandler(req as any, res as any);

    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(revokeCredential).toHaveBeenCalledWith("uid-1", "cred-1");
    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({
      revoked: true,
      credentialId: "cred-1",
    });
  });
});
