import { createHmac, timingSafeEqual } from "crypto";
export class FlutterwaveProviderRequestError extends Error {
    status;
    code;
    details;
    type;
    payload;
    constructor(message, status, code, details = [], type, payload = {}) {
        super(message);
        this.status = status;
        this.code = code;
        this.details = details;
        this.type = type;
        this.payload = payload;
        this.name = "FlutterwaveProviderRequestError";
    }
}
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
        const customerId = await this.resolveCustomerId(input);
        const amountMajor = roundMajorFromMinor(input.amountMinor);
        const payload = {
            reference: input.reference,
            customer_id: customerId,
            account_type: "dynamic",
            currency: input.currency,
            amount: amountMajor,
            expiry: this.options.virtualAccountExpirySeconds,
            narration: buildVirtualAccountNarration(input.customer),
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
        const expiresAtMs = parseDateToMs(asString(response.account_expiration_datetime)) ??
            createdAtMs + this.options.virtualAccountExpirySeconds * 1000;
        const virtualAccount = this.mapVirtualAccountDetails(response, input.reference, buildVirtualAccountNarration(input.customer));
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
            expiresAtMs,
            customerId,
            virtualAccount,
        };
    }
    async createPermanentFundingAccount(input) {
        this.ensureConfigured();
        const customerId = await this.resolveCustomerId(input);
        const payload = {
            reference: input.reference,
            customer_id: customerId,
            account_type: "static",
            amount: 0,
            currency: "NGN",
            narration: buildVirtualAccountNarration(input.customer),
            ...(input.kyc?.bvn ? { bvn: input.kyc.bvn } : {}),
            ...(input.kyc?.nin ? { nin: input.kyc.nin } : {}),
        };
        const data = await this.providerRequest("/virtual-accounts", "POST", payload, {
            "X-Idempotency-Key": input.idempotencyKey || input.reference,
            "X-Trace-Id": `${input.reference}-static-account`,
        });
        return this.mapPermanentFundingAccount(data, customerId, input.reference);
    }
    async retrievePermanentFundingAccount(accountId) {
        this.ensureConfigured();
        const cleanAccountId = accountId.trim();
        if (!cleanAccountId) {
            throw new Error("Missing Flutterwave funding account id");
        }
        const data = await this.providerRequest(`/virtual-accounts/${encodeURIComponent(cleanAccountId)}`, "GET");
        return this.mapPermanentFundingAccount(data);
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
    async resolveCustomerId(input) {
        const existingCustomerId = input.customerId?.trim();
        if (existingCustomerId) {
            return existingCustomerId;
        }
        const customer = await this.createCustomer(input);
        return customer.id;
    }
    async createCustomer(input) {
        const payload = {
            name: {
                first: input.customer.firstName,
                last: input.customer.lastName,
            },
            email: input.customer.email,
        };
        try {
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
        catch (error) {
            if (this.isExistingCustomerConflict(error)) {
                const existingCustomerId = await this.findExistingCustomerIdByEmail(input.customer.email, error.payload);
                if (existingCustomerId) {
                    return { id: existingCustomerId };
                }
            }
            throw error;
        }
    }
    mapPermanentFundingAccount(payload, fallbackCustomerId, fallbackReference) {
        const response = resolveFlutterwaveData(payload);
        const bank = asRecord(response.bank);
        const accountId = asString(response.id) || asString(response.account_id);
        const accountNumber = asString(response.account_number) || asString(response.accountNumber);
        const bankName = asString(response.account_bank_name) ||
            asString(response.bank_name) ||
            asString(bank.name) ||
            asString(bank.bank_name);
        const accountName = asString(response.account_name) ||
            asString(response.accountName) ||
            asString(response.name);
        const reference = asString(response.reference) ||
            asString(response.tx_ref) ||
            fallbackReference ||
            "";
        const status = asString(response.status) || "pending";
        const customerId = asString(response.customer_id) ||
            asString(asRecord(response.customer).id) ||
            fallbackCustomerId ||
            "";
        const note = asString(response.note) ||
            asString(response.narration) ||
            asString(response.description) ||
            undefined;
        const createdAtMs = parseDateToMs(asString(response.created_datetime)) ??
            parseDateToMs(asString(response.createdAt)) ??
            Date.now();
        const updatedAtMs = parseDateToMs(asString(response.updated_datetime)) ??
            parseDateToMs(asString(response.updatedAt)) ??
            createdAtMs;
        if (!accountId ||
            !accountNumber ||
            !bankName ||
            !accountName ||
            !reference ||
            !customerId) {
            throw new Error("Flutterwave permanent funding account response is incomplete");
        }
        return {
            accountId,
            accountNumber,
            bankName,
            accountName,
            reference,
            status,
            customerId,
            note,
            createdAtMs,
            updatedAtMs,
        };
    }
    mapVirtualAccountDetails(response, fallbackReference, fallbackAccountName) {
        const bank = asRecord(response.bank);
        const accountNumber = asString(response.account_number) || asString(response.accountNumber);
        const bankName = asString(response.account_bank_name) ||
            asString(response.bank_name) ||
            asString(bank.name) ||
            asString(bank.bank_name);
        const accountName = asString(response.account_name) ||
            asString(response.accountName) ||
            asString(response.name) ||
            fallbackAccountName;
        const reference = asString(response.reference) ||
            asString(response.tx_ref) ||
            fallbackReference;
        const note = asString(response.note) ||
            asString(response.narration) ||
            asString(response.description) ||
            undefined;
        if (!accountNumber || !bankName || !reference) {
            throw new Error("Flutterwave virtual account response is incomplete");
        }
        return {
            accountNumber,
            bankName,
            accountName,
            reference,
            note,
        };
    }
    isExistingCustomerConflict(error) {
        return (error instanceof FlutterwaveProviderRequestError &&
            error.status === 409 &&
            (error.code === "10409" ||
                error.type === "RESOURCE_CONFLICT" ||
                error.message.toLowerCase().includes("already exists")));
    }
    async findExistingCustomerIdByEmail(email, conflictPayload) {
        const cleanEmail = email.trim().toLowerCase();
        if (!cleanEmail) {
            return null;
        }
        const fromConflictPayload = extractCustomerIdFromLookupResponse(conflictPayload ?? {}, cleanEmail);
        if (fromConflictPayload) {
            return fromConflictPayload;
        }
        const directLookup = await this.tryLookupCustomerId(`/customers?email=${encodeURIComponent(cleanEmail)}`, "GET", undefined, cleanEmail);
        if (directLookup) {
            return directLookup;
        }
        const searchLookup = await this.tryLookupCustomerId("/customers/search", "POST", { email: cleanEmail }, cleanEmail);
        if (searchLookup) {
            return searchLookup;
        }
        return this.tryLookupCustomerId("/customers", "GET", undefined, cleanEmail);
    }
    async tryLookupCustomerId(path, method, body, email) {
        try {
            const response = await this.providerRequest(path, method, body);
            return extractCustomerIdFromLookupResponse(response, email);
        }
        catch {
            return null;
        }
    }
    async providerRequest(path, method, body, extraHeaders) {
        const authorizationHeader = await this.resolveAuthorizationHeader();
        const normalizedBaseUrl = normalizeBaseUrl(this.options.baseUrl);
        const url = `${normalizedBaseUrl}${path}`;
        const headers = {
            Authorization: authorizationHeader,
            Accept: "application/json",
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
            const providerError = asRecord(parsed.error);
            const details = extractFlutterwaveValidationDetails(parsed.validation_errors, providerError.validation_errors, parsed.errors, providerError.errors);
            const baseMessage = asString(providerError.message) ||
                asString(providerError.type) ||
                asString(parsed.message) ||
                asString(parsed.error_description) ||
                `Flutterwave request failed (${response.status})`;
            const message = details.length > 0 ? `${baseMessage}: ${details.join("; ")}` : baseMessage;
            const code = asString(parsed.code) ||
                asString(providerError.code) ||
                undefined;
            const type = asString(providerError.type) ||
                asString(parsed.type) ||
                undefined;
            throw new FlutterwaveProviderRequestError(message, response.status, code, details, type, parsed);
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
        const idpUrl = buildFlutterwaveOauthTokenUrl(this.options.idpBaseUrl);
        const tokenResponse = await fetch(idpUrl, {
            method: "POST",
            headers: {
                Accept: "application/json",
                "Content-Type": "application/x-www-form-urlencoded",
            },
            body: tokenPayload.toString(),
        });
        const rawBody = await tokenResponse.text();
        const parsed = safeJsonParse(rawBody);
        if (!tokenResponse.ok) {
            const message = asString(asRecord(parsed.error).message) ||
                asString(parsed.error_description) ||
                asString(asRecord(parsed.error).error_description) ||
                asString(parsed.message) ||
                asString(parsed.error) ||
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
function extractCustomerIdFromLookupResponse(response, email) {
    const normalizedEmail = email.trim().toLowerCase();
    for (const customer of collectCustomerRecords(response)) {
        const id = asString(customer.id) || asString(customer.customer_id);
        const customerEmail = asString(customer.email).toLowerCase();
        if (!id) {
            continue;
        }
        if (!normalizedEmail || !customerEmail || customerEmail === normalizedEmail) {
            return id;
        }
    }
    return null;
}
function resolveFlutterwaveData(payload) {
    const data = asRecord(payload.data);
    return Object.keys(data).length > 0 ? data : payload;
}
function collectCustomerRecords(response) {
    const data = response.data;
    const candidates = [];
    const direct = asRecord(data);
    if (Object.keys(direct).length > 0) {
        candidates.push(direct);
    }
    for (const row of asArray(data)) {
        const candidate = asRecord(row);
        if (Object.keys(candidate).length > 0) {
            candidates.push(candidate);
        }
    }
    const nestedCollections = [
        asArray(direct.data),
        asArray(direct.records),
        asArray(direct.customers),
        asArray(response.customers),
    ];
    for (const rows of nestedCollections) {
        for (const row of rows) {
            const candidate = asRecord(row);
            if (Object.keys(candidate).length > 0) {
                candidates.push(candidate);
            }
        }
    }
    return candidates;
}
function normalizeBaseUrl(raw) {
    const trimmed = raw.trim();
    if (!trimmed) {
        return "https://developersandbox-api.flutterwave.com";
    }
    if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        return trimmed.replace(/\/+$/, "");
    }
    return `https://${trimmed.replace(/\/+$/, "")}`;
}
export function buildFlutterwaveOauthTokenUrl(rawBaseUrl) {
    const normalized = rawBaseUrl.trim()
        ? normalizeBaseUrl(rawBaseUrl)
        : "https://idp.flutterwave.com";
    if (normalized.endsWith("/realms/flutterwave/protocol/openid-connect/token")) {
        return normalized;
    }
    if (normalized.endsWith("/realms/flutterwave/protocol/openid-connect")) {
        return `${normalized}/token`;
    }
    if (normalized.endsWith("/oauth2/token")) {
        return normalized.replace(/\/oauth2\/token$/, "/realms/flutterwave/protocol/openid-connect/token");
    }
    return `${normalized}/realms/flutterwave/protocol/openid-connect/token`;
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
function buildVirtualAccountNarration(customer) {
    const fullName = [customer.firstName, customer.lastName]
        .map((part) => part.trim())
        .filter(Boolean)
        .join(" ")
        .replace(/\s+/g, " ");
    const fallback = "PaySmart User";
    const narration = fullName || fallback;
    return narration.slice(0, 35);
}
function extractFlutterwaveValidationDetails(...sources) {
    const details = new Set();
    for (const source of sources) {
        collectValidationDetails(source, details);
    }
    return Array.from(details);
}
function collectValidationDetails(source, out) {
    if (!source) {
        return;
    }
    if (Array.isArray(source)) {
        for (const item of source) {
            collectValidationDetails(item, out);
        }
        return;
    }
    if (typeof source === "string") {
        const value = source.trim();
        if (value) {
            out.add(value);
        }
        return;
    }
    if (typeof source !== "object") {
        return;
    }
    const record = source;
    const field = asString(record.field) ||
        asString(record.field_name) ||
        asString(record.property);
    const message = asString(record.message) ||
        asString(record.error) ||
        asString(record.description);
    if (field || message) {
        out.add(field && message ? `${field}: ${message}` : field || message);
        return;
    }
    for (const [key, value] of Object.entries(record)) {
        if (Array.isArray(value)) {
            for (const item of value) {
                const nested = formatValidationDetail(item, key);
                if (nested) {
                    out.add(nested);
                }
            }
            continue;
        }
        const nested = formatValidationDetail(value, key);
        if (nested) {
            out.add(nested);
        }
    }
}
function formatValidationDetail(value, fallbackField) {
    if (typeof value === "string") {
        const message = value.trim();
        if (!message) {
            return "";
        }
        return fallbackField ? `${fallbackField}: ${message}` : message;
    }
    if (!value || typeof value !== "object") {
        return "";
    }
    const record = value;
    const field = asString(record.field) ||
        asString(record.field_name) ||
        asString(record.property) ||
        fallbackField ||
        "";
    const message = asString(record.message) ||
        asString(record.error) ||
        asString(record.description);
    if (!field && !message) {
        return "";
    }
    return field && message ? `${field}: ${message}` : field || message;
}
//# sourceMappingURL=flutterwavePaymentsService.js.map