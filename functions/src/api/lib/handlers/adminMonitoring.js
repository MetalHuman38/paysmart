import { initDeps } from "../dependencies.js";
const DEFAULT_LIMIT = 25;
const MAX_LIMIT = 100;
function parseLimit(raw) {
    if (typeof raw !== "string")
        return DEFAULT_LIMIT;
    const parsed = Number(raw);
    if (!Number.isFinite(parsed))
        return DEFAULT_LIMIT;
    const bounded = Math.floor(parsed);
    if (bounded <= 0)
        return DEFAULT_LIMIT;
    return Math.min(bounded, MAX_LIMIT);
}
function asRecord(value) {
    if (!value || typeof value !== "object")
        return {};
    return value;
}
function asString(value) {
    if (typeof value !== "string")
        return undefined;
    const normalized = value.trim();
    return normalized.length > 0 ? normalized : undefined;
}
function asNumber(value) {
    if (typeof value === "number" && Number.isFinite(value))
        return value;
    if (typeof value === "string") {
        const parsed = Number(value);
        if (Number.isFinite(parsed))
            return parsed;
    }
    return undefined;
}
function asBoolean(value) {
    if (typeof value === "boolean")
        return value;
    return undefined;
}
function timestampToMillis(value) {
    const fromNumber = asNumber(value);
    if (fromNumber !== undefined)
        return fromNumber;
    const maybeTimestamp = asRecord(value);
    const maybeToMillis = maybeTimestamp.toMillis;
    if (typeof maybeToMillis === "function") {
        try {
            const millis = maybeToMillis.call(value);
            const normalized = asNumber(millis);
            if (normalized !== undefined)
                return normalized;
        }
        catch {
            return 0;
        }
    }
    return 0;
}
function latestTimestamp(...values) {
    return values.reduce((max, next) => {
        const current = timestampToMillis(next);
        return current > max ? current : max;
    }, 0);
}
async function queryLatestDocs(collectionRef, limit, preferredOrderField = "updatedAt") {
    try {
        return await collectionRef.orderBy(preferredOrderField, "desc").limit(limit).get();
    }
    catch {
        return await collectionRef.limit(limit).get();
    }
}
function normalizedUid(raw) {
    return (raw || "").trim();
}
export async function adminUserSessionSnapshotHandler(req, res) {
    try {
        const uid = normalizedUid(req.params.uid);
        if (!uid) {
            return res.status(400).json({ error: "Missing uid" });
        }
        const { firestore } = initDeps();
        const userRef = firestore.collection("users").doc(uid);
        const [sessionSnap, securitySnap] = await Promise.all([
            userRef.collection("authSessionState").doc("current").get(),
            userRef.collection("security").doc("settings").get(),
        ]);
        const sessionData = sessionSnap.exists ? asRecord(sessionSnap.data()) : null;
        const securityData = securitySnap.exists ? asRecord(securitySnap.data()) : null;
        return res.status(200).json({
            uid,
            sessionState: sessionData ?
                {
                    activeSid: asString(sessionData.activeSid) || null,
                    sessionVersion: asNumber(sessionData.sessionVersion) || null,
                    lastIssuedAtMs: asNumber(sessionData.lastIssuedAtMs) || null,
                    updatedAtMs: latestTimestamp(sessionData.updatedAt, sessionData.createdAt),
                } :
                null,
            securitySettings: securityData ?
                {
                    sessionLocked: asBoolean(securityData.sessionLocked) ?? null,
                    killSwitchActive: asBoolean(securityData.killSwitchActive) ?? null,
                    lockAfterMinutes: asNumber(securityData.lockAfterMinutes) ?? null,
                    biometricsEnabled: asBoolean(securityData.biometricsEnabled) ?? null,
                    passwordEnabled: asBoolean(securityData.passwordEnabled) ?? null,
                    passcodeEnabled: asBoolean(securityData.passcodeEnabled) ?? null,
                    updatedAtMs: latestTimestamp(securityData.updatedAt, securityData.createdAt),
                } :
                null,
            observedAtMs: Date.now(),
        });
    }
    catch (error) {
        console.error("adminUserSessionSnapshotHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
export async function adminUserActivityFeedHandler(req, res) {
    try {
        const uid = normalizedUid(req.params.uid);
        if (!uid) {
            return res.status(400).json({ error: "Missing uid" });
        }
        const perSourceLimit = parseLimit(req.query.limit);
        const { firestore } = initDeps();
        const userRef = firestore.collection("users").doc(uid);
        const [sessionStateSnap, identityUploadsSnap, providerSessionsSnap, addMoneySnap, walletTxSnap] = await Promise.all([
            queryLatestDocs(userRef.collection("authSessionState"), perSourceLimit),
            queryLatestDocs(userRef.collection("identityUploads"), perSourceLimit),
            queryLatestDocs(userRef.collection("identityProviderSessions"), perSourceLimit),
            queryLatestDocs(userRef.collection("payments").doc("add_money").collection("sessions"), perSourceLimit),
            queryLatestDocs(userRef.collection("walletTransactions"), perSourceLimit, "createdAt"),
        ]);
        const feed = [];
        sessionStateSnap.docs.forEach((doc) => {
            const data = asRecord(doc.data());
            feed.push({
                id: doc.id,
                source: "session_state",
                status: "state_snapshot",
                summary: `sid=${asString(data.activeSid) || "none"} sv=${asNumber(data.sessionVersion) || 0}`,
                timestampMs: latestTimestamp(data.updatedAt, data.createdAt),
                metadata: {
                    sessionVersion: asNumber(data.sessionVersion) || null,
                    lastIssuedAtMs: asNumber(data.lastIssuedAtMs) || null,
                },
            });
        });
        identityUploadsSnap.docs.forEach((doc) => {
            const data = asRecord(doc.data());
            const status = asString(data.status) || "unknown";
            const documentType = asString(data.documentType) || "unknown";
            feed.push({
                id: doc.id,
                source: "identity_upload",
                status,
                summary: `${documentType} -> ${status}`,
                timestampMs: latestTimestamp(data.reviewedAt, data.updatedAt, data.committedAt, data.payloadUploadedAt, data.createdAt),
                metadata: {
                    reviewDecision: asString(data.reviewDecision) || null,
                    reviewReason: asString(data.reviewDecisionReason) || null,
                    verificationId: asString(data.verificationId) || null,
                },
            });
        });
        providerSessionsSnap.docs.forEach((doc) => {
            const data = asRecord(doc.data());
            const status = asString(data.status) || "unknown";
            const event = asString(data.lastEvent) || "n/a";
            feed.push({
                id: doc.id,
                source: "identity_provider",
                status,
                summary: `${status} (${event})`,
                timestampMs: latestTimestamp(data.updatedAt, data.createdAt, data.updatedAtMs),
                metadata: {
                    provider: asString(data.provider) || null,
                    reason: asString(data.reason) || null,
                    providerRef: asString(data.providerRef) || null,
                },
            });
        });
        addMoneySnap.docs.forEach((doc) => {
            const data = asRecord(doc.data());
            const status = asString(data.status) || "unknown";
            const currency = asString(data.currency) || "N/A";
            const amountMinor = asNumber(data.amountMinor) || 0;
            feed.push({
                id: doc.id,
                source: "add_money",
                status,
                summary: `${status} ${currency} ${amountMinor}`,
                timestampMs: latestTimestamp(data.walletAppliedAt, data.settledAt, data.updatedAt, data.createdAt),
                metadata: {
                    provider: asString(data.provider) || null,
                    sessionId: asString(data.sessionId) || doc.id,
                    paymentIntentId: asString(data.stripePaymentIntentId) || null,
                },
            });
        });
        walletTxSnap.docs.forEach((doc) => {
            const data = asRecord(doc.data());
            const status = asString(data.status) || "unknown";
            const txType = asString(data.type) || "transaction";
            const currency = asString(data.currency) || "N/A";
            const amountMinor = asNumber(data.amountMinor) || 0;
            feed.push({
                id: doc.id,
                source: "wallet_transaction",
                status,
                summary: `${txType} ${status} ${currency} ${amountMinor}`,
                timestampMs: latestTimestamp(data.updatedAt, data.createdAt),
            });
        });
        feed.sort((a, b) => b.timestampMs - a.timestampMs);
        return res.status(200).json({
            uid,
            count: feed.length,
            perSourceLimit,
            items: feed,
            observedAtMs: Date.now(),
        });
    }
    catch (error) {
        console.error("adminUserActivityFeedHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=adminMonitoring.js.map