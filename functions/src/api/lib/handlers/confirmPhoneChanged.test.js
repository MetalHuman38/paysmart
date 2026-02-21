import { beforeEach, describe, expect, it, vi } from "vitest";
const verifyIdToken = vi.fn();
const createIfMissing = vi.fn();
const update = vi.fn();
const updatePhoneNumber = vi.fn();
const logAuditEvent = vi.fn();
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
        userRepo: {
            updatePhoneNumber,
            logAuditEvent,
        },
    }),
}));
import { confirmPhoneChangedHandler } from "./confirmPhoneChanged.js";
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
describe("confirmPhoneChangedHandler", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });
    it("returns 200 and confirms phone change when token and payload are valid", async () => {
        verifyIdToken.mockResolvedValue({ uid: "uid-1" });
        createIfMissing.mockResolvedValue(undefined);
        update.mockResolvedValue(undefined);
        updatePhoneNumber.mockResolvedValue(undefined);
        logAuditEvent.mockResolvedValue(undefined);
        const req = {
            headers: {
                authorization: "Bearer test-token",
            },
            body: {
                phoneNumber: "+447988777954",
            },
        };
        const res = createResponseRecorder();
        await confirmPhoneChangedHandler(req, res);
        expect(res.statusCode).toBe(200);
        expect(res.payload).toEqual({ ok: true });
        expect(verifyIdToken).toHaveBeenCalledWith("test-token");
        expect(createIfMissing).toHaveBeenCalledWith("uid-1");
        expect(updatePhoneNumber).toHaveBeenCalledWith("uid-1", "+447988777954");
        expect(logAuditEvent).toHaveBeenCalledWith(expect.objectContaining({
            uid: "uid-1",
            event: "phone_changed_confirmed",
            phoneNumber: "+447988777954",
        }));
    });
});
//# sourceMappingURL=confirmPhoneChanged.test.js.map