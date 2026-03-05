import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const completeRegistration = vi.fn();
const createIfMissing = vi.fn();
const update = vi.fn();

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
      completeRegistration,
    },
    securitySettings: {
      createIfMissing,
      update,
    },
  }),
}));

import { passkeyRegisterVerifyHandler } from "../../handlers/passkeyRegisterVerify.js";

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

describe("passkeyRegisterVerifyHandler", () => {
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

    await passkeyRegisterVerifyHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Missing credentialJson" });
  });

  it("returns 200 when registration verification succeeds", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    completeRegistration.mockResolvedValue({
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

    await passkeyRegisterVerifyHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(completeRegistration).toHaveBeenCalledWith("uid-1", "{\"id\":\"cred-1\"}");
    expect(createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        passkeyEnabled: true,
        hasSkippedPasskeyEnrollmentPrompt: false,
      })
    );
    expect(res.payload).toEqual({
      verified: true,
      credentialId: "cred-1",
    });
  });
});
