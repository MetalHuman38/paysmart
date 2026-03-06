import { beforeEach, describe, expect, it, vi } from "vitest";

const createCustomToken = vi.fn();
const completeSignIn = vi.fn();
const authSessionStateGet = vi.fn();
const authSessionStateSet = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      createCustomToken,
    },
    firestore: {
      collection: vi.fn(() => ({
        doc: vi.fn(() => ({
          collection: vi.fn(() => ({
            doc: vi.fn(() => ({
              get: authSessionStateGet,
              set: authSessionStateSet,
            })),
          })),
        })),
      })),
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    passkeys: {
      completeSignIn,
    },
  }),
}));

import { passkeySignInVerifyHandler } from "../../handlers/passkeySignInVerify.js";

type TestReq = {
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

describe("passkeySignInVerifyHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    authSessionStateGet.mockResolvedValue({
      exists: true,
      get(field: string) {
        if (field === "activeSid") return "sid-existing";
        if (field === "sessionVersion") return 2;
        return null;
      },
    });
  });

  it("returns 400 when credential is missing", async () => {
    const req = { body: {} } as TestReq;
    const res = createResponseRecorder();

    await passkeySignInVerifyHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Missing credentialJson" });
  });

  it("verifies sign-in and returns custom token", async () => {
    completeSignIn.mockResolvedValue({
      verified: true,
      uid: "uid-1",
      credentialId: "cred-1",
    });
    createCustomToken.mockResolvedValue("custom-token-1");

    const req = {
      body: {
        credentialJson: "{\"id\":\"cred-1\"}",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeySignInVerifyHandler(req as any, res as any);

    expect(completeSignIn).toHaveBeenCalledWith("{\"id\":\"cred-1\"}");
    expect(createCustomToken).toHaveBeenCalledWith("uid-1", {
      sid: "sid-existing",
      sv: 2,
    });
    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({
      verified: true,
      uid: "uid-1",
      credentialId: "cred-1",
      customToken: "custom-token-1",
    });
  });

  it("returns 401 for unknown credential", async () => {
    completeSignIn.mockRejectedValue(new Error("PASSKEY_CREDENTIAL_NOT_REGISTERED"));
    const req = {
      body: {
        credentialJson: "{\"id\":\"missing-cred\"}",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeySignInVerifyHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "PASSKEY_CREDENTIAL_NOT_REGISTERED" });
  });

  it("bootstraps session claims when authSessionState/current is missing", async () => {
    completeSignIn.mockResolvedValue({
      verified: true,
      uid: "uid-bootstrap",
      credentialId: "cred-bootstrap",
    });
    authSessionStateGet.mockResolvedValue({ exists: false });
    createCustomToken.mockResolvedValue("custom-token-bootstrap");

    const req = {
      body: {
        credentialJson: "{\"id\":\"cred-bootstrap\"}",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeySignInVerifyHandler(req as any, res as any);

    expect(authSessionStateSet).toHaveBeenCalledTimes(1);
    expect(createCustomToken).toHaveBeenCalledWith(
      "uid-bootstrap",
      expect.objectContaining({
        sv: 1,
      })
    );
    const claims = createCustomToken.mock.calls[0]?.[1] as { sid?: string; sv?: number };
    expect(typeof claims?.sid).toBe("string");
    expect((claims?.sid || "").length).toBeGreaterThan(0);
    expect(res.statusCode).toBe(200);
  });
});
