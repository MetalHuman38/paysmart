import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { UploadIdentityEncryptedPayload } from "../application/usecase/UploadIdentityEncryptedPayload.js";
export async function identityUploadPayloadHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const sessionId = (req.body?.sessionId ?? "").toString().trim();
        const payloadBase64 = (req.body?.payloadBase64 ?? "").toString().trim();
        const contentType = (req.body?.contentType ?? "").toString().trim();
        if (!sessionId) {
            return res.status(400).json({ error: "Missing sessionId" });
        }
        if (!payloadBase64) {
            return res.status(400).json({ error: "Missing payloadBase64" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { identityUploads } = authContainer();
        const useCase = new UploadIdentityEncryptedPayload(identityUploads);
        const result = await useCase.execute(decoded.uid, {
            sessionId,
            payloadBase64,
            contentType,
        });
        return res.status(200).json(result);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("EMULATOR_ONLY") ||
            message.includes("Missing") ||
            message.includes("Invalid") ||
            message.includes("expired") ||
            message.includes("not found") ||
            message.includes("already committed") ||
            message.includes("exceeds max size") ||
            message.includes("empty")) {
            return res.status(400).json({ error: message });
        }
        console.error("identityUploadPayloadHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=identityUploadPayload.js.map