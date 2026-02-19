export function corsify(res) {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Firebase-AppCheck, X-API-KEY");
    res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
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
    console.log(`[${ts}] [AuthPolicy:${label}]]`, JSON.stringify(data, null, 2));
}
//# sourceMappingURL=utils.js.map