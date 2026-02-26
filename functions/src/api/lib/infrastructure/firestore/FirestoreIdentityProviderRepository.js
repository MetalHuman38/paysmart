import { randomUUID } from "crypto";
import { FieldValue, Timestamp } from "firebase-admin/firestore";
const SESSION_TTL_MS = 30 * 60 * 1000;
const RAW_DEEP_LINK_MAX_LEN = 4000;
export class FirestoreIdentityProviderRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    providerName() {
        const configured = (process.env.IDENTITY_PROVIDER_NAME || "").trim();
        return configured || "third_party";
    }
    sessionsCollection(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("identityProviderSessions");
    }
    sessionRef(uid, sessionId) {
        return this.sessionsCollection(uid).doc(sessionId);
    }
    async startSession(uid, input) {
        const now = Date.now();
        const sessionId = randomUUID();
        const expiresAtMs = now + SESSION_TTL_MS;
        const provider = this.providerName();
        const launchUrl = resolveLaunchUrlTemplate(sessionId, provider);
        const doc = {
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
    async resumeSession(uid, input) {
        const sessionId = input.sessionId.trim();
        if (!sessionId) {
            throw new Error("Missing sessionId");
        }
        const ref = this.sessionRef(uid, sessionId);
        const snap = await ref.get();
        if (!snap.exists) {
            throw new Error("Provider session not found");
        }
        const data = snap.data();
        if (typeof data.expiresAtMs === "number" &&
            Date.now() > data.expiresAtMs &&
            (data.status === "session_created" || data.status === "in_progress")) {
            const now = Date.now();
            await ref.set({
                status: "cancelled",
                reason: "session_expired",
                updatedAtMs: now,
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
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
    async submitCallback(uid, input) {
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
        const current = snap.data();
        const next = resolveStatusTransition(current.status, event);
        const now = Date.now();
        const callbackCount = (current.callbackCount ?? 0) + 1;
        await ref.set({
            status: next.status,
            reason: next.reason ?? null,
            providerRef: normalizeProviderRef(input.providerRef),
            lastEvent: event,
            lastCallbackRawDeepLink: normalizeRawDeepLink(input.rawDeepLink),
            callbackCount,
            updatedAtMs: now,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
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
function resolveStatusTransition(currentStatus, event) {
    const normalized = event.trim().toLowerCase();
    if (!normalized) {
        return { status: currentStatus };
    }
    if (normalized === "started" ||
        normalized === "open" ||
        normalized === "opened" ||
        normalized === "launched" ||
        normalized === "sdk_started") {
        return { status: "in_progress", reason: null };
    }
    if (normalized === "submitted" ||
        normalized === "complete" ||
        normalized === "completed" ||
        normalized === "review_pending") {
        return { status: "pending_review", reason: null };
    }
    if (normalized === "verified" ||
        normalized === "approved" ||
        normalized === "success") {
        return { status: "verified", reason: null };
    }
    if (normalized === "rejected" ||
        normalized === "declined" ||
        normalized === "failed" ||
        normalized === "error") {
        return {
            status: "rejected",
            reason: `provider_event_${normalized}`,
        };
    }
    if (normalized === "cancelled" ||
        normalized === "canceled" ||
        normalized === "aborted") {
        return { status: "cancelled", reason: "cancelled_by_user" };
    }
    return {
        status: currentStatus,
        reason: currentStatus === "rejected" || currentStatus === "cancelled" ?
            `provider_event_${normalized}` :
            null,
    };
}
function resolveLaunchUrlTemplate(sessionId, provider) {
    const template = (process.env.IDENTITY_PROVIDER_LAUNCH_URL_TEMPLATE || "").trim();
    if (!template) {
        return null;
    }
    return template
        .replaceAll("{sessionId}", encodeURIComponent(sessionId))
        .replaceAll("{provider}", encodeURIComponent(provider));
}
function normalizeCountryIso2(value) {
    if (!value)
        return null;
    const trimmed = value.trim().toUpperCase();
    return trimmed.length === 2 ? trimmed : null;
}
function normalizeDocumentType(value) {
    if (!value)
        return null;
    const normalized = value.trim().toLowerCase().replace(/\s+/g, "_");
    return normalized.length > 0 ? normalized : null;
}
function normalizeProviderRef(value) {
    if (!value)
        return null;
    const trimmed = value.trim();
    return trimmed.length > 0 ? trimmed.slice(0, 512) : null;
}
function normalizeRawDeepLink(value) {
    if (!value)
        return null;
    const trimmed = value.trim();
    if (!trimmed)
        return null;
    return trimmed.slice(0, RAW_DEEP_LINK_MAX_LEN);
}
function normalizeEvent(event) {
    return event.trim().toLowerCase();
}
function parseSessionIdFromDeepLink(rawDeepLink) {
    if (!rawDeepLink)
        return null;
    const trimmed = rawDeepLink.trim();
    if (!trimmed)
        return null;
    return (runCatching(() => {
        const url = new URL(trimmed);
        return url.searchParams.get("sessionId");
    }) ?? null);
}
function runCatching(fn) {
    try {
        return fn();
    }
    catch {
        return null;
    }
}
function toResume(data, updatedAtField) {
    const updatedAtMs = typeof data.updatedAtMs === "number" ?
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
function timestampMillis(value) {
    if (value instanceof Timestamp) {
        return value.toMillis();
    }
    return undefined;
}
//# sourceMappingURL=FirestoreIdentityProviderRepository.js.map