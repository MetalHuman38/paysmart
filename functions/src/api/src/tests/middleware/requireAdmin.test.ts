import { beforeEach, describe, expect, it, vi } from "vitest";
import { createRequireAdmin } from "../../middleware/requireAdmin.js";

function createResponseRecorder() {
  return {
    statusCode: 200,
    payload: undefined as unknown,
    locals: {} as Record<string, unknown>,
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

describe("requireAdmin", () => {
  const verifyIdToken = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when bearer token is missing", async () => {
    const middleware = createRequireAdmin({
      verifyIdToken,
      allowlistedEmails: new Set<string>(),
    });
    const req = { headers: {} };
    const res = createResponseRecorder();
    const next = vi.fn();

    await middleware(req as any, res as any, next);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(next).not.toHaveBeenCalled();
  });

  it("returns 403 when token is valid but user is not admin", async () => {
    verifyIdToken.mockResolvedValue({
      uid: "user_1",
      email: "dev@paysmart.app",
      admin: false,
    });
    const middleware = createRequireAdmin({
      verifyIdToken,
      allowlistedEmails: new Set(["ops@paysmart.app"]),
    });
    const req = { headers: { authorization: "Bearer token_1" } };
    const res = createResponseRecorder();
    const next = vi.fn();

    await middleware(req as any, res as any, next);

    expect(res.statusCode).toBe(403);
    expect(res.payload).toEqual({ error: "Admin access required" });
    expect(next).not.toHaveBeenCalled();
  });

  it("calls next when token has admin claim", async () => {
    verifyIdToken.mockResolvedValue({
      uid: "admin_1",
      email: "ops@paysmart.app",
      admin: true,
    });
    const middleware = createRequireAdmin({
      verifyIdToken,
      allowlistedEmails: new Set<string>(),
    });
    const req = { headers: { authorization: "Bearer token_1" } };
    const res = createResponseRecorder();
    const next = vi.fn();

    await middleware(req as any, res as any, next);

    expect(next).toHaveBeenCalledOnce();
    expect(res.locals.admin).toEqual(
      expect.objectContaining({
        uid: "admin_1",
        email: "ops@paysmart.app",
        isAdminClaim: true,
      })
    );
  });

  it("calls next when email is allowlisted", async () => {
    verifyIdToken.mockResolvedValue({
      uid: "ops_1",
      email: "ops@paysmart.app",
      admin: false,
    });
    const middleware = createRequireAdmin({
      verifyIdToken,
      allowlistedEmails: new Set(["ops@paysmart.app"]),
    });
    const req = { headers: { authorization: "Bearer token_1" } };
    const res = createResponseRecorder();
    const next = vi.fn();

    await middleware(req as any, res as any, next);

    expect(next).toHaveBeenCalledOnce();
    expect(res.locals.admin).toEqual(
      expect.objectContaining({
        uid: "ops_1",
        email: "ops@paysmart.app",
        isAllowlistedEmail: true,
      })
    );
  });
});
