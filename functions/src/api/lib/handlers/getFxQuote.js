import { fxContainer } from "../infrastructure/di/fxContainer.js";
export function createGetFxQuoteHandler(rateProvider = fxContainer().exchangeRateProvider) {
    return async function getFxQuoteHandler(req, res) {
        const sourceCurrency = normalizeCurrency(req.query.source || "USD");
        const targetCurrency = normalizeCurrency(req.query.target || "EUR");
        const amount = normalizeAmount(req.query.amount || "100");
        const method = normalizeMethod(req.query.method || "wire");
        if (!sourceCurrency || !targetCurrency) {
            return res.status(400).json({ error: "invalid_currency" });
        }
        if (amount <= 0) {
            return res.status(400).json({ error: "invalid_amount" });
        }
        try {
            const rateResult = await rateProvider.getRate(sourceCurrency, targetCurrency);
            const methodFee = feeForMethod(method);
            const ourFee = 3.76;
            const totalFees = methodFee + ourFee;
            const recipientAmount = round2((amount - totalFees) * rateResult.rate);
            const responseBody = {
                sourceCurrency,
                targetCurrency,
                sourceAmount: amount,
                rate: rateResult.rate,
                recipientAmount,
                fees: [
                    {
                        label: "Payment method fee",
                        amount: methodFee,
                        code: "payment_method_fee",
                    },
                    {
                        label: "Our fee",
                        amount: ourFee,
                        code: "our_fee",
                    },
                ],
                guaranteeSeconds: 11 * 60 * 60,
                arrivalSeconds: arrivalForMethod(method),
            };
            res.setHeader("Content-Type", "application/json");
            res.setHeader("Cache-Control", "private, max-age=30");
            res.setHeader("X-Rate-Source", rateResult.source);
            return res.status(200).json(responseBody);
        }
        catch (error) {
            const message = error instanceof Error ? error.message : String(error);
            const low = message.toLowerCase();
            if (low.includes("not configured")) {
                return res.status(503).json({ error: "missing_api_key" });
            }
            if (low.includes("provider status 401") ||
                low.includes("provider status 403") ||
                low.includes("provider status 404")) {
                return res.status(400).json({ error: "provider", detail: message });
            }
            return res.status(502).json({ error: "provider", detail: message });
        }
    };
}
export const getFxQuoteHandler = createGetFxQuoteHandler();
function normalizeCurrency(value) {
    const normalized = value.trim().toUpperCase();
    return normalized.length === 3 ? normalized : "";
}
function normalizeAmount(value) {
    const parsed = Number(value);
    return Number.isFinite(parsed) ? parsed : 0;
}
function normalizeMethod(value) {
    const method = value.trim();
    switch (method) {
        case "wire":
        case "debitCard":
        case "creditCard":
        case "accountTransfer":
            return method;
        default:
            return "wire";
    }
}
function feeForMethod(method) {
    switch (method) {
        case "wire":
            return 6.11;
        case "debitCard":
            return 12.35;
        case "creditCard":
            return 61.65;
        case "accountTransfer":
            return 0.0;
        default:
            return 6.11;
    }
}
function arrivalForMethod(method) {
    if (method === "wire") {
        return 4 * 60 * 60;
    }
    return 2 * 60 * 60;
}
function round2(value) {
    return Math.round((value + Number.EPSILON) * 100) / 100;
}
//# sourceMappingURL=getFxQuote.js.map