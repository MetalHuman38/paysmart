import { randomUUID } from "crypto";
import { FieldValue, Firestore, Timestamp } from "firebase-admin/firestore";
import {
  IdentityProviderCallbackInput,
  IdentityProviderSession,
  IdentityProviderSessionResume,
  IdentityProviderStatus,
  ResumeIdentityProviderSessionInput,
  StartIdentityProviderSessionInput,
} from "../../domain/model/identityProvider.js";
import { IdentityProviderRepository } from "../../domain/repository/IdentityProviderRepository.js";

const SESSION_TTL_MS = 30 * 60 * 1000;
const RAW_DEEP_LINK_MAX_LEN = 4000;

type IdentityProviderSessionDoc = {
  sessionId: string;
  uid: string;
  provider: string;
  status: IdentityProviderStatus;
  launchUrl?: string;
  expiresAtMs?: number;
  reason?: string | null;
  providerRef?: string | null;
  countryIso2?: string | null;
  documentType?: string | null;
  lastEvent?: string | null;
  lastCallbackRawDeepLink?: string | null;
  callbackCount?: number;
  createdAtMs: number;
  updatedAtMs: number;
};

type EventStatus = {
  status: IdentityProviderStatus;
  reason?: string | null;
};

export class FirestoreIdentityProviderRepository
  implements IdentityProviderRepository
{
  constructor(private readonly firestore: Firestore) {}

  private providerName(): string {
    const configured = (process.env.IDENTITY_PROVIDER_NAME || "").trim();
    return configured || "third_party";
  }

  private sessionsCollection(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("identityProviderSessions");
  }

  private sessionRef(uid: string, sessionId: string) {
    return this.sessionsCollection(uid).doc(sessionId);
  }

  async startSession(
    uid: string,
    input: StartIdentityProviderSessionInput
  ): Promise<IdentityProviderSession> {
    const now = Date.now();
    const sessionId = randomUUID();
    const expiresAtMs = now + SESSION_TTL_MS;
    const provider = this.providerName();
    const launchUrl = resolveLaunchUrlTemplate(sessionId, provider);

    const doc: IdentityProviderSessionDoc = {
      sessionId,
      uid,
      provider,
      status: "session_created",
      launchUrl: launchUrl || undefined,
      expiresAtMs,
      reason: null,
      providerRef: null,
      countryIso2: normalizeCountryIso2(input.countryIso2),
      documentType: normalizeDocumentType(input.documentType),
      callbackCount: 0,
      createdAtMs: now,
      updatedAtMs: now,
    };

    await this.sessionRef(uid, sessionId).set({
      ...doc,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });

    return {
      sessionId,
      provider,
      status: "session_created",
      launchUrl: launchUrl || undefined,
      expiresAtMs,
    };
  }

  async resumeSession(
    uid: string,
    input: ResumeIdentityProviderSessionInput
  ): Promise<IdentityProviderSessionResume> {
    const sessionId = input.sessionId.trim();
    if (!sessionId) {
      throw new Error("Missing sessionId");
    }

    const ref = this.sessionRef(uid, sessionId);
    const snap = await ref.get();
    if (!snap.exists) {
      throw new Error("Provider session not found");
    }

    const data = snap.data() as IdentityProviderSessionDoc;
    if (
      typeof data.expiresAtMs === "number" &&
      Date.now() > data.expiresAtMs &&
      (data.status === "session_created" || data.status === "in_progress")
    ) {
      const now = Date.now();
      await ref.set(
        {
          status: "cancelled",
          reason: "session_expired",
          updatedAtMs: now,
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      return {
        sessionId: data.sessionId,
        provider: data.provider,
        status: "cancelled",
        launchUrl: data.launchUrl,
        reason: "session_expired",
        updatedAtMs: now,
      };
    }

    return toResume(data, snap.get("updatedAt"));
  }

  async submitCallback(
    uid: string,
    input: IdentityProviderCallbackInput
  ): Promise<IdentityProviderSessionResume> {
    const event = normalizeEvent(input.event);
    if (!event) {
      throw new Error("Missing event");
    }

    const fallbackSessionId = parseSessionIdFromDeepLink(input.rawDeepLink);
    const sessionId = (input.sessionId || fallbackSessionId || "").trim();
    if (!sessionId) {
      throw new Error("Missing sessionId");
    }

    const ref = this.sessionRef(uid, sessionId);
    const snap = await ref.get();
    if (!snap.exists) {
      throw new Error("Provider session not found");
    }

    const current = snap.data() as IdentityProviderSessionDoc;
    const next = resolveStatusTransition(current.status, event);
    const now = Date.now();
    const callbackCount = (current.callbackCount ?? 0) + 1;

    await ref.set(
      {
        status: next.status,
        reason: next.reason ?? null,
        providerRef: normalizeProviderRef(input.providerRef),
        lastEvent: event,
        lastCallbackRawDeepLink: normalizeRawDeepLink(input.rawDeepLink),
        callbackCount,
        updatedAtMs: now,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    return {
      sessionId: current.sessionId,
      provider: current.provider,
      status: next.status,
      launchUrl: current.launchUrl,
      reason: next.reason ?? undefined,
      updatedAtMs: now,
    };
  }
}

function resolveStatusTransition(
  currentStatus: IdentityProviderStatus,
  event: string
): EventStatus {
  const normalized = event.trim().toLowerCase();
  if (!normalized) {
    return { status: currentStatus };
  }

  if (
    normalized === "started" ||
    normalized === "open" ||
    normalized === "opened" ||
    normalized === "launched" ||
    normalized === "sdk_started"
  ) {
    return { status: "in_progress", reason: null };
  }

  if (
    normalized === "submitted" ||
    normalized === "complete" ||
    normalized === "completed" ||
    normalized === "review_pending"
  ) {
    return { status: "pending_review", reason: null };
  }

  if (
    normalized === "verified" ||
    normalized === "approved" ||
    normalized === "success"
  ) {
    return { status: "verified", reason: null };
  }

  if (
    normalized === "rejected" ||
    normalized === "declined" ||
    normalized === "failed" ||
    normalized === "error"
  ) {
    return {
      status: "rejected",
      reason: `provider_event_${normalized}`,
    };
  }

  if (
    normalized === "cancelled" ||
    normalized === "canceled" ||
    normalized === "aborted"
  ) {
    return { status: "cancelled", reason: "cancelled_by_user" };
  }

  return {
    status: currentStatus,
    reason:
      currentStatus === "rejected" || currentStatus === "cancelled" ?
        `provider_event_${normalized}` :
        null,
  };
}

function resolveLaunchUrlTemplate(
  sessionId: string,
  provider: string
): string | null {
  const template = (process.env.IDENTITY_PROVIDER_LAUNCH_URL_TEMPLATE || "").trim();
  if (!template) {
    return null;
  }

  return template
    .replaceAll("{sessionId}", encodeURIComponent(sessionId))
    .replaceAll("{provider}", encodeURIComponent(provider));
}

function normalizeCountryIso2(value: string | undefined): string | null {
  if (!value) return null;
  const trimmed = value.trim().toUpperCase();
  return trimmed.length === 2 ? trimmed : null;
}

function normalizeDocumentType(value: string | undefined): string | null {
  if (!value) return null;
  const normalized = value.trim().toLowerCase().replace(/\s+/g, "_");
  return normalized.length > 0 ? normalized : null;
}

function normalizeProviderRef(value: string | undefined): string | null {
  if (!value) return null;
  const trimmed = value.trim();
  return trimmed.length > 0 ? trimmed.slice(0, 512) : null;
}

function normalizeRawDeepLink(value: string | undefined): string | null {
  if (!value) return null;
  const trimmed = value.trim();
  if (!trimmed) return null;
  return trimmed.slice(0, RAW_DEEP_LINK_MAX_LEN);
}

function normalizeEvent(event: string): string {
  return event.trim().toLowerCase();
}

function parseSessionIdFromDeepLink(rawDeepLink: string | undefined): string | null {
  if (!rawDeepLink) return null;
  const trimmed = rawDeepLink.trim();
  if (!trimmed) return null;
  return (
    runCatching(() => {
      const url = new URL(trimmed);
      return url.searchParams.get("sessionId");
    }) ?? null
  );
}

function runCatching<T>(fn: () => T): T | null {
  try {
    return fn();
  } catch {
    return null;
  }
}

function toResume(
  data: IdentityProviderSessionDoc,
  updatedAtField: unknown
): IdentityProviderSessionResume {
  const updatedAtMs =
    typeof data.updatedAtMs === "number" ?
      data.updatedAtMs :
      timestampMillis(updatedAtField);

  return {
    sessionId: data.sessionId,
    provider: data.provider,
    status: data.status,
    launchUrl: data.launchUrl,
    reason: data.reason ?? undefined,
    updatedAtMs,
  };
}

function timestampMillis(value: unknown): number | undefined {
  if (value instanceof Timestamp) {
    return value.toMillis();
  }
  return undefined;
}
