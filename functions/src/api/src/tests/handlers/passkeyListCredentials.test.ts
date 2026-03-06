import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const listCredentials = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    passkeys: {
      listCredentials,
    },
  }),
}));

import { passkeyListCredentialsHandler } from "../../handlers/passkeyListCredentials.js";

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

describe("passkeyListCredentialsHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = {
      headers: {},
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyListCredentialsHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
  });

  it("returns credentials for authenticated user", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    listCredentials.mockResolvedValue({
      credentials: [
        {
          credentialId: "cred-1",
          deviceType: "multiDevice",
          backedUp: true,
          transports: ["internal"],
          createdAtMs: 111,
          updatedAtMs: 222,
        },
      ],
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await passkeyListCredentialsHandler(req as any, res as any);

    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(listCredentials).toHaveBeenCalledWith("uid-1");
    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual({
      credentials: [
        {
          credentialId: "cred-1",
          deviceType: "multiDevice",
          backedUp: true,
          transports: ["internal"],
          createdAtMs: 111,
          updatedAtMs: 222,
        },
      ],
    });
  });
});
