import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { StartIdentityProviderSession } from "../application/usecase/StartIdentityProviderSession.js";
export async function identityProviderStartSessionHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const countryIso2 = asOptionalString(req.body?.countryIso2);
        const documentType = asOptionalString(req.body?.documentType);
        const { identityProvider } = authContainer();
        const useCase = new StartIdentityProviderSession(identityProvider);
        const session = await useCase.execute(decoded.uid, {
            countryIso2,
            documentType,
        });
        return res.status(200).json(session);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("Missing") || message.includes("Invalid")) {
            return res.status(400).json({ error: message });
        }
        console.error("identityProviderStartSessionHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
function asOptionalString(value) {
    if (typeof value !== "string")
        return undefined;
    const trimmed = value.trim();
    return trimmed || undefined;
}
//# sourceMappingURL=identityProviderStartSession.js.map