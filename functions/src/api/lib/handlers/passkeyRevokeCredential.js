import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
export async function passkeyRevokeCredentialHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const credentialId = (req.body?.credentialId || "").toString().trim();
        if (!credentialId) {
            return res.status(400).json({ error: "credentialId is required" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { passkeys } = authContainer();
        const result = await passkeys.revokeCredential(decoded.uid, credentialId);
        return res.status(200).json(result);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("PASSKEY_CREDENTIAL")) {
            return res.status(404).json({ error: message });
        }
        console.error("passkeyRevokeCredentialHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=passkeyRevokeCredential.js.map