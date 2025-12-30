// src/config/config.ts
import { Mailer } from "../services/mailer.js";
import { ConsoleMailer } from "../services/consoleMailer.js";
import { ResendMailer } from "../services/resendMailer.js";
import {
  RESEND_API_KEY,
  MAIL_FROM,
  VERIFY_URL,
  SEND_REAL_EMAILS,
} from "./params";

export type Config = {
  projectId: string;
  storageBucket?: string;

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
    value.split(",").map((item) =>
      lower ? item.toLowerCase() : item.trim()
    )
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

export function loadConfig(): Config {
  const projectId =
    process.env.GOOGLE_CLOUD_PROJECT ||
    process.env.GCP_PROJECT ||
    process.env.FIREBASE_PROJECT ||
    "";

  let cachedMailer: Mailer | null = null;

  return {
    projectId,
    storageBucket: process.env.STORAGE_BUCKET,

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
