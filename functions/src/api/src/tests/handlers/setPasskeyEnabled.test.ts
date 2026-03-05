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

import { setPasskeyEnabledHandler } from "../../handlers/setPasskeyEnabled.js";

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

describe("setPasskeyEnabledHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when authorization token is missing", async () => {
    const req = { headers: {}, body: {} } as TestReq;
    const res = createResponseRecorder();

    await setPasskeyEnabledHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 400 when payload is invalid", async () => {
    const req = {
      headers: { authorization: "Bearer token-1" },
      body: { passkeyEnabled: "yes" },
    } as unknown as TestReq;
    const res = createResponseRecorder();

    await setPasskeyEnabledHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "passkeyEnabled must be a boolean" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 200 and persists passkey state", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    createIfMissing.mockResolvedValue(undefined);
    update.mockResolvedValue(undefined);

    const req = {
      headers: { authorization: "Bearer token-1" },
      body: { passkeyEnabled: false },
    } as TestReq;
    const res = createResponseRecorder();

    await setPasskeyEnabledHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({
      ok: true,
      passkeyEnabled: false,
      hasSkippedPasskeyEnrollmentPrompt: true,
    });
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(update).toHaveBeenCalledWith(
      "uid-1",
      expect.objectContaining({
        passkeyEnabled: false,
        hasSkippedPasskeyEnrollmentPrompt: true,
      })
    );
  });
});
