import { ConsoleMailer } from "../services/consoleMailer.js";
import { ResendMailer } from "../services/resendMailer.js";
import { RESEND_API_KEY, MAIL_FROM, VERIFY_URL, SEND_REAL_EMAILS, } from "./params.js";
function parseFirebaseConfig() {
    const raw = (process.env.FIREBASE_CONFIG || "").trim();
    if (!raw || !raw.startsWith("{")) {
        return {};
    }
    try {
        const parsed = JSON.parse(raw);
        return typeof parsed === "object" && parsed !== null ? parsed : {};
    }
    catch {
        return {};
    }
}
function stringValue(value) {
    return typeof value === "string" ? value.trim() : "";
}
function csvSet(name, lower = false) {
    const value = process.env[name];
    if (!value)
        return new Set();
    return new Set(value
        .split(",")
        .map((item) => item.trim())
        .filter(Boolean)
        .map((item) => (lower ? item.toLowerCase() : item)));
}
function compileRegexList(name) {
    const patterns = (process.env[name] || "").trim();
    if (!patterns)
        return [];
    return patterns
        .split(",")
        .map((p) => p.trim())
        .filter(Boolean)
        .map((p) => new RegExp(p, "i"));
}
function readBoolean(name, fallback) {
    const value = process.env[name];
    if (value === undefined)
        return fallback;
    return value.toLowerCase() === "true";
}
function readNumber(name, fallback) {
    const raw = process.env[name];
    if (!raw)
        return fallback;
    const parsed = Number(raw);
    return Number.isFinite(parsed) ? parsed : fallback;
}
function normalizeAndroidPasskeyOrigin(raw) {
    const trimmed = raw.trim();
    if (!trimmed)
        return "";
    if (trimmed.startsWith("android:apk-key-hash:")) {
        return trimmed;
    }
    return `android:apk-key-hash:${trimmed}`;
}
export function loadConfig() {
    const firebaseConfig = parseFirebaseConfig();
    const firebaseProjectId = stringValue(firebaseConfig.projectId);
    const firebaseStorageBucket = stringValue(firebaseConfig.storageBucket);
    const projectId = process.env.GOOGLE_CLOUD_PROJECT ||
        process.env.GCLOUD_PROJECT ||
        process.env.GCP_PROJECT ||
        process.env.FIREBASE_PROJECT ||
        firebaseProjectId ||
        "";
    const configuredStorageBucket = stringValue(process.env.STORAGE_BUCKET);
    const storageBucket = configuredStorageBucket ||
        stringValue(process.env.FIREBASE_STORAGE_BUCKET) ||
        firebaseStorageBucket ||
        (projectId ? `${projectId}.appspot.com` : "");
    const isProduction = (process.env.NODE_ENV || "").toLowerCase() === "production";
    const configuredAppVerdicts = csvSet("PLAY_INTEGRITY_ALLOWED_APP_VERDICTS", true);
    const configuredDeviceVerdicts = csvSet("PLAY_INTEGRITY_ALLOWED_DEVICE_VERDICTS", true);
    const defaultAppVerdicts = new Set(["play_recognized"]);
    const defaultDeviceVerdicts = new Set([
        "meets_device_integrity",
        "meets_strong_integrity",
    ]);
    const configuredTopupCurrencies = csvSet("STRIPE_ALLOWED_TOPUP_CURRENCIES");
    const stripeAllowedTopupCurrencies = new Set((configuredTopupCurrencies.size > 0 ?
        Array.from(configuredTopupCurrencies) :
        ["GBP", "EUR", "USD"]).map((currency) => currency.toUpperCase()));
    const configuredFlutterwaveTopupCurrencies = csvSet("FLUTTERWAVE_ALLOWED_TOPUP_CURRENCIES");
    const flutterwaveAllowedTopupCurrencies = new Set((configuredFlutterwaveTopupCurrencies.size > 0 ?
        Array.from(configuredFlutterwaveTopupCurrencies) :
        ["NGN"]).map((currency) => currency.toUpperCase()));
    const passkeyRpId = (process.env.PASSKEY_RP_ID || "").trim();
    const configuredPasskeyOrigins = csvSet("PASSKEY_EXPECTED_ORIGINS");
    const configuredAndroidPasskeyOrigins = csvSet("PASSKEY_ANDROID_EXPECTED_ORIGINS");
    const configuredAndroidApkKeyHashes = csvSet("PASSKEY_ANDROID_APK_KEY_HASHES");
    const androidPasskeyOrigins = new Set([
        ...Array.from(configuredAndroidPasskeyOrigins).map(normalizeAndroidPasskeyOrigin),
        ...Array.from(configuredAndroidApkKeyHashes).map(normalizeAndroidPasskeyOrigin),
    ].filter(Boolean));
    const passkeyExpectedOrigins = configuredPasskeyOrigins.size > 0 || androidPasskeyOrigins.size > 0 ?
        new Set([
            ...Array.from(configuredPasskeyOrigins),
            ...Array.from(androidPasskeyOrigins),
        ]) :
        passkeyRpId ?
            new Set([`https://${passkeyRpId}`]) :
            new Set();
    const passkeyEnabled = passkeyRpId.length > 0 && passkeyExpectedOrigins.size > 0;
    let cachedMailer = null;
    return {
        projectId,
        storageBucket,
        playIntegrityPackageName: (process.env.PLAY_INTEGRITY_PACKAGE_NAME || "").trim(),
        playIntegrityAllowFallback: readBoolean("PLAY_INTEGRITY_ALLOW_FALLBACK", !isProduction),
        playIntegrityMaxAgeMs: readNumber("PLAY_INTEGRITY_MAX_AGE_MS", 5 * 60 * 1000),
        playIntegrityAllowedAppVerdicts: configuredAppVerdicts.size > 0 ?
            configuredAppVerdicts :
            defaultAppVerdicts,
        playIntegrityAllowedDeviceVerdicts: configuredDeviceVerdicts.size > 0 ?
            configuredDeviceVerdicts :
            defaultDeviceVerdicts,
        playIntegrityRequireLicensed: readBoolean("PLAY_INTEGRITY_REQUIRE_LICENSED", isProduction),
        identityKmsKeyName: (process.env.IDENTITY_UPLOAD_KMS_KEY_NAME || "").trim(),
        identityMaxPayloadBytes: readNumber("IDENTITY_UPLOAD_MAX_PAYLOAD_BYTES", 15 * 1024 * 1024),
        identityOcrEnabled: readBoolean("IDENTITY_OCR_ENABLED", true),
        identityOcrAllowPayloadFallback: readBoolean("IDENTITY_OCR_ALLOW_PAYLOAD_FALLBACK", !isProduction),
        passkeyEnabled,
        passkeyRpId,
        passkeyRpName: (process.env.PASSKEY_RP_NAME || "PaySmart").trim(),
        passkeyExpectedOrigins,
        passkeyChallengeTtlMs: readNumber("PASSKEY_CHALLENGE_TTL_MS", 5 * 60 * 1000),
        exchangeRateApiKey: (process.env.EXCHANGE_RATE_API_KEY || "").trim(),
        exchangeRateCacheTtlMs: readNumber("FX_RATE_CACHE_TTL_MS", 60 * 1000),
        exchangeRateTimeoutMs: readNumber("FX_RATE_TIMEOUT_MS", 4 * 1000),
        exchangeRateUpstreamBaseUrl: (process.env.FX_RATE_UPSTREAM_BASE_URL ||
            "https://v6.exchangerate-api.com/v6").trim(),
        stripeSecretKey: (process.env.STRIPE_SECRET_KEY || process.env.STRIPE_API_KEY || "").trim(),
        stripeWebhookSecret: (process.env.STRIPE_WEBHOOK_SECRET || "").trim(),
        stripePublishableKey: (process.env.STRIPE_PUBLISHABLE_KEY ||
            process.env.STRIPE_PUBLIC_KEY ||
            "").trim(),
        stripeAllowUnsignedWebhooks: readBoolean("STRIPE_ALLOW_UNSIGNED_WEBHOOKS", !isProduction),
        stripeSuccessUrl: (process.env.STRIPE_SUCCESS_URL ||
            "https://pay-smart.net/add-money/success?session_id={CHECKOUT_SESSION_ID}").trim(),
        stripeCancelUrl: (process.env.STRIPE_CANCEL_URL ||
            "https://pay-smart.net/add-money/cancel").trim(),
        stripeAllowedTopupCurrencies,
        stripeMinimumTopupAmountMinor: readNumber("STRIPE_MINIMUM_TOPUP_AMOUNT_MINOR", 100),
        flutterwaveSecretKey: (process.env.FLUTTERWAVE_SECRET_KEY ||
            process.env.FLW_SECRET_KEY ||
            "").trim(),
        flutterwavePublicKey: (process.env.FLUTTERWAVE_PUBLIC_KEY ||
            process.env.FLW_PUBLIC_KEY ||
            "").trim(),
        flutterwaveWebhookSecretHash: (process.env.FLUTTERWAVE_WEBHOOK_SECRET_HASH ||
            process.env.FLW_WEBHOOK_SECRET_HASH ||
            "").trim(),
        flutterwaveAllowUnsignedWebhooks: readBoolean("FLUTTERWAVE_ALLOW_UNSIGNED_WEBHOOKS", !isProduction),
        flutterwaveBaseUrl: (process.env.FLUTTERWAVE_BASE_URL || "https://api.flutterwave.cloud").trim(),
        flutterwaveIdpBaseUrl: (process.env.FLUTTERWAVE_IDP_BASE_URL || "https://idp.flutterwave.com").trim(),
        flutterwaveClientId: (process.env.FLUTTERWAVE_CLIENT_ID || process.env.FLW_CLIENT_ID || "").trim(),
        flutterwaveClientSecret: (process.env.FLUTTERWAVE_CLIENT_SECRET || process.env.FLW_CLIENT_SECRET || "").trim(),
        flutterwaveVirtualAccountExpirySeconds: readNumber("FLUTTERWAVE_VIRTUAL_ACCOUNT_EXPIRY_SECONDS", 3600),
        flutterwaveAllowedTopupCurrencies,
        flutterwaveMinimumTopupAmountMinor: readNumber("FLUTTERWAVE_MINIMUM_TOPUP_AMOUNT_MINOR", 100),
        shouldSendRealEmails() {
            return SEND_REAL_EMAILS.value() === "true";
        },
        getVerifyUrl() {
            return VERIFY_URL.value();
        },
        getMailer() {
            if (cachedMailer)
                return cachedMailer;
            cachedMailer = this.shouldSendRealEmails()
                ? new ResendMailer(RESEND_API_KEY.value(), MAIL_FROM.value())
                : new ConsoleMailer();
            return cachedMailer;
        },
        allowedEmailDomains: csvSet("ALLOWED_EMAIL_DOMAINS", true),
        blockedEmailDomains: csvSet("BLOCKED_EMAIL_DOMAINS", true),
        blockedEmails: csvSet("BLOCKED_EMAILS", true),
        allowedTenants: csvSet("ALLOWED_TENANTS", true),
        allowedProviders: csvSet("ALLOWED_PROVIDERS", true),
        allowedAppIds: csvSet("ALLOWED_APP_IDS"),
        corsAllowedOrigins: csvSet("CORS_ALLOWED_ORIGINS"),
        appCheckRequired: process.env.APP_CHECK_REQUIRED === "true",
        disposablePatterns: compileRegexList("DISPOSABLE_PATTERNS"),
        port: Number(process.env.PORT || "8080"),
    };
}
//# sourceMappingURL=configuration.js.map