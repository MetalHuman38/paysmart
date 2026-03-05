import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
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
    securitySettings: {
      createIfMissing,
      update,
    },
  }),
}));

import { setMfaEnrollmentPromptStateHandler } from "../../handlers/setMfaEnrollmentPromptState.js";

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

describe("setMfaEnrollmentPromptStateHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when authorization token is missing", async () => {
    const req = { headers: {}, body: {} } as TestReq;
    const res = createResponseRecorder();

    await setMfaEnrollmentPromptStateHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
    expect(createIfMissing).not.toHaveBeenCalled();
    expect(update).not.toHaveBeenCalled();
  });

  it("returns 400 when payload is invalid", async () => {
    const req = {
      headers: {
        authorization: "Bearer test-token",
      },
      body: {
        hasSkippedMfaEnrollmentPrompt: "yes",
      },
    } as unknown as TestReq;
    const res = createResponseRecorder();

    await setMfaEnrollmentPromptStateHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({
      error: "hasSkippedMfaEnrollmentPrompt must be a boolean",
    });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 and persists skip prompt state", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createIfMissing.mockResolvedValue(undefined);
    update.mockResolvedValue(undefined);

    const req = {
      headers: {
        authorization: "Bearer test-token",
      },
      body: {
        hasSkippedMfaEnrollmentPrompt: true,
      },
    } as TestReq;
    const res = createResponseRecorder();

    await setMfaEnrollmentPromptStateHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({
      ok: true,
      hasSkippedMfaEnrollmentPrompt: true,
    });
    expect(verifyIdToken).toHaveBeenCalledWith("test-token");
    expect(createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        hasSkippedMfaEnrollmentPrompt: true,
      })
    );
  });
});
