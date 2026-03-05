import { createHmac, timingSafeEqual } from "crypto";
export class FlutterwavePaymentsService {
    options;
    tokenCache = null;
    constructor(options) {
        this.options = options;
    }
    ensureConfigured() {
        if (this.hasOauthCredentials()) {
            return;
        }
        if (!this.options.secretKey.trim()) {
            throw new Error("FLUTTERWAVE_SECRET_KEY is not configured");
        }
    }
    async createTopupSession(input) {
        this.ensureConfigured();
        const customer = await this.createCustomer(input);
        const amountMajor = roundMajorFromMinor(input.amountMinor);
        const payload = {
            reference: input.reference,
            customer_id: customer.id,
            account_type: "dynamic",
            currency: input.currency,
            amount: amountMajor,
            expiry: this.options.virtualAccountExpirySeconds,
            narration: `PaySmart add money (${input.currency})`,
        };
        const data = await this.providerRequest("/virtual-accounts", "POST", payload, {
            "X-Idempotency-Key": input.idempotencyKey || input.reference,
            "X-Trace-Id": input.reference,
        });
        const response = asRecord(data.data);
        const id = asString(response.id);
        const checkoutUrl = asString(response.checkout_url) || asString(response.payment_url) || undefined;
        const status = asString(response.status) || "pending";
        const createdAtMs = parseDateToMs(asString(response.created_datetime)) ?? Date.now();
        if (!id) {
            throw new Error("Flutterwave virtual account response is incomplete");
        }
        return {
            sessionId: id,
            txRef: input.reference,
            checkoutUrl,
            status,
            amountMinor: input.amountMinor,
            currency: input.currency,
            createdAtMs,
        };
    }
    async retrieveTransactionStatus(transactionId) {
        this.ensureConfigured();
        const cleanId = transactionId.trim();
        if (!cleanId) {
            throw new Error("Missing Flutterwave transactionId");
        }
        const data = await this.providerRequest(`/charges/${encodeURIComponent(cleanId)}`, "GET");
        const response = asRecord(data.data);
        return this.mapTransaction(response);
    }
    async retrieveLatestVirtualAccountCharge(virtualAccountId, expectedReference) {
        this.ensureConfigured();
        const cleanVirtualAccountId = virtualAccountId.trim();
        if (!cleanVirtualAccountId) {
            return null;
        }
        const data = await this.providerRequest(`/charges?virtual_account_id=${encodeURIComponent(cleanVirtualAccountId)}`, "GET");
        const rows = asArray(data.data);
        const cleanReference = expectedReference.trim();
        if (rows.length === 0) {
            return null;
        }
        const matched = rows
            .map(asRecord)
            .filter((row) => {
            if (!cleanReference)
                return true;
            return asString(row.reference) === cleanReference;
        })
            .sort((left, right) => {
            const leftDate = parseDateToMs(asString(left.created_datetime)) ?? 0;
            const rightDate = parseDateToMs(asString(right.created_datetime)) ?? 0;
            return rightDate - leftDate;
        })[0];
        if (!matched) {
            return null;
        }
        return this.mapTransaction(matched);
    }
    parseWebhookEvent(rawPayload, signatureHeader, signatureName) {
        this.ensureConfigured();
        const trimmedSignature = signatureHeader?.trim() || "";
        const normalizedName = (signatureName || "").toLowerCase();
        if (this.options.webhookSecretHash.trim()) {
            if (!trimmedSignature) {
                throw new Error("Missing Flutterwave signature header");
            }
            if (normalizedName === "flutterwave-signature") {
                if (!verifyFlutterwaveHmac(rawPayload, trimmedSignature, this.options.webhookSecretHash)) {
                    throw new Error("Invalid Flutterwave signature");
                }
            }
            else if (trimmedSignature !== this.options.webhookSecretHash.trim()) {
                throw new Error("Invalid Flutterwave signature");
            }
        }
        else if (!this.options.allowUnsignedWebhooks) {
            throw new Error("FLUTTERWAVE_WEBHOOK_SECRET_HASH is required when unsigned webhooks are disabled");
        }
        const parsed = safeJsonParse(rawPayload);
        const type = asString(parsed.event) || asString(parsed.type);
        const dataObject = asRecord(parsed.data);
        const id = asString(parsed.id) ||
            asString(dataObject.id) ||
            asString(dataObject.flw_ref) ||
            asString(dataObject.tx_ref);
        if (!id || !type || Object.keys(dataObject).length === 0) {
            throw new Error("Invalid Flutterwave webhook payload");
        }
        return {
            id,
            type,
            dataObject,
        };
    }
    hasOauthCredentials() {
        return (this.options.clientId.trim().length > 0 &&
            this.options.clientSecret.trim().length > 0);
    }
    async createCustomer(input) {
        const payload = {
            name: {
                first: input.customer.firstName,
                last: input.customer.lastName,
            },
            email: input.customer.email,
        };
        const data = await this.providerRequest("/customers", "POST", payload, {
            "X-Idempotency-Key": input.idempotencyKey || input.reference,
            "X-Trace-Id": `${input.reference}-customer`,
        });
        const customer = asRecord(data.data);
        const id = asString(customer.id);
        if (!id) {
            throw new Error("Flutterwave customer response is incomplete");
        }
        return { id };
    }
    async providerRequest(path, method, body, extraHeaders) {
        const authorizationHeader = await this.resolveAuthorizationHeader();
        const normalizedBaseUrl = normalizeBaseUrl(this.options.baseUrl);
        const url = `${normalizedBaseUrl}${path}`;
        const headers = {
            Authorization: authorizationHeader,
            ...extraHeaders,
        };
        const requestInit = {
            method,
            headers,
        };
        if (body) {
            headers["Content-Type"] = "application/json";
            requestInit.body = JSON.stringify(body);
        }
        const response = await fetch(url, requestInit);
        const rawBody = await response.text();
        const parsed = safeJsonParse(rawBody);
        if (!response.ok) {
            const message = asString(asRecord(parsed.error).message) ||
                asString(asRecord(parsed.error).type) ||
                asString(parsed.message) ||
                `Flutterwave request failed (${response.status})`;
            throw new Error(message);
        }
        return parsed;
    }
    async resolveAuthorizationHeader() {
        if (!this.hasOauthCredentials()) {
            return `Bearer ${this.options.secretKey.trim()}`;
        }
        const now = Date.now();
        if (this.tokenCache && this.tokenCache.expiresAtMs > now + 30_000) {
            return `Bearer ${this.tokenCache.value}`;
        }
        const tokenPayload = new URLSearchParams();
        tokenPayload.set("grant_type", "client_credentials");
        tokenPayload.set("client_id", this.options.clientId.trim());
        tokenPayload.set("client_secret", this.options.clientSecret.trim());
        const idpUrl = `${normalizeBaseUrl(this.options.idpBaseUrl)}/oauth2/token`;
        const tokenResponse = await fetch(idpUrl, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: tokenPayload.toString(),
        });
        const rawBody = await tokenResponse.text();
        const parsed = safeJsonParse(rawBody);
        if (!tokenResponse.ok) {
            const message = asString(asRecord(parsed.error).message) ||
                asString(parsed.message) ||
                `Flutterwave auth failed (${tokenResponse.status})`;
            throw new Error(message);
        }
        const accessToken = asString(parsed.access_token) ||
            asString(asRecord(parsed.data).access_token);
        if (!accessToken) {
            throw new Error("Flutterwave auth response is missing access_token");
        }
        const expiresInSeconds = asNumber(parsed.expires_in) || asNumber(asRecord(parsed.data).expires_in) || 600;
        this.tokenCache = {
            value: accessToken,
            expiresAtMs: now + expiresInSeconds * 1000,
        };
        return `Bearer ${accessToken}`;
    }
    mapTransaction(data) {
        const transactionId = asString(data.id) || asString(data.flw_ref) || asString(data.transaction_id);
        const txRef = asString(data.reference) || asString(data.tx_ref);
        const status = asString(data.status);
        const currency = asString(data.currency).toUpperCase();
        const amountMajor = asNumber(data.amount);
        const amountMinor = Math.round(amountMajor * 100);
        if (!transactionId || !txRef || !status || !currency || amountMinor <= 0) {
            throw new Error("Flutterwave charge response is incomplete");
        }
        return {
            transactionId,
            txRef,
            status,
            amountMinor,
            currency,
        };
    }
}
function normalizeBaseUrl(raw) {
    const trimmed = raw.trim();
    if (!trimmed) {
        return "https://api.flutterwave.cloud";
    }
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        return trimmed.replace(/\/+$/, "");
    }
    return `https://${trimmed.replace(/\/+$/, "")}`;
}
function verifyFlutterwaveHmac(rawPayload, signatureHeader, secretHash) {
    const expectedHex = createHmac("sha256", secretHash.trim())
        .update(rawPayload, "utf8")
        .digest("hex");
    const expectedBase64 = createHmac("sha256", secretHash.trim())
        .update(rawPayload, "utf8")
        .digest("base64");
    return (safeCompare(signatureHeader, expectedHex) ||
        safeCompare(signatureHeader, expectedBase64));
}
function safeCompare(left, right) {
    const a = Buffer.from(left.trim());
    const b = Buffer.from(right.trim());
    if (a.length !== b.length) {
        return false;
    }
    return timingSafeEqual(a, b);
}
function safeJsonParse(raw) {
    try {
        const parsed = JSON.parse(raw);
        if (!parsed || typeof parsed !== "object") {
            return {};
        }
        return parsed;
    }
    catch {
        return {};
    }
}
function asRecord(raw) {
    return raw && typeof raw === "object" ? raw : {};
}
function asArray(raw) {
    return Array.isArray(raw) ? raw : [];
}
function asString(raw) {
    return typeof raw === "string" ? raw.trim() : "";
}
function asNumber(raw) {
    if (typeof raw === "number" && Number.isFinite(raw)) {
        return raw;
    }
    if (typeof raw === "string") {
        const parsed = Number(raw.trim());
        if (Number.isFinite(parsed)) {
            return parsed;
        }
    }
    return 0;
}
function parseDateToMs(raw) {
    if (!raw)
        return null;
    const parsed = Date.parse(raw);
    return Number.isFinite(parsed) ? parsed : null;
}
function roundMajorFromMinor(amountMinor) {
    return Math.round((amountMinor / 100 + Number.EPSILON) * 100) / 100;
}
//# sourceMappingURL=flutterwavePaymentsService.js.map