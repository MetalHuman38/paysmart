import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
export async function passkeyRegisterVerifyHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const credential = req.body?.credentialJson ?? req.body?.credential;
        if (!credential) {
            return res.status(400).json({ error: "Missing credentialJson" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { passkeys, securitySettings } = authContainer();
        const result = await passkeys.completeRegistration(decoded.uid, credential);
        await securitySettings.createIfMissing(decoded.uid);
        await securitySettings.update(decoded.uid, {
            passkeyEnabled: true,
            hasSkippedPasskeyEnrollmentPrompt: false,
        });
        return res.status(200).json(result);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("PASSKEY_NOT_CONFIGURED")) {
            return res.status(503).json({
                error: "Passkey service is not configured. Set PASSKEY_RP_ID and PASSKEY_EXPECTED_ORIGINS (or PASSKEY_ANDROID_APK_KEY_HASHES). Android hashes must be Base64URL without padding.",
                code: "PASSKEY_NOT_CONFIGURED",
            });
        }
        if (message.includes("PASSKEY_CHALLENGE") ||
            message.includes("PASSKEY_REGISTRATION") ||
            message.includes("credential")) {
            return res.status(400).json({ error: message });
        }
        console.error("passkeyRegisterVerifyHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=passkeyRegisterVerify.js.map