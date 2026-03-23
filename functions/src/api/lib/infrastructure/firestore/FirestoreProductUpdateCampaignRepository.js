import { FieldValue, Timestamp } from "firebase-admin/firestore";
export class FirestoreProductUpdateCampaignRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    collection() {
        return this.firestore.collection("notificationCampaigns");
    }
    ref(campaignId) {
        return this.collection().doc(campaignId);
    }
    deliveryRef(campaignId, uid) {
        return this.ref(campaignId).collection("deliveries").doc(uid);
    }
    async createCampaign(input) {
        const docRef = this.collection().doc();
        await docRef.set({
            campaignType: "product_update",
            status: input.status ?? "draft",
            title: input.title.trim(),
            subject: input.subject?.trim() || input.title.trim(),
            summary: input.summary.trim(),
            body: input.body?.trim() || null,
            area: input.area?.trim() || null,
            releaseStatus: input.releaseStatus?.trim() || null,
            highlights: (input.highlights || []).map((item) => item.trim()).filter(Boolean),
            ctaLabel: input.ctaLabel?.trim() || null,
            ctaUrl: input.ctaUrl?.trim() || null,
            sendPush: input.sendPush !== false,
            sendEmail: input.sendEmail !== false,
            createdByUid: input.createdByUid?.trim() || null,
            createdByEmail: input.createdByEmail?.trim().toLowerCase() || null,
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
        return docRef.id;
    }
    async dispatchCampaign(campaignId) {
        await this.ref(campaignId).set({
            status: "ready",
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async claimReadyCampaign(campaignId) {
        const ref = this.ref(campaignId);
        return this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            if (!snap.exists) {
                return null;
            }
            const data = snap.data();
            if (data.campaignType !== "product_update") {
                return null;
            }
            if (data.status !== "ready") {
                return null;
            }
            tx.set(ref, {
                status: "processing",
                processingStartedAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
                lastError: FieldValue.delete(),
            }, { merge: true });
            return mapCampaign(ref.id, data);
        });
    }
    async markSent(campaignId, stats) {
        await this.ref(campaignId).set({
            status: "sent",
            sentAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
            deliveryStats: stats,
        }, { merge: true });
    }
    async markFailed(campaignId, errorMessage, stats) {
        await this.ref(campaignId).set({
            status: "failed",
            updatedAt: FieldValue.serverTimestamp(),
            lastError: errorMessage.slice(0, 512),
            ...(stats ? { deliveryStats: stats } : {}),
        }, { merge: true });
    }
    async getDeliverySnapshot(campaignId, uid) {
        const snap = await this.deliveryRef(campaignId, uid).get();
        if (!snap.exists) {
            return {};
        }
        const data = snap.data();
        return {
            emailSentAtMs: timestampToMillis(data.emailSentAt),
            notificationSentAtMs: timestampToMillis(data.notificationSentAt),
        };
    }
    async markNotificationDelivered(campaignId, uid, notificationId) {
        await this.deliveryRef(campaignId, uid).set({
            notificationId,
            notificationSentAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async markNotificationFailed(campaignId, uid, errorMessage) {
        await this.deliveryRef(campaignId, uid).set({
            notificationFailedAt: FieldValue.serverTimestamp(),
            notificationLastError: errorMessage.slice(0, 512),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async markEmailDelivered(campaignId, uid, email) {
        await this.deliveryRef(campaignId, uid).set({
            email,
            emailSentAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async markEmailFailed(campaignId, uid, errorMessage) {
        await this.deliveryRef(campaignId, uid).set({
            emailFailedAt: FieldValue.serverTimestamp(),
            emailLastError: errorMessage.slice(0, 512),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async listPublishedCampaigns(limit = 20) {
        const normalizedLimit = normalizeLimit(limit);
        const rawLimit = Math.max(normalizedLimit * 5, 50);
        const snap = await this.collection().orderBy("sentAt", "desc").limit(rawLimit).get();
        return snap.docs
            .map((doc) => mapPublishedCampaign(doc.id, doc.data()))
            .filter((campaign) => campaign !== null)
            .slice(0, normalizedLimit);
    }
}
function mapCampaign(campaignId, data) {
    return {
        campaignId,
        status: normalizeStatus(data.status),
        title: asString(data.title),
        subject: asString(data.subject) || asString(data.title),
        summary: asString(data.summary),
        body: asOptionalString(data.body),
        area: asOptionalString(data.area),
        releaseStatus: asOptionalString(data.releaseStatus),
        highlights: Array.isArray(data.highlights)
            ? data.highlights.map((item) => asString(item)).filter(Boolean)
            : [],
        ctaLabel: asOptionalString(data.ctaLabel),
        ctaUrl: asOptionalString(data.ctaUrl),
        sendPush: data.sendPush !== false,
        sendEmail: data.sendEmail !== false,
    };
}
function mapPublishedCampaign(campaignId, data) {
    if (asString(data.campaignType) !== "product_update") {
        return null;
    }
    if (asString(data.status) !== "sent") {
        return null;
    }
    const sentAtMs = timestampToMillis(data.sentAt);
    if (typeof sentAtMs !== "number" || !Number.isFinite(sentAtMs)) {
        return null;
    }
    return {
        ...mapCampaign(campaignId, data),
        status: "sent",
        sentAtMs,
    };
}
function normalizeStatus(raw) {
    switch (asString(raw)) {
        case "draft":
        case "ready":
        case "processing":
        case "sent":
        case "failed":
            return asString(raw);
        default:
            return "draft";
    }
}
function asString(value) {
    return typeof value === "string" ? value.trim() : "";
}
function asOptionalString(value) {
    const normalized = asString(value);
    return normalized || undefined;
}
function timestampToMillis(value) {
    return value instanceof Timestamp ? value.toMillis() : undefined;
}
function normalizeLimit(value) {
    if (!Number.isFinite(value)) {
        return 20;
    }
    return Math.max(1, Math.min(Math.trunc(value), 50));
}
//# sourceMappingURL=FirestoreProductUpdateCampaignRepository.js.map