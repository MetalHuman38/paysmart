// src/config/config.ts
import { Mailer } from "../services/mailer.js";
import { ConsoleMailer } from "../services/consoleMailer.js";
import { ResendMailer } from "../services/resendMailer.js";
import {
  RESEND_API_KEY,
  MAIL_FROM,
  VERIFY_URL,
  SEND_REAL_EMAILS,
} from "./params.js";

export type Config = {
  projectId: string;
  storageBucket?: string;
  playIntegrityPackageName: string;
  playIntegrityAllowFallback: boolean;
  playIntegrityMaxAgeMs: number;
  playIntegrityAllowedAppVerdicts: Set<string>;
  playIntegrityAllowedDeviceVerdicts: Set<string>;
  playIntegrityRequireLicensed: boolean;
  identityKmsKeyName: string;
  identityMaxPayloadBytes: number;
  identityOcrEnabled: boolean;
  identityOcrAllowPayloadFallback: boolean;
  passkeyEnabled: boolean;
  passkeyRpId: string;
  passkeyRpName: string;
  passkeyExpectedOrigins: Set<string>;
  passkeyChallengeTtlMs: number;
  exchangeRateApiKey: string;
  exchangeRateCacheTtlMs: number;
  exchangeRateTimeoutMs: number;
  exchangeRateUpstreamBaseUrl: string;
  stripeSecretKey: string;
  stripeWebhookSecret: string;
  stripePublishableKey: string;
  stripeAllowUnsignedWebhooks: boolean;
  stripeSuccessUrl: string;
  stripeCancelUrl: string;
  stripeAllowedTopupCurrencies: Set<string>;
  stripeMinimumTopupAmountMinor: number;

  getMailer(): Mailer;
  getVerifyUrl(): string;
  shouldSendRealEmails(): boolean;

  allowedEmailDomains: Set<string>;
  blockedEmailDomains: Set<string>;
  blockedEmails: Set<string>;
  allowedTenants: Set<string>;
  allowedProviders: Set<string>;
  allowedAppIds: Set<string>;
  corsAllowedOrigins?: Set<string>;
  appCheckRequired: boolean;
  disposablePatterns: RegExp[];
  port: number;
};

function csvSet(name: string, lower = false): Set<string> {
  const value = process.env[name];
  if (!value) return new Set();
  return new Set(
    value
      .split(",")
      .map((item) => item.trim())
      .filter(Boolean)
      .map((item) => (lower ? item.toLowerCase() : item))
  );
}

function compileRegexList(name: string): RegExp[] {
  const patterns = (process.env[name] || "").trim();
  if (!patterns) return [];
  return patterns
    .split(",")
    .map((p) => p.trim())
    .filter(Boolean)
    .map((p) => new RegExp(p, "i"));
}

function readBoolean(name: string, fallback: boolean): boolean {
  const value = process.env[name];
  if (value === undefined) return fallback;
  return value.toLowerCase() === "true";
}

function readNumber(name: string, fallback: number): number {
  const raw = process.env[name];
  if (!raw) return fallback;

  const parsed = Number(raw);
  return Number.isFinite(parsed) ? parsed : fallback;
}

