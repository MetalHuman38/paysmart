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

import { confirmPhoneChangedHandler } from "../../handlers/confirmPhoneChanged.js";

type TestReq = {
  headers: {
    authorization?: string;
  };
  body?: {
    phoneNumber?: string;
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

describe("confirmPhoneChangedHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 200 and confirms phone change when token and payload are valid", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getUser.mockResolvedValue({
      uid: "uid-1",
      email: "user@example.com",
      phoneNumber: "+447988777954",
      isAnonymous: false,
      providerIds: ["phone"],
      tenantId: null,
      photoURL: null,
      displayName: "Test User",
    });
    createIfMissing.mockResolvedValue(undefined);
    update.mockResolvedValue(undefined);
    upsertVerifiedPhoneSignup.mockResolvedValue(undefined);
    logAuditEvent.mockResolvedValue(undefined);

    const req = {
      headers: {
        authorization: "Bearer test-token",
      },
      body: {
        phoneNumber: "+447988777954",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await confirmPhoneChangedHandler(req as any, res as any);

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
        event: "phone_changed_confirmed",
        phoneNumber: "+447988777954",
      })
    );
  });

  it("returns 409 when verified phone number does not match requested phone number", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    getUser.mockResolvedValue({
      uid: "uid-1",
      phoneNumber: "+447988777955",
      providerIds: ["phone"],
    });

    const req = {
      headers: {
        authorization: "Bearer test-token",
      },
      body: {
        phoneNumber: "+447988777954",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await confirmPhoneChangedHandler(req as any, res as any);

    expect(res.statusCode).toBe(409);
    expect(res.payload).toEqual({
      error: "Verified phone number does not match requested phone number",
    });
    expect(upsertVerifiedPhoneSignup).not.toHaveBeenCalled();
  });
});
