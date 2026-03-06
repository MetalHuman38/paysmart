import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
export async function passkeyAuthenticateOptionsHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { passkeys } = authContainer();
        const options = await passkeys.beginAuthentication(decoded.uid);
        return res.status(200).json({ options });
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("PASSKEY_NOT_CONFIGURED")) {
            return res.status(503).json({
                error: "Passkey service is not configured. Set PASSKEY_RP_ID and PASSKEY_EXPECTED_ORIGINS (or PASSKEY_ANDROID_APK_KEY_HASHES). Android hashes must be Base64URL without padding.",
                code: "PASSKEY_NOT_CONFIGURED",
            });
        }
        if (message.includes("PASSKEY_CREDENTIALS_NOT_FOUND")) {
            return res.status(404).json({ error: "No passkeys are registered for this user" });
        }
        console.error("passkeyAuthenticateOptionsHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=passkeyAuthenticateOptions.js.map