import { beforeEach, describe, expect, it, vi } from "vitest";

const beginSignIn = vi.fn();

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    passkeys: {
      beginSignIn,
    },
  }),
}));

import { passkeySignInOptionsHandler } from "../../handlers/passkeySignInOptions.js";

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

describe("passkeySignInOptionsHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns options for unauthenticated passkey sign-in", async () => {
    beginSignIn.mockResolvedValue({ challenge: "challenge-1" });

    const req = { body: {} };
    const res = createResponseRecorder();

    await passkeySignInOptionsHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(beginSignIn).toHaveBeenCalledTimes(1);
    expect(res.payload).toEqual({
      options: {
        challenge: "challenge-1",
      },
    });
  });

  it("returns 503 when passkey is not configured", async () => {
    beginSignIn.mockRejectedValue(new Error("PASSKEY_NOT_CONFIGURED"));

    const req = { body: {} };
    const res = createResponseRecorder();

    await passkeySignInOptionsHandler(req as any, res as any);

    expect(res.statusCode).toBe(503);
    expect(res.payload).toEqual(
      expect.objectContaining({
        code: "PASSKEY_NOT_CONFIGURED",
      })
    );
  });
});
