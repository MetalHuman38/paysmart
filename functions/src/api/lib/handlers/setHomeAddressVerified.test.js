import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const createIfMissing = vi.fn();
const update = vi.fn();
vi.mock("../dependencies.js", () => ({
    initDeps: () => ({
        auth: {
            verifyIdToken,
        },
    }),
}));
vi.mock("../infrastructure/di/authContainer.js", () => ({
    authContainer: () => ({
        securitySettings: {
            createIfMissing,
            update,
        },
    }),
}));
import { setHomeAddressVerifiedHandler } from "./setHomeAddressVerified.js";
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
describe("setHomeAddressVerifiedHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 401 when authorization token is missing", async () => {
        const req = { headers: {} };
        const res = createResponseRecorder();
        await setHomeAddressVerifiedHandler(req, res);
        expect(res.statusCode).toBe(401);
        expect(res.payload).toEqual({ error: "Missing token" });
        expect(verifyIdToken).not.toHaveBeenCalled();
        expect(createIfMissing).not.toHaveBeenCalled();
        expect(update).not.toHaveBeenCalled();
    });
    it("returns 200 and marks address as verified on valid token", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        createIfMissing.mockResolvedValue(undefined);
        update.mockResolvedValue(undefined);
        const req = {
            headers: {
                authorization: "Bearer test-token",
            },
        };
        const res = createResponseRecorder();
        await setHomeAddressVerifiedHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(res.payload).toEqual({ ok: true });
        expect(verifyIdToken).toHaveBeenCalledWith("test-token");
        expect(createIfMissing).toHaveBeenCalledWith("uid-1");
        expect(update).toHaveBeenCalledWith("uid-1", expect.objectContaining({
            hasAddedHomeAddress: true,
        }));
    });
});
//# sourceMappingURL=setHomeAddressVerified.test.js.map