import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { CreateIdentityUploadSession } from "../application/usecase/CreateIdentityUploadSession.js";
const SUPPORTED_DOCUMENT_TYPES = new Set([
    "passport",
    "drivers_license",
    "national_id",
]);
export async function identityUploadSessionHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const documentType = (req.body?.documentType ?? "").toString().trim();
        const payloadSha256 = (req.body?.payloadSha256 ?? "").toString().trim();
        const contentType = (req.body?.contentType ?? "").toString().trim();
        if (!SUPPORTED_DOCUMENT_TYPES.has(documentType)) {
            return res.status(400).json({ error: "Unsupported documentType" });
        }
        if (!payloadSha256) {
            return res.status(400).json({ error: "Missing payloadSha256" });
        }
        if (!contentType) {
            return res.status(400).json({ error: "Missing contentType" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { identityUploads } = authContainer();
        const useCase = new CreateIdentityUploadSession(identityUploads);
        const session = await useCase.execute(decoded.uid, {
            documentType,
            payloadSha256,
            contentType,
        });
        return res.status(200).json(session);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("not configured")) {
            console.error("identityUploadSessionHandler misconfigured", error);
            return res.status(503).json({
                error: "Identity upload service is not configured",
                code: "IDENTITY_UPLOAD_SERVICE_MISCONFIGURED",
            });
        }
        if (message.includes("STORAGE_EMULATOR_SIGNED_URL_UNSUPPORTED")) {
            return res.status(503).json({
                error: "Storage emulator does not support signed URL uploads for identity. Run without Storage emulator for identity E2E, or add a local server-upload fallback.",
                code: "IDENTITY_UPLOAD_STORAGE_EMULATOR_UNSUPPORTED",
            });
        }
        if (message.includes("Unsupported") ||
            message.includes("Invalid") ||
            message.includes("Missing")) {
            return res.status(400).json({ error: message });
        }
        console.error("identityUploadSessionHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=identityUploadSession.js.map