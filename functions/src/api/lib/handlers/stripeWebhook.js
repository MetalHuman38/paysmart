import { authContainer } from "../infrastructure/di/authContainer.js";
import { HandleStripeWebhook } from "../application/usecase/HandleStripeWebhook.js";
export async function stripeWebhookHandler(req, res) {
    try {
        const rawPayload = resolveRawPayload(req.body);
        if (!rawPayload) {
            return res.status(400).json({ error: "Missing payload" });
        }
        const signatureHeader = resolveSignatureHeader(req.headers["stripe-signature"]);
        const { addMoney } = authContainer();
        const useCase = new HandleStripeWebhook(addMoney);
        const result = await useCase.execute(rawPayload, signatureHeader);
        return res.status(200).json({
            ok: true,
            handled: result.handled,
            sessionId: result.sessionId ?? null,
            uid: result.uid ?? null,
            status: result.status ?? null,
        });
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (message.includes("signature") ||
            message.includes("payload") ||
            message.includes("Missing Stripe signature header")) {
            return res.status(400).json({ error: message });
        }
        if (message.includes("not configured") ||
            message.includes("must be a secret key") ||
            message.includes("STRIPE_WEBHOOK_SECRET is required")) {
            return res.status(503).json({
                error: "Payments service is not configured",
                code: "PAYMENTS_SERVICE_MISCONFIGURED",
            });
        }
        console.error("stripeWebhookHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
function resolveRawPayload(rawBody) {
    if (Buffer.isBuffer(rawBody)) {
        return rawBody.toString("utf8");
    }
    if (typeof rawBody === "string") {
        return rawBody;
    }
    if (rawBody && typeof rawBody === "object") {
        return JSON.stringify(rawBody);
    }
    return "";
}
function resolveSignatureHeader(rawHeader) {
    if (typeof rawHeader === "string" && rawHeader.trim()) {
        return rawHeader.trim();
    }
    if (Array.isArray(rawHeader) && rawHeader.length > 0) {
        const first = rawHeader[0];
        if (typeof first === "string" && first.trim()) {
            return first.trim();
        }
    }
    return undefined;
}
//# sourceMappingURL=stripeWebhook.js.map