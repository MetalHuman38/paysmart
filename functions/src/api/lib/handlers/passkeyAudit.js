import { APP, SECURITY } from "../config/globals.js";
export function logPasskeyAuditEvent(req, event, extra = {}) {
    const payload = {
        event,
        path: req.path,
        method: req.method,
        origin: readHeader(req, "origin") || "none",
        userAgent: readHeader(req, "user-agent") || "unknown",
        ip: resolveClientIp(req),
        appCheckVerdict: resolveAppCheckVerdict(req),
        appId: req.appCheck?.app_id,
        ...extra,
    };
    console.info("[PasskeyAudit]", payload);
}
function resolveClientIp(req) {
    const forwarded = readHeader(req, "x-forwarded-for");
    if (forwarded)
        return forwarded.split(",")[0].trim();
    return (req.ip || "").trim() || "unknown";
}
function resolveAppCheckVerdict(req) {
    if (APP.emulator || !SECURITY.appCheckRequired) {
        return "bypassed";
    }
    if (req.appCheck?.app_id) {
        return "verified";
    }
    if (req.appCheck) {
        return "verified_without_app_id";
    }
    return "missing_or_invalid";
}
function readHeader(req, name) {
    const viaFn = typeof req.header === "function" ? req.header(name) : undefined;
    if (typeof viaFn === "string" && viaFn.trim()) {
        return viaFn.trim();
    }
    const rawHeaders = req.headers || {};
    const lower = name.toLowerCase();
    const value = rawHeaders[lower] ?? rawHeaders[name];
    if (Array.isArray(value)) {
        return (value[0] || "").toString().trim();
    }
    return (value || "").toString().trim();
}
//# sourceMappingURL=passkeyAudit.js.map