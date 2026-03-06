import type { Request } from "express";
import { APP, SECURITY } from "../config/globals.js";

type PasskeyAuditEvent =
  | "signin_options_attempt"
  | "signin_options_success"
  | "signin_options_failure"
  | "signin_verify_attempt"
  | "signin_verify_success"
  | "signin_verify_failure"
  | "signin_rate_limited";

type PasskeyAuditPayload = {
  event: PasskeyAuditEvent;
  path: string;
  method: string;
  origin: string;
  userAgent: string;
  ip: string;
  appCheckVerdict: string;
  appId?: string;
  error?: string;
  code?: string;
  credentialId?: string;
  uid?: string;
};

export function logPasskeyAuditEvent(
  req: Request,
  event: PasskeyAuditEvent,
  extra: Partial<PasskeyAuditPayload> = {}
) {
  const payload: PasskeyAuditPayload = {
    event,
    path: req.path,
    method: req.method,
    origin: readHeader(req, "origin") || "none",
    userAgent: readHeader(req, "user-agent") || "unknown",
    ip: resolveClientIp(req),
    appCheckVerdict: resolveAppCheckVerdict(req),
    appId: req.appCheck?.app_id,
    ...extra,
  };

  console.info("[PasskeyAudit]", payload);
}

function resolveClientIp(req: Request): string {
  const forwarded = readHeader(req, "x-forwarded-for");
  if (forwarded) return forwarded.split(",")[0].trim();
  return (req.ip || "").trim() || "unknown";
}

function resolveAppCheckVerdict(req: Request): string {
  if (APP.emulator || !SECURITY.appCheckRequired) {
    return "bypassed";
  }
  if (req.appCheck?.app_id) {
    return "verified";
  }
  if (req.appCheck) {
    return "verified_without_app_id";
  }
  return "missing_or_invalid";
}

function readHeader(req: Request, name: string): string {
  const viaFn =
    typeof req.header === "function" ? req.header(name) : undefined;
  if (typeof viaFn === "string" && viaFn.trim()) {
    return viaFn.trim();
  }

  const rawHeaders = req.headers || {};
  const lower = name.toLowerCase();
  const value = rawHeaders[lower] ?? rawHeaders[name];
  if (Array.isArray(value)) {
    return (value[0] || "").toString().trim();
  }
  return (value || "").toString().trim();
}
