import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { CreateFlutterwaveAddMoneySession } from "../application/usecase/CreateFlutterwaveAddMoneySession.js";
import { resolveFlutterwavePaymentsConfigErrorCode } from "./utils/flutterwavePaymentsConfigError.js";
import { FlutterwaveProviderRequestError } from "../services/flutterwavePaymentsService.js";
export async function createFlutterwaveAddMoneySessionHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const amountMinor = resolveAmountMinor(req.body);
        const currency = (req.body?.currency ?? "NGN").toString();
        const idempotencyKey = resolveIdempotencyKey(req);
        const { addMoneyFlutterwave } = authContainer();
        const useCase = new CreateFlutterwaveAddMoneySession(addMoneyFlutterwave);
        const session = await useCase.execute(decoded.uid, {
            amountMinor,
            currency,
            idempotencyKey,
        });
        return res.status(200).json(session);
    }
    catch (error) {
        const message = error instanceof Error ? error.message : "Internal error";
        if (error instanceof FlutterwaveProviderRequestError) {
            if (error.status === 400 || error.status === 422) {
                return res.status(400).json({
                    error: message,
                    code: error.code,
                    details: error.details.length > 0 ? error.details : undefined,
                });
            }
            if (error.status === 409) {
                return res.status(409).json({
                    error: message,
                    code: error.code || error.type || "FLUTTERWAVE_PROVIDER_CONFLICT",
                    details: error.details.length > 0 ? error.details : undefined,
                });
            }
            if (error.status === 401 || error.status === 403) {
                return res.status(503).json({
                    error: "Payments provider rejected the request",
                    code: error.code || error.type || "FLUTTERWAVE_PROVIDER_REJECTED",
                    details: error.details.length > 0 ? error.details : undefined,
                });
            }
        }
        const paymentsConfigErrorCode = resolveFlutterwavePaymentsConfigErrorCode(message);
        if (paymentsConfigErrorCode) {
            return res.status(503).json({
                error: "Payments service is not configured",
                code: paymentsConfigErrorCode,
            });
        }
        const normalizedMessage = message.toLowerCase();
        if (normalizedMessage.includes("invalid") ||
            normalizedMessage.includes("unsupported") ||
            normalizedMessage.includes("missing") ||
            normalizedMessage.includes("amount must be") ||
            normalizedMessage.includes("amount exceeds") ||
            normalizedMessage.includes("request is not valid")) {
            return res.status(400).json({ error: message });
        }
        console.error("createFlutterwaveAddMoneySessionHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
function resolveAmountMinor(rawBody) {
    const body = rawBody;
    const asMinor = body?.amountMinor;
    const parsedMinor = parseNumber(asMinor);
    if (parsedMinor !== null) {
        return parsedMinor;
    }
    const asMajor = parseNumber(body?.amount);
    if (asMajor === null) {
        throw new Error("Missing amountMinor");
    }
    return Math.round(asMajor * 100);
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
function parseNumber(raw) {
    if (typeof raw === "number" && Number.isFinite(raw)) {
        return raw;
    }
    if (typeof raw === "string") {
        const parsed = Number(raw.trim());
        if (Number.isFinite(parsed)) {
            return parsed;
        }
    }
    return null;
}
//# sourceMappingURL=createFlutterwaveAddMoneySession.js.map