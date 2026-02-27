const REDACT_KEY_PATTERN = /(authorization|token|secret|password|credential|key|jwt|payload|link|cookie)/i;
export function corsify(res) {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Headers", "Authorization, Content-Type, Idempotency-Key, Stripe-Signature, X-Firebase-AppCheck, X-API-KEY");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
}
export function detectProvider(eventType, data, user) {
    // Prefer eventType suffix: providers/firebase.auth/eventTypes/beforeSignIn:<suffix>
    const suffix = eventType?.split(":").pop()?.toLowerCase();
    if (suffix)
        return suffix;
    // Look in data
    const ev = data?.providerId || data?.oauthProvider || data?.signInMethod;
    if (ev)
        return String(ev).toLowerCase();
    // Look in user record
    const up = user?.providerId || user?.provider || user?.signInMethod;
    if (up)
        return String(up).toLowerCase();
    return "";
}
export function getUserMap(b) {
    const d = b.data || {};
    if (d.userInfo && typeof d.userInfo === "object")
        return d.userInfo;
    if (d.user && typeof d.user === "object")
        return d.user;
    return {};
}
export function splitEmail(raw) {
    const e = (raw || "").trim().toLowerCase();
    const i = e.lastIndexOf("@");
    if (i <= 0)
        return { email: "", domain: "" };
    return { email: e, domain: e.slice(i + 1) };
}
export function isDisposable(patterns, email) {
    return patterns.some(rx => rx.test(email));
}
export function isFreeEmail(patterns, email) {
    return patterns.some(rx => rx.test(email));
}
export function isBlockedEmail(patterns, email) {
    return patterns.some(rx => rx.test(email));
}
export function deny(res, status, code, message) {
    corsify(res);
    res.status(status).json({ error: { code, message } });
}
export function ok(res, body) {
    corsify(res);
    res.status(200).json(body);
}
export function logEvent(label, data) {
    const ts = new Date().toISOString();
    console.log(`[${ts}] [AuthPolicy:${label}]`, JSON.stringify(sanitizeForLog(data)));
}
function sanitizeForLog(value, key = "", depth = 0) {
    if (depth > 4)
        return "[TRUNCATED_DEPTH]";
    if (value == null)
        return value;
    const loweredKey = key.toLowerCase();
    if (REDACT_KEY_PATTERN.test(loweredKey))
        return "[REDACTED]";
    if (typeof value === "string") {
        if (loweredKey.includes("email"))
            return maskEmail(value);
        if (loweredKey.includes("phone"))
            return maskPhone(value);
        if (loweredKey === "uid" || loweredKey.endsWith("uid"))
            return maskUid(value);
        return value.length > 200 ? `${value.slice(0, 80)}...(${value.length} chars)` : value;
    }
    if (typeof value === "number" || typeof value === "boolean")
        return value;
    if (Array.isArray(value)) {
        const maxItems = 20;
        const items = value.slice(0, maxItems).map((entry) => sanitizeForLog(entry, key, depth + 1));
        if (value.length > maxItems)
            items.push(`...(${value.length - maxItems} more)`);
        return items;
    }
    if (typeof value === "object") {
        const input = value;
        const output = {};
        for (const [field, fieldValue] of Object.entries(input)) {
            output[field] = sanitizeForLog(fieldValue, field, depth + 1);
        }
        return output;
    }
    return String(value);
}
function maskEmail(raw) {
    const value = raw.trim();
    const at = value.indexOf("@");
    if (at <= 0)
        return "***";
    const local = value.slice(0, at);
    const domain = value.slice(at + 1);
    const localMasked = local.length <= 2 ? `${local[0] ?? "*"}*` : `${local[0]}***${local.slice(-1)}`;
    return `${localMasked}@${domain}`;
}
function maskPhone(raw) {
    const digits = raw.replace(/\D/g, "");
    if (!digits)
        return "***";
    return `***${digits.slice(-4)}`;
}
function maskUid(raw) {
    const value = raw.trim();
    if (!value)
        return "***";
    if (value.length <= 6)
        return "***";
    return `${value.slice(0, 2)}***${value.slice(-2)}`;
}
//# sourceMappingURL=utils.js.map