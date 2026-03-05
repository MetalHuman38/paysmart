import { beforeEach, describe, expect, it, vi } from "vitest";

const getSessionState = vi.fn();
const getSecuritySettings = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    firestore: {
      collection: (name: string) => {
        if (name !== "users") {
          throw new Error(`Unexpected collection ${name}`);
        }
        return {
          doc: () => ({
            collection: (subName: string) => {
              if (subName === "authSessionState") {
                return {
                  doc: () => ({
                    get: getSessionState,
                  }),
                  orderBy: () => ({
                    limit: () => ({
                      get: async () => ({ docs: [] }),
                    }),
                  }),
                  limit: () => ({
                    get: async () => ({ docs: [] }),
                  }),
                };
              }
              if (subName === "security") {
                return {
                  doc: () => ({
                    get: getSecuritySettings,
                  }),
                };
              }
              if (subName === "identityUploads" || subName === "identityProviderSessions") {
                return {
                  orderBy: () => ({
                    limit: () => ({
                      get: async () => ({ docs: [] }),
                    }),
                  }),
                  limit: () => ({
                    get: async () => ({ docs: [] }),
                  }),
                };
              }
              if (subName === "payments") {
                return {
                  doc: () => ({
                    collection: () => ({
                      orderBy: () => ({
                        limit: () => ({
                          get: async () => ({ docs: [] }),
                        }),
                      }),
                      limit: () => ({
                        get: async () => ({ docs: [] }),
                      }),
                    }),
                  }),
                };
              }
              if (subName === "walletTransactions") {
                return {
                  orderBy: () => ({
                    limit: () => ({
                      get: async () => ({ docs: [] }),
                    }),
                  }),
                  limit: () => ({
                    get: async () => ({ docs: [] }),
                  }),
                };
              }
              throw new Error(`Unexpected sub-collection ${subName}`);
            },
          }),
        };
      },
    },
  }),
}));

import { adminUserSessionSnapshotHandler } from "../../handlers/adminMonitoring.js";

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

describe("adminUserSessionSnapshotHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 400 when uid is missing", async () => {
    const req = { params: {} };
    const res = createResponseRecorder();

    await adminUserSessionSnapshotHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Missing uid" });
  });

  it("returns merged session and security snapshot", async () => {
    getSessionState.mockResolvedValue({
      exists: true,
      data: () => ({
        activeSid: "sid_123",
        sessionVersion: 5,
        lastIssuedAtMs: 1710000000000,
      }),
    });
    getSecuritySettings.mockResolvedValue({
      exists: true,
      data: () => ({
        sessionLocked: true,
        killSwitchActive: false,
        lockAfterMinutes: 5,
        biometricsEnabled: true,
        passcodeEnabled: false,
        passwordEnabled: true,
      }),
    });

    const req = { params: { uid: "uid_1" } };
    const res = createResponseRecorder();

    await adminUserSessionSnapshotHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual(
      expect.objectContaining({
        uid: "uid_1",
        sessionState: expect.objectContaining({
          activeSid: "sid_123",
          sessionVersion: 5,
        }),
        securitySettings: expect.objectContaining({
          sessionLocked: true,
          lockAfterMinutes: 5,
          biometricsEnabled: true,
        }),
      })
    );
  });
});
