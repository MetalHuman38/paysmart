import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { CommitIdentityUpload } from "../application/usecase/CommitIdentityUpload.js";
export async function identityUploadCommitHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const sessionId = (req.body?.sessionId ?? "").toString().trim();
        const payloadSha256 = (req.body?.payloadSha256 ?? "").toString().trim();
        const attestationJwt = (req.body?.attestationJwt ?? "").toString().trim();
        if (!sessionId) {
            return res.status(400).json({ error: "Missing sessionId" });
        }
        if (!payloadSha256) {
            return res.status(400).json({ error: "Missing payloadSha256" });
        }
        if (!attestationJwt) {
            return res.status(400).json({ error: "Missing attestationJwt" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { identityUploads, securitySettings } = authContainer();
        const useCase = new CommitIdentityUpload(identityUploads, securitySettings);
        const receipt = await useCase.execute(decoded.uid, {
            sessionId,
            payloadSha256,
            attestationJwt,
        });
        return res.status(200).json(receipt);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("expired") ||
            message.includes("mismatch") ||
            message.includes("not found") ||
            message.includes("Missing") ||
            message.includes("uploaded") ||
            message.includes("empty") ||
            message.includes("attestation") ||
            message.includes("Integrity") ||
            message.includes("payload") ||
            message.includes("session") ||
            message.includes("disabled") ||
            message.includes("licensed")) {
            return res.status(400).json({ error: message });
        }
        console.error("identityUploadCommitHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=identityUploadCommit.js.map