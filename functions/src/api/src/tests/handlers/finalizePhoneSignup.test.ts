import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const getUser = vi.fn();
const createIfMissing = vi.fn();
const update = vi.fn();
const upsertVerifiedPhoneSignup = vi.fn();
const logAuditEvent = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    authService: {
      getUser,
    },
    securitySettings: {
      createIfMissing,
      update,
    },
    userRepo: {
      upsertVerifiedPhoneSignup,
      logAuditEvent,
    },
  }),
}));

import { finalizePhoneSignupHandler } from "../../handlers/finalizePhoneSignup.js";

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

describe("finalizePhoneSignupHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when authorization token is missing", async () => {
    const req = { headers: {} } as TestReq;
    const res = createResponseRecorder();

    await finalizePhoneSignupHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns 400 when verified phone number is unavailable", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getUser.mockResolvedValue({
      uid: "uid-1",
      providerIds: ["phone"],
    });

    const req = {
      headers: { authorization: "Bearer test-token" },
    } as TestReq;
    const res = createResponseRecorder();

    await finalizePhoneSignupHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Verified phone number is unavailable" });
    expect(upsertVerifiedPhoneSignup).not.toHaveBeenCalled();
  });

  it("returns 200 and finalizes the verified phone signup", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getUser.mockResolvedValue({
      uid: "uid-1",
      email: "user@example.com",
      phoneNumber: "+447988777954",
      isAnonymous: false,
      providerIds: ["phone"],
      tenantId: null,
      photoURL: "https://example.com/avatar.png",
      displayName: "Test User",
    });
    createIfMissing.mockResolvedValue(undefined);
    update.mockResolvedValue(undefined);
    upsertVerifiedPhoneSignup.mockResolvedValue(undefined);
    logAuditEvent.mockResolvedValue(undefined);

    const req = {
      headers: { authorization: "Bearer test-token" },
    } as TestReq;
    const res = createResponseRecorder();

    await finalizePhoneSignupHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({ ok: true });
    expect(verifyIdToken).toHaveBeenCalledWith("test-token");
    expect(getUser).toHaveBeenCalledWith("uid-1");
    expect(createIfMissing).toHaveBeenCalledWith("uid-1");
    expect(upsertVerifiedPhoneSignup).toHaveBeenCalledWith(
      expect.objectContaining({
        uid: "uid-1",
        phoneNumber: "+447988777954",
        providerIds: ["phone"],
      })
    );
    expect(logAuditEvent).toHaveBeenCalledWith(
      expect.objectContaining({
        uid: "uid-1",
        event: "phone_signup_finalized",
        phoneNumber: "+447988777954",
      })
    );
  });
});
