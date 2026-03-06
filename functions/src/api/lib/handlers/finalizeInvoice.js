import { FinalizeInvoice } from "../application/usecase/FinalizeInvoice.js";
import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
export async function finalizeInvoiceHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const idempotencyKey = resolveIdempotencyKey(req);
        const body = req.body;
        const { invoices } = authContainer();
        const useCase = new FinalizeInvoice(invoices);
        const result = await useCase.execute(decoded.uid, {
            ...body,
            idempotencyKey,
        });
        return res.status(200).json(result);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (isBadRequest(message)) {
            return res.status(400).json({ error: message });
        }
        console.error("finalizeInvoiceHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
function resolveIdempotencyKey(req) {
    const fromHeader = req.headers["idempotency-key"];
    if (typeof fromHeader === "string" && fromHeader.trim()) {
        return fromHeader.trim();
    }
    const fromBody = req.body?.idempotencyKey;
    if (typeof fromBody === "string" && fromBody.trim()) {
        return fromBody.trim();
    }
    return undefined;
}
function isBadRequest(message) {
    return (message.includes("required") ||
        message.includes("must be") ||
        message.includes("Invalid") ||
        message.includes("currency"));
}
//# sourceMappingURL=finalizeInvoice.js.map