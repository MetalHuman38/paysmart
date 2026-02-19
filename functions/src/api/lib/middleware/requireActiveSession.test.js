import { describe, expect, it, vi } from "vitest";
import { createRequireActiveSession } from "./requireActiveSession.js";
function createResponseRecorder() {
    return {
        statusCode: 200,
        payload: undefined,
        status(code) {
            this.statusCode = code;
            return this;
        },
        json(body) {
            this.payload = body;
            return this;
        },
    };
}
describe("requireActiveSession middleware", () => {
    it("allows request when sid/sv claims match server session state", async () => {
        const verifyIdToken = vi.fn().mockResolvedValue({
            uid: "uid-1",
            sid: "sid-1",
            sv: 3,
        });
        const getSessionState = vi.fn().mockResolvedValue({
            activeSid: "sid-1",
            sessionVersion: 3,
        });
        const middleware = createRequireActiveSession({
            verifyIdToken,
            getSessionState,
        });
        const req = {
            headers: {
                authorization: "Bearer token",
            },
        };
        const res = createResponseRecorder();
        const next = vi.fn();
        await middleware(req, res, next);
        expect(next).toHaveBeenCalledTimes(1);
        expect(res.statusCode).toBe(200);
    });
    it("rejects when sid does not match active session", async () => {
        const middleware = createRequireActiveSession({
            verifyIdToken: vi.fn().mockResolvedValue({
                uid: "uid-2",
                sid: "sid-client",
                sv: 1,
            }),
            getSessionState: vi.fn().mockResolvedValue({
                activeSid: "sid-server",
                sessionVersion: 1,
            }),
        });
        const req = {
            headers: {
                authorization: "Bearer token",
            },
        };
        const res = createResponseRecorder();
        const next = vi.fn();
        await middleware(req, res, next);
        expect(next).not.toHaveBeenCalled();
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Session replaced. Please sign in again." });
    });
    it("rejects when token is missing sid/sv claims", async () => {
        const middleware = createRequireActiveSession({
            verifyIdToken: vi.fn().mockResolvedValue({
                uid: "uid-3",
            }),
            getSessionState: vi.fn(),
        });
        const req = {
            headers: {
                authorization: "Bearer token",
            },
        };
        const res = createResponseRecorder();
        const next = vi.fn();
        await middleware(req, res, next);
        expect(next).not.toHaveBeenCalled();
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Session claims missing. Please sign in again." });
    });
});
//# sourceMappingURL=requireActiveSession.test.js.map