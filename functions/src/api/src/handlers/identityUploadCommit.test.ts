import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const commitSession = vi.fn();
const createIfMissing = vi.fn();
const update = vi.fn();

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
      commitSession,
    },
    securitySettings: {
      createIfMissing,
      update,
    },
  }),
}));

import { identityUploadCommitHandler } from "./identityUploadCommit.js";

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

describe("identityUploadCommitHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = {
      headers: {},
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await identityUploadCommitHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns 200 and commits upload", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    commitSession.mockResolvedValue({
      verificationId: "sess-1",
      status: "pending_review",
    });
    createIfMissing.mockResolvedValue(undefined);
    update.mockResolvedValue(undefined);

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        sessionId: "sess-1",
        payloadSha256: "abc123",
        attestationJwt: "attest-jwt",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await identityUploadCommitHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(commitSession).toHaveBeenCalledWith("uid-1", {
      sessionId: "sess-1",
      payloadSha256: "abc123",
      attestationJwt: "attest-jwt",
    });
    expect(createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        hasVerifiedIdentity: false,
        kycStatus: "pending_review",
      })
    );
    expect(res.payload).toEqual({
      verificationId: "sess-1",
      status: "pending_review",
    });
  });
});
