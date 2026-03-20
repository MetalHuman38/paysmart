import { createHmac, timingSafeEqual } from "crypto";
const STRIPE_API_BASE = "https://api.stripe.com/v1";
export class StripePaymentsService {
    options;
    static EPHEMERAL_KEY_API_VERSION = "2020-03-02";
    constructor(options) {
        this.options = options;
    }
    ensureConfigured() {
        if (!this.options.secretKey) {
            throw new Error("STRIPE_SECRET_KEY is not configured");
        }
        if (this.options.secretKey.startsWith("pk_")) {
            throw new Error("STRIPE_SECRET_KEY must be a secret key (sk_ or rk_), not a publishable key (pk_)");
        }
    }
    async createTopupCheckoutSession(input) {
        this.ensureConfigured();
        const payload = new URLSearchParams();
        payload.set("mode", "payment");
        payload.set("success_url", input.successUrl);
        payload.set("cancel_url", input.cancelUrl);
        payload.set("client_reference_id", input.uid);
        payload.set("line_items[0][price_data][currency]", input.currency.toLowerCase());
        payload.set("line_items[0][price_data][product_data][name]", "PaySmart wallet top up");
        payload.set("line_items[0][price_data][product_data][description]", `Top up ${input.currency}`);
        payload.set("line_items[0][price_data][unit_amount]", String(input.amountMinor));
        payload.set("line_items[0][quantity]", "1");
        payload.set("metadata[uid]", input.uid);
        payload.set("metadata[purpose]", "wallet_topup");
        payload.set("metadata[currency]", input.currency);
        payload.set("metadata[amountMinor]", String(input.amountMinor));
        payload.set("payment_intent_data[metadata][uid]", input.uid);
        payload.set("payment_intent_data[metadata][purpose]", "wallet_topup");
        payload.set("payment_intent_data[metadata][currency]", input.currency);
        payload.set("payment_intent_data[metadata][amountMinor]", String(input.amountMinor));
        payload.append("expand[]", "payment_intent");
        const headers = {};
        if (input.idempotencyKey) {
            headers["Idempotency-Key"] = input.idempotencyKey;
        }
        const data = await this.stripeRequest("/checkout/sessions", "POST", payload, headers);
        return this.mapCheckoutSession(data);
    }
    async createTopupPaymentIntent(input) {
        this.ensureConfigured();
        const payload = new URLSearchParams();
        payload.set("amount", String(input.amountMinor));
        payload.set("currency", input.currency.toLowerCase());
        payload.set("description", `PaySmart wallet top up (${input.currency})`);
        payload.set("automatic_payment_methods[enabled]", "true");
        if (input.customerId?.trim()) {
            payload.set("customer", input.customerId.trim());
        }
        if (input.setupFutureUsage) {
            payload.set("setup_future_usage", input.setupFutureUsage);
        }
        payload.set("metadata[uid]", input.uid);
        payload.set("metadata[purpose]", "wallet_topup");
        payload.set("metadata[currency]", input.currency);
        payload.set("metadata[amountMinor]", String(input.amountMinor));
        const headers = {};
        if (input.idempotencyKey) {
            headers["Idempotency-Key"] = input.idempotencyKey;
        }
        const data = await this.stripeRequest("/payment_intents", "POST", payload, headers);
        return this.mapPaymentIntent(data);
    }
    async retrieveCheckoutSession(sessionId) {
        this.ensureConfigured();
        const escaped = encodeURIComponent(sessionId.trim());
        const data = await this.stripeRequest(`/checkout/sessions/${escaped}?expand%5B%5D=payment_intent`, "GET");
        return this.mapCheckoutSession(data);
    }
    async retrievePaymentIntent(paymentIntentId) {
        this.ensureConfigured();
        const escaped = encodeURIComponent(paymentIntentId.trim());
        const data = await this.stripeRequest(`/payment_intents/${escaped}?expand%5B%5D=payment_method`, "GET");
        return this.mapPaymentIntent(data);
    }
    async ensureCustomer(input) {
        this.ensureConfigured();
        const existingCustomerId = input.existingCustomerId?.trim();
        if (existingCustomerId) {
            try {
                return await this.retrieveCustomer(existingCustomerId);
            }
            catch (error) {
                const message = error instanceof Error ? error.message : "";
                if (!message.includes("No such customer")) {
                    throw error;
                }
            }
        }
        const payload = new URLSearchParams();
        payload.set("email", input.email.trim().toLowerCase());
        if (input.name?.trim()) {
            payload.set("name", input.name.trim());
        }
        payload.set("metadata[uid]", input.uid);
        const data = await this.stripeRequest("/customers", "POST", payload);
        return this.mapCustomer(data);
    }
    async retrieveCustomer(customerId) {
        this.ensureConfigured();
        const escaped = encodeURIComponent(customerId.trim());
        const data = await this.stripeRequest(`/customers/${escaped}`, "GET");
        return this.mapCustomer(data);
    }
    async createEphemeralKey(customerId) {
        this.ensureConfigured();
        const payload = new URLSearchParams();
        payload.set("customer", customerId.trim());
        const data = await this.stripeRequest("/ephemeral_keys", "POST", payload, { "Stripe-Version": StripePaymentsService.EPHEMERAL_KEY_API_VERSION });
        const secret = asString(data.secret);
        if (!secret) {
            throw new Error("Stripe ephemeral key response is incomplete");
        }
        return { secret };
    }
    async listCustomerCardPaymentMethods(customerId) {
        this.ensureConfigured();
        const escaped = encodeURIComponent(customerId.trim());
        const data = await this.stripeRequest(`/payment_methods?customer=${escaped}&type=card`, "GET");
        const items = asArray(data.data);
        return items.map((entry) => this.mapCustomerCardPaymentMethod(asRecord(entry)));
    }
    async detachPaymentMethod(paymentMethodId) {
        this.ensureConfigured();
        const escaped = encodeURIComponent(paymentMethodId.trim());
        await this.stripeRequest(`/payment_methods/${escaped}/detach`, "POST", new URLSearchParams());
    }
    async setDefaultPaymentMethod(customerId, paymentMethodId) {
        this.ensureConfigured();
        const escaped = encodeURIComponent(customerId.trim());
        const payload = new URLSearchParams();
        payload.set("invoice_settings[default_payment_method]", paymentMethodId?.trim() || "");
        await this.stripeRequest(`/customers/${escaped}`, "POST", payload);
    }
    parseWebhookEvent(rawPayload, signatureHeader) {
        this.ensureConfigured();
        if (this.options.webhookSigningSecret) {
            if (!signatureHeader) {
                throw new Error("Missing Stripe signature header");
            }
            if (!verifyStripeSignature(rawPayload, signatureHeader, this.options.webhookSigningSecret)) {
                throw new Error("Invalid Stripe signature");
            }
        }
        else if (!this.options.allowUnsignedWebhooks) {
            throw new Error("STRIPE_WEBHOOK_SECRET is required when unsigned webhooks are disabled");
        }
        const parsed = safeJsonParse(rawPayload);
        const id = asString(parsed.id);
        const type = asString(parsed.type);
        const data = asRecord(parsed.data);
        const dataObject = asRecord(data.object);
        if (!id || !type || Object.keys(dataObject).length === 0) {
            throw new Error("Invalid Stripe webhook payload");
        }
        return {
            id,
            type,
            dataObject,
        };
    }
    async stripeRequest(path, method, body, extraHeaders) {
        const url = `${STRIPE_API_BASE}${path}`;
        const headers = {
            Authorization: `Bearer ${this.options.secretKey}`,
            ...extraHeaders,
        };
        const requestInit = {
            method,
            headers,
        };
        if (body) {
            headers["Content-Type"] = "application/x-www-form-urlencoded";
            requestInit.body = body.toString();
        }
        const response = await fetch(url, requestInit);
        const rawBody = await response.text();
        const parsed = safeJsonParse(rawBody);
        if (!response.ok) {
            const message = asString(asRecord(parsed.error).message) ||
                asString(parsed.message) ||
                "Stripe request failed";
            throw new Error(message);
        }
        return parsed;
    }
    mapCheckoutSession(data) {
        const id = asString(data.id);
        const url = asString(data.url);
        const expiresAtSeconds = asNumber(data.expires_at);
        const status = asString(data.status);
        const paymentStatus = asString(data.payment_status);
        const amountTotalMinor = asNumber(data.amount_total);
        const currency = asString(data.currency).toUpperCase();
        const metadata = toStringMap(data.metadata);
        const paymentIntentId = resolvePaymentIntentId(data.payment_intent);
        if (!id || !url || !expiresAtSeconds || !currency) {
            throw new Error("Stripe checkout session response is incomplete");
        }
        return {
            id,
            url,
            expiresAtMs: expiresAtSeconds * 1000,
            status,
            paymentStatus,
            amountTotalMinor,
            currency,
            paymentIntentId,
            metadata,
        };
    }
    mapPaymentIntent(data) {
        const id = asString(data.id);
        const clientSecret = asString(data.client_secret) || undefined;
        const status = asString(data.status);
        const amountMinor = asNumber(data.amount);
        const currency = asString(data.currency).toUpperCase();
        const metadata = toStringMap(data.metadata);
        const createdAtSeconds = asNumber(data.created);
        const customerId = asString(data.customer) || undefined;
        const paymentMethodId = resolveId(data.payment_method);
        if (!id || !status || !currency || amountMinor <= 0) {
            throw new Error("Stripe payment intent response is incomplete");
        }
        return {
            id,
            clientSecret,
            status,
            amountMinor,
            currency,
            metadata,
            createdAtMs: createdAtSeconds > 0 ? createdAtSeconds * 1000 : Date.now(),
            customerId,
            paymentMethodId,
        };
    }
    mapCustomer(data) {
        const id = asString(data.id);
        if (!id) {
            throw new Error("Stripe customer response is incomplete");
        }
        const invoiceSettings = asRecord(data.invoice_settings);
        return {
            id,
            email: asString(data.email) || undefined,
            name: asString(data.name) || undefined,
            defaultPaymentMethodId: asString(invoiceSettings.default_payment_method) || undefined,
        };
    }
    mapCustomerCardPaymentMethod(data) {
        const card = asRecord(data.card);
        const id = asString(data.id);
        const last4 = asString(card.last4);
        const expMonth = asNumber(card.exp_month);
        const expYear = asNumber(card.exp_year);
        if (!id || !last4 || expMonth <= 0 || expYear <= 0) {
            throw new Error("Stripe payment method response is incomplete");
        }
        return {
            id,
            brand: asString(card.brand) || "card",
            last4,
            expMonth,
            expYear,
            funding: asString(card.funding) || undefined,
            country: asString(card.country) || undefined,
            fingerprint: asString(card.fingerprint) || undefined,
            createdAtMs: asNumber(data.created) > 0 ? asNumber(data.created) * 1000 : Date.now(),
        };
    }
}
function resolvePaymentIntentId(raw) {
    if (typeof raw === "string" && raw.trim().length > 0) {
        return raw.trim();
    }
    if (raw && typeof raw === "object") {
        const id = asString(raw.id);
        return id || undefined;
    }
    return undefined;
}
function resolveId(raw) {
    if (typeof raw === "string" && raw.trim().length > 0) {
        return raw.trim();
    }
    if (raw && typeof raw === "object") {
        const id = asString(raw.id);
        return id || undefined;
    }
    return undefined;
}
function verifyStripeSignature(rawPayload, signatureHeader, signingSecret) {
    const parts = signatureHeader
        .split(",")
        .map((entry) => entry.trim())
        .filter(Boolean);
    let timestamp = "";
    const signatures = [];
    for (const part of parts) {
        const [key, value] = part.split("=", 2);
        if (!key || !value)
            continue;
        if (key === "t") {
            timestamp = value;
        }
        if (key === "v1") {
            signatures.push(value);
        }
    }
    if (!timestamp || signatures.length === 0)
        return false;
    const signedPayload = `${timestamp}.${rawPayload}`;
    const expected = createHmac("sha256", signingSecret)
        .update(signedPayload, "utf8")
        .digest("hex");
    return signatures.some((candidate) => {
        const expectedBuf = Buffer.from(expected, "hex");
        const candidateBuf = Buffer.from(candidate, "hex");
        if (expectedBuf.length !== candidateBuf.length) {
            return false;
        }
        return timingSafeEqual(expectedBuf, candidateBuf);
    });
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
    return typeof raw === "string" ? raw : "";
}
function asNumber(raw) {
    if (typeof raw === "number" && Number.isFinite(raw)) {
        return raw;
    }
    if (typeof raw === "string") {
        const parsed = Number(raw);
        if (Number.isFinite(parsed)) {
            return parsed;
        }
    }
    return 0;
}
function toStringMap(raw) {
    const record = asRecord(raw);
    const out = {};
    for (const [key, value] of Object.entries(record)) {
        if (typeof value === "string") {
            out[key] = value;
        }
        else if (typeof value === "number" && Number.isFinite(value)) {
            out[key] = String(value);
        }
    }
    return out;
}
//# sourceMappingURL=stripePaymentsService.js.map