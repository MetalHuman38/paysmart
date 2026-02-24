import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const uploadEncryptedPayload = vi.fn();
let consoleErrorSpy;
vi.mock("../dependencies.js", () => ({
    initDeps: () => ({
        auth: {
            verifyIdToken,
        },
    }),
}));
vi.mock("../infrastructure/di/authContainer.js", () => ({
    authContainer: () => ({
        identityUploads: {
            uploadEncryptedPayload,
        },
    }),
}));
import { identityUploadPayloadHandler } from "./identityUploadPayload.js";
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
describe("identityUploadPayloadHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
        consoleErrorSpy = vi.spyOn(console, "error").mockImplementation(() => { });
    });
    afterEach(() => {
        consoleErrorSpy.mockRestore();
    });
    it("returns 401 when token is missing", async () => {
        const req = {
            headers: {},
            body: {},
        };
        const res = createResponseRecorder();
        await identityUploadPayloadHandler(req, res);
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Missing token" });
        expect(verifyIdToken).not.toHaveBeenCalled();
    });
    it("returns 200 and uploads encrypted payload", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        uploadEncryptedPayload.mockResolvedValue({
            sessionId: "sess-1",
            objectPath: "identityUploads/uid-1/sess-1/payload.enc",
            bytesWritten: 128,
        });
        const req = {
            headers: {
                authorization: "Bearer token-1",
            },
            body: {
                sessionId: "sess-1",
                payloadBase64: "AQID",
                contentType: "application/octet-stream",
            },
        };
        const res = createResponseRecorder();
        await identityUploadPayloadHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(verifyIdToken).toHaveBeenCalledWith("token-1");
        expect(uploadEncryptedPayload).toHaveBeenCalledWith("uid-1", {
            sessionId: "sess-1",
            payloadBase64: "AQID",
            contentType: "application/octet-stream",
        });
        expect(res.payload).toEqual({
            sessionId: "sess-1",
            objectPath: "identityUploads/uid-1/sess-1/payload.enc",
            bytesWritten: 128,
        });
    });
    it("returns 400 for emulator-only restriction", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        uploadEncryptedPayload.mockRejectedValue(new Error("EMULATOR_ONLY: direct payload upload endpoint is only available when FIREBASE_STORAGE_EMULATOR_HOST is configured"));
        const req = {
            headers: {
                authorization: "Bearer token-1",
            },
            body: {
                sessionId: "sess-1",
                payloadBase64: "AQID",
            },
        };
        const res = createResponseRecorder();
        await identityUploadPayloadHandler(req, res);
        expect(res.statusCode).toBe(400);
        expect(res.payload).toEqual({
            error: "EMULATOR_ONLY: direct payload upload endpoint is only available when FIREBASE_STORAGE_EMULATOR_HOST is configured",
        });
    });
});
//# sourceMappingURL=identityUploadPayload.test.js.map