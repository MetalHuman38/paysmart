import { randomUUID } from "node:crypto";
import { FieldValue } from "firebase-admin/firestore";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { logPasskeyAuditEvent } from "./passkeyAudit.js";
export async function passkeySignInVerifyHandler(req, res) {
    const attemptedCredentialId = extractCredentialIdForAudit(req.body?.credentialJson);
    logPasskeyAuditEvent(req, "signin_verify_attempt", {
        credentialId: toCredentialIdPreview(attemptedCredentialId),
    });
    try {
        const credential = req.body?.credentialJson ?? req.body?.credential;
        if (!credential) {
            logPasskeyAuditEvent(req, "signin_verify_failure", {
                error: "Missing credentialJson",
                code: "MISSING_CREDENTIAL_JSON",
            });
            return res.status(400).json({ error: "Missing credentialJson" });
        }
        const { passkeys } = authContainer();
        const result = await passkeys.completeSignIn(credential);
        const { auth } = initDeps();
        const sessionClaims = await resolveActiveSessionClaims(result.uid);
        const customToken = await auth.createCustomToken(result.uid, sessionClaims);
        logPasskeyAuditEvent(req, "signin_verify_success", {
            uid: result.uid,
            credentialId: result.credentialId,
        });
        return res.status(200).json({
            verified: result.verified,
            uid: result.uid,
            credentialId: result.credentialId,
            customToken,
        });
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        logPasskeyAuditEvent(req, "signin_verify_failure", {
            error: message,
            code: message,
            credentialId: toCredentialIdPreview(attemptedCredentialId),
        });
        if (message.includes("PASSKEY_NOT_CONFIGURED")) {
            return res.status(503).json({
                error: "Passkey service is not configured. Set PASSKEY_RP_ID and PASSKEY_EXPECTED_ORIGINS (or PASSKEY_ANDROID_APK_KEY_HASHES). Android hashes must be Base64URL without padding.",
                code: "PASSKEY_NOT_CONFIGURED",
            });
        }
        if (message.includes("PASSKEY_CHALLENGE") ||
            message.includes("PASSKEY_AUTHENTICATION") ||
            message.includes("PASSKEY_CREDENTIAL")) {
            return res.status(401).json({ error: message });
        }
        console.error("passkeySignInVerifyHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
async function resolveActiveSessionClaims(uid) {
    const { firestore } = initDeps();
    const currentStateRef = firestore
        .collection("users")
        .doc(uid)
        .collection("authSessionState")
        .doc("current");
    const currentStateSnap = await currentStateRef.get();
    const existingSid = currentStateSnap.exists ? normalizeSid(currentStateSnap.get("activeSid")) : null;
    const existingSv = currentStateSnap.exists ? normalizeSessionVersion(currentStateSnap.get("sessionVersion")) : null;
    if (existingSid && existingSv) {
        return { sid: existingSid, sv: existingSv };
    }
    const bootstrapSessionClaims = {
        sid: randomUUID(),
        sv: 1,
    };
    await currentStateRef.set({
        activeSid: bootstrapSessionClaims.sid,
        sessionVersion: bootstrapSessionClaims.sv,
        updatedAt: FieldValue.serverTimestamp(),
    }, { merge: true });
    return bootstrapSessionClaims;
}
function normalizeSid(value) {
    if (typeof value !== "string")
        return null;
    const sid = value.trim();
    return sid.length > 0 ? sid : null;
}
function normalizeSessionVersion(value) {
    if (typeof value === "number" && Number.isFinite(value) && value > 0) {
        return Math.floor(value);
    }
    if (typeof value === "string") {
        const parsed = Number(value);
        if (Number.isFinite(parsed) && parsed > 0) {
            return Math.floor(parsed);
        }
    }
    return null;
}
function extractCredentialIdForAudit(input) {
    if (!input)
        return "";
    if (typeof input === "string") {
        const trimmed = input.trim();
        if (!trimmed)
            return "";
        try {
            const parsed = JSON.parse(trimmed);
            return ((parsed.id ?? parsed.rawId ?? "").toString().trim());
        }
        catch {
            return "";
        }
    }
    if (typeof input === "object") {
        const parsed = input;
        return ((parsed.id ?? parsed.rawId ?? "").toString().trim());
    }
    return "";
}
function toCredentialIdPreview(value) {
    if (!value)
        return undefined;
    if (value.length <= 12)
        return value;
    return `${value.slice(0, 12)}...`;
}
//# sourceMappingURL=passkeySignInVerify.js.map