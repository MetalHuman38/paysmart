import { getApp, getApps, initializeApp } from "firebase-admin/app";
import { getAppCheck } from "firebase-admin/app-check";
import { APP, SECURITY } from "./globals.js";
// Middleware to require and verify Firebase App Check token
export async function requireAppCheck(req, res, next) {
    // Emulator or disabled → pass through (with a marker)
    if (APP.emulator || !SECURITY.appCheckRequired) {
        req.appCheck = null;
        return next();
    }
    try {
        const token = (req.header("X-Firebase-AppCheck") ||
            req.header("X-Firebase-AppCheck-Token") || // older alt header
            "").trim();
        if (!token) {
            console.warn("[AppCheck] Missing token header", {
                path: req.path,
                method: req.method
            });
            return res.status(401).json({ error: "Missing App Check token" });
        }
        const app = getApps().length === 0 ? initializeApp() : getApp();
        const result = await getAppCheck(app).verifyToken(token);
        // attach claims for downstream handlers (optional)
        req.appCheck = result.token;
        return next();
    }
    catch (err) {
        console.warn("[AppCheck] Token verification failed", {
            path: req.path,
            method: req.method,
            error: err instanceof Error ? err.message : String(err)
        });
        return res.status(401).json({ error: "Invalid App Check token" });
    }
}
//# sourceMappingURL=appcheck.js.map