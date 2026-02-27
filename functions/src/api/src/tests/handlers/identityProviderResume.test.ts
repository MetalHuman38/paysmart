import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const resumeSession = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    identityProvider: {
      resumeSession,
    },
  }),
}));

import { identityProviderResumeHandler } from "../../handlers/identityProviderResume.js";

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

describe("identityProviderResumeHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 400 when sessionId is missing", async () => {
    const req = {
      headers: { authorization: "Bearer token-1" },
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await identityProviderResumeHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Missing sessionId" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 and resumes provider session", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    resumeSession.mockResolvedValue({
      sessionId: "provider-1",
      provider: "mock",
      status: "in_progress",
      updatedAtMs: 123456789,
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        sessionId: "provider-1",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await identityProviderResumeHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(resumeSession).toHaveBeenCalledWith("uid-1", {
      sessionId: "provider-1",
    });
  });
});
