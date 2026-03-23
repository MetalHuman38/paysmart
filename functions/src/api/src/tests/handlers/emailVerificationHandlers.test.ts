import { beforeEach, describe, expect, it, vi } from "vitest";

const getUIDFromAuthHeader = {
  execute: vi.fn(),
};
const generateEmailVerification = {
  execute: vi.fn(),
};

vi.mock("../../infrastructure/di/apiContainer.js", () => ({
  apiContainer: () => ({
    getUIDFromAuthHeader,
    generateEmailVerification,
  }),
}));

import { generateEmailVerificationHandler } from "../../handlers/emailVerificationHandlers.js";

type TestReq = {
  headers: {
    authorization?: string;
  };
  body?: {
    email?: string;
    returnRoute?: string;
  };
};

function createResponseRecorder() {
  return {
    headers: {} as Record<string, string>,
    statusCode: 200,
    payload: undefined as unknown,
    setHeader(name: string, value: string) {
      this.headers[name] = value;
      return this;
    },
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

describe("generateEmailVerificationHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    getUIDFromAuthHeader.execute.mockResolvedValue("uid-1");
  });

  it("returns a structured cooldown response with Retry-After", async () => {
    generateEmailVerification.execute.mockResolvedValue({
      sent: false,
      reason: "cooldown",
      retryAfter: 42,
    });

    const req = {
      headers: { authorization: "Bearer test-token" },
      body: { email: "tester@example.com" },
    } as TestReq;
    const res = createResponseRecorder();

    await generateEmailVerificationHandler(req as any, res as any);

    expect(res.statusCode).toBe(429);
    expect(res.headers["Retry-After"]).toBe("42");
    expect(res.payload).toEqual({
      error: "Email verification cooldown active",
      code: "email_verification_cooldown",
      retryAfter: 42,
      sent: false,
    });
  });

  it("returns a daily limit response instead of masquerading as success", async () => {
    generateEmailVerification.execute.mockResolvedValue({
      sent: false,
      reason: "daily_limit",
      retryAfter: 3600,
    });

    const req = {
      headers: { authorization: "Bearer test-token" },
      body: { email: "tester@example.com" },
    } as TestReq;
    const res = createResponseRecorder();

    await generateEmailVerificationHandler(req as any, res as any);

    expect(res.statusCode).toBe(429);
    expect(res.headers["Retry-After"]).toBe("3600");
    expect(res.payload).toEqual({
      error: "Daily email verification limit reached",
      code: "email_verification_daily_limit",
      retryAfter: 3600,
      sent: false,
    });
  });

  it("returns a conflict when the email is already verified", async () => {
    generateEmailVerification.execute.mockResolvedValue({
      sent: false,
      reason: "already_verified",
    });

    const req = {
      headers: { authorization: "Bearer test-token" },
      body: { email: "tester@example.com" },
    } as TestReq;
    const res = createResponseRecorder();

    await generateEmailVerificationHandler(req as any, res as any);

    expect(res.statusCode).toBe(409);
    expect(res.payload).toEqual({
      error: "Email is already verified",
      code: "email_already_verified",
      sent: false,
    });
  });

  it("passes the optional return route to the use case", async () => {
    generateEmailVerification.execute.mockResolvedValue({
      sent: true,
    });

    const req = {
      headers: { authorization: "Bearer test-token" },
      body: {
        email: "tester@example.com",
        returnRoute: "profile/mfa_nudge",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await generateEmailVerificationHandler(req as any, res as any);

    expect(generateEmailVerification.execute).toHaveBeenCalledWith({
      uid: "uid-1",
      email: "tester@example.com",
      returnRoute: "profile/mfa_nudge",
    });
    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({ sent: true });
  });
});