export function loadConfig(): Config {
  const projectId =
    process.env.GOOGLE_CLOUD_PROJECT ||
    process.env.GCP_PROJECT ||
    process.env.FIREBASE_PROJECT ||
    "";
  const isProduction =
    (process.env.NODE_ENV || "").toLowerCase() === "production";
  const configuredAppVerdicts = csvSet(
    "PLAY_INTEGRITY_ALLOWED_APP_VERDICTS",
    true
  );
  const configuredDeviceVerdicts = csvSet(
    "PLAY_INTEGRITY_ALLOWED_DEVICE_VERDICTS",
    true
  );
  const defaultAppVerdicts = new Set(["play_recognized"]);
  const defaultDeviceVerdicts = new Set([
    "meets_device_integrity",
    "meets_strong_integrity",
  ]);
  const configuredTopupCurrencies = csvSet("STRIPE_ALLOWED_TOPUP_CURRENCIES");
  const stripeAllowedTopupCurrencies = new Set(
    (configuredTopupCurrencies.size > 0 ?
      Array.from(configuredTopupCurrencies) :
      ["GBP", "EUR", "USD"]
    ).map((currency) => currency.toUpperCase())
  );
  const passkeyRpId = (process.env.PASSKEY_RP_ID || "").trim();
  const configuredPasskeyOrigins = csvSet("PASSKEY_EXPECTED_ORIGINS");
  const passkeyExpectedOrigins =
    configuredPasskeyOrigins.size > 0 ?
      configuredPasskeyOrigins :
      passkeyRpId ?
        new Set([`https://${passkeyRpId}`]) :
        new Set<string>();
  const passkeyEnabled = passkeyRpId.length > 0 && passkeyExpectedOrigins.size > 0;

  let cachedMailer: Mailer | null = null;

  return {
    projectId,
    storageBucket: process.env.STORAGE_BUCKET,
    playIntegrityPackageName: (process.env.PLAY_INTEGRITY_PACKAGE_NAME || "").trim(),
    playIntegrityAllowFallback: readBoolean(
      "PLAY_INTEGRITY_ALLOW_FALLBACK",
      !isProduction
    ),
    playIntegrityMaxAgeMs: readNumber("PLAY_INTEGRITY_MAX_AGE_MS", 5 * 60 * 1000),
    playIntegrityAllowedAppVerdicts:
      configuredAppVerdicts.size > 0 ?
        configuredAppVerdicts :
        defaultAppVerdicts,
    playIntegrityAllowedDeviceVerdicts:
      configuredDeviceVerdicts.size > 0 ?
        configuredDeviceVerdicts :
        defaultDeviceVerdicts,
    playIntegrityRequireLicensed: readBoolean(
      "PLAY_INTEGRITY_REQUIRE_LICENSED",
      isProduction
    ),
    identityKmsKeyName: (process.env.IDENTITY_UPLOAD_KMS_KEY_NAME || "").trim(),
    identityMaxPayloadBytes: readNumber(
      "IDENTITY_UPLOAD_MAX_PAYLOAD_BYTES",
      15 * 1024 * 1024
    ),
    identityOcrEnabled: readBoolean("IDENTITY_OCR_ENABLED", true),
    identityOcrAllowPayloadFallback: readBoolean(
      "IDENTITY_OCR_ALLOW_PAYLOAD_FALLBACK",
      !isProduction
    ),
    passkeyEnabled,
    passkeyRpId,
    passkeyRpName: (process.env.PASSKEY_RP_NAME || "PaySmart").trim(),
    passkeyExpectedOrigins,
    passkeyChallengeTtlMs: readNumber("PASSKEY_CHALLENGE_TTL_MS", 5 * 60 * 1000),
    exchangeRateApiKey: (process.env.EXCHANGE_RATE_API_KEY || "").trim(),
    exchangeRateCacheTtlMs: readNumber("FX_RATE_CACHE_TTL_MS", 60 * 1000),
    exchangeRateTimeoutMs: readNumber("FX_RATE_TIMEOUT_MS", 4 * 1000),
    exchangeRateUpstreamBaseUrl: (
      process.env.FX_RATE_UPSTREAM_BASE_URL ||
      "https://v6.exchangerate-api.com/v6"
    ).trim(),
    stripeSecretKey: (process.env.STRIPE_SECRET_KEY || process.env.STRIPE_API_KEY || "").trim(),
    stripeWebhookSecret: (process.env.STRIPE_WEBHOOK_SECRET || "").trim(),
    stripePublishableKey: (
      process.env.STRIPE_PUBLISHABLE_KEY ||
      process.env.STRIPE_PUBLIC_KEY ||
      ""
    ).trim(),
    stripeAllowUnsignedWebhooks: readBoolean(
      "STRIPE_ALLOW_UNSIGNED_WEBHOOKS",
      !isProduction
    ),
    stripeSuccessUrl: (
      process.env.STRIPE_SUCCESS_URL ||
      "https://pay-smart.net/add-money/success?session_id={CHECKOUT_SESSION_ID}"
    ).trim(),
    stripeCancelUrl: (
      process.env.STRIPE_CANCEL_URL ||
      "https://pay-smart.net/add-money/cancel"
    ).trim(),
    stripeAllowedTopupCurrencies,
    stripeMinimumTopupAmountMinor: readNumber(
      "STRIPE_MINIMUM_TOPUP_AMOUNT_MINOR",
      100
    ),

    shouldSendRealEmails() {
      return SEND_REAL_EMAILS.value() === "true";
    },

    getVerifyUrl() {
      return VERIFY_URL.value();
    },

    getMailer() {
      if (cachedMailer) return cachedMailer;

      cachedMailer = this.shouldSendRealEmails()
        ? new ResendMailer(
            RESEND_API_KEY.value(),
            MAIL_FROM.value()
          )
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

