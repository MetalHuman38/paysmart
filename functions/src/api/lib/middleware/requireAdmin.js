import { initDeps } from "../dependencies.js";
function normalizeEmail(raw) {
    if (typeof raw !== "string")
        return null;
    const email = raw.trim().toLowerCase();
    return email.length > 0 ? email : null;
}
function parseAllowlistedEmails(raw) {
    if (!raw)
        return new Set();
    return new Set(raw
        .split(",")
        .map((value) => value.trim().toLowerCase())
        .filter(Boolean));
}
function hasAdminClaim(decoded) {
    if (decoded.admin === true)
        return true;
    if (typeof decoded.role === "string" && decoded.role.trim().toLowerCase() === "admin") {
        return true;
    }
    if (Array.isArray(decoded.roles)) {
        return decoded.roles.some((entry) => {
            return typeof entry === "string" && entry.trim().toLowerCase() === "admin";
        });
    }
    return false;
}
export function createRequireAdmin(deps) {
    const resolvedDeps = {
        verifyIdToken: deps?.verifyIdToken ??
            (async (idToken) => {
                const { auth } = initDeps();
                return (await auth.verifyIdToken(idToken, true));
            }),
        allowlistedEmails: deps?.allowlistedEmails ??
            parseAllowlistedEmails(process.env.ADMIN_ALLOWLIST_EMAILS),
    };
    return async function requireAdmin(req, res, next) {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.substring("Bearer ".length).trim();
        if (!idToken) {
            return res.status(401).json({ error: "Missing token" });
        }
        let decoded;
        try {
            decoded = await resolvedDeps.verifyIdToken(idToken);
        }
        catch {
            return res.status(401).json({ error: "Invalid token" });
        }
        const email = normalizeEmail(decoded.email);
        const adminClaim = hasAdminClaim(decoded);
        const allowlisted = email ? resolvedDeps.allowlistedEmails.has(email) : false;
        if (!adminClaim && !allowlisted) {
            return res.status(403).json({ error: "Admin access required" });
        }
        res.locals.admin = {
            uid: decoded.uid,
            email,
            isAdminClaim: adminClaim,
            isAllowlistedEmail: allowlisted,
        };
        return next();
    };
}
export const requireAdmin = createRequireAdmin();
//# sourceMappingURL=requireAdmin.js.map