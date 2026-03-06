import rateLimit from "express-rate-limit";
import { APP, SECURITY } from "../config/globals.js";
import { logPasskeyAuditEvent } from "../handlers/passkeyAudit.js";
function buildLimiter(maxRequests) {
    return rateLimit({
        windowMs: SECURITY.rateLimit.authWindowMs,
        max: maxRequests,
        standardHeaders: true,
        legacyHeaders: false,
        keyGenerator: (req) => {
            const ip = (req.ip || "").trim() || "unknown";
            const appId = (req.appCheck?.app_id || "").trim() || "unknown-app";
            return `${appId}:${ip}`;
        },
        skip: () => APP.emulator,
        handler: (req, res) => {
            logPasskeyAuditEvent(req, "signin_rate_limited", {
                code: "PASSKEY_SIGNIN_RATE_LIMITED",
            });
            return res.status(429).json({
                error: "Too many passkey sign-in attempts. Please try again shortly.",
                code: "PASSKEY_SIGNIN_RATE_LIMITED",
            });
        },
    });
}
const baseAuthMax = Math.max(5, SECURITY.rateLimit.authMax);
export const passkeySignInOptionsRateLimit = buildLimiter(Math.max(8, Math.floor(baseAuthMax)));
export const passkeySignInVerifyRateLimit = buildLimiter(Math.max(5, Math.floor(baseAuthMax / 2)));
//# sourceMappingURL=passkeySignInRateLimit.js.map