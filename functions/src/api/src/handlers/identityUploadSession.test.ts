import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const createSession = vi.fn();
let consoleErrorSpy: ReturnType<typeof vi.spyOn>;

vi.mock("../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    identityUploads: {
      createSession,
    },
  }),
}));

import { identityUploadSessionHandler } from "./identityUploadSession.js";

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

describe("identityUploadSessionHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => {});
  });

  afterEach(() => {
    consoleErrorSpy.mockRestore();
  });

  it("returns 401 when token is missing", async () => {
    const req = {
      headers: {},
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await identityUploadSessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 and creates upload session", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createSession.mockResolvedValue({
      sessionId: "sess-1",
      uploadUrl: "https://example.test/upload",
      objectPath: "identityUploads/uid-1/sess-1/payload.enc",
      associatedData: "uid:uid-1|session:sess-1|doc:passport",
      attestationNonce: "nonce",
      expiresAtMs: 123456789,
      encryptionKeyBase64: "mocked-session-key",
      encryptionSchema: "aes-256-gcm-v1",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        documentType: "passport",
        payloadSha256: "abc123",
        contentType: "image/jpeg",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await identityUploadSessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(createSession).toHaveBeenCalledWith("uid-1", {
      documentType: "passport",
      payloadSha256: "abc123",
      contentType: "image/jpeg",
    });
    expect(res.payload).toEqual(
      expect.objectContaining({
        sessionId: "sess-1",
        encryptionSchema: "aes-256-gcm-v1",
      })
    );
  });

  it("returns 503 when identity upload service is not configured", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createSession.mockRejectedValue(
      new Error("IDENTITY_UPLOAD_KMS_KEY_NAME is not configured")
    );

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        documentType: "passport",
        payloadSha256: "abc123",
        contentType: "image/jpeg",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await identityUploadSessionHandler(req as any, res as any);

    expect(res.statusCode).toBe(503);
    expect(res.payload).toEqual({
      error: "Identity upload service is not configured",
      code: "IDENTITY_UPLOAD_SERVICE_MISCONFIGURED",
    });
    expect(consoleErrorSpy).toHaveBeenCalled();
  });
});
