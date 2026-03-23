import { FieldValue, Timestamp } from "firebase-admin/firestore";
export class FirestoreNotificationPreferencesRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    ref(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("notificationPreferences")
            .doc("settings");
    }
    async get(uid) {
        const snap = await this.ref(uid).get();
        return snap.exists ? mapPreferences(snap.data()) : null;
    }
    async getOrCreate(uid) {
        const ref = this.ref(uid);
        await this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            if (!snap.exists) {
                tx.set(ref, buildDefaultPreferencesDocument());
            }
        });
        const existing = await this.get(uid);
        return existing ?? buildDefaultPreferences();
    }
    async update(uid, input) {
        const ref = this.ref(uid);
        const now = Timestamp.now();
        await this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            const current = snap.exists
                ? mapPreferences(snap.data())
                : buildDefaultPreferences();
            const nextEmailProductUpdates = input.emailProductUpdates ?? current.emailProductUpdates;
            const nextPushProductUpdates = input.pushProductUpdates ?? current.pushProductUpdates;
            const hadMarketingConsent = current.emailProductUpdates || current.pushProductUpdates;
            const hasMarketingConsent = nextEmailProductUpdates || nextPushProductUpdates;
            const marketingConsentAt = hasMarketingConsent && !hadMarketingConsent
                ? now
                : current.marketingConsentAt;
            const productUpdatesUnsubscribedAt = nextEmailProductUpdates
                ? null
                : input.emailProductUpdates === false && current.emailProductUpdates
                    ? now
                    : current.productUpdatesUnsubscribedAt;
            tx.set(ref, {
                emailTransactional: input.emailTransactional ?? current.emailTransactional,
                emailProductUpdates: nextEmailProductUpdates,
                pushTransactional: input.pushTransactional ?? current.pushTransactional,
                pushProductUpdates: nextPushProductUpdates,
                preferredLocale: normalizeLocale(input.preferredLocale) ??
                    current.preferredLocale ??
                    FieldValue.delete(),
                marketingConsentAt: marketingConsentAt ?? current.marketingConsentAt ?? null,
                productUpdatesUnsubscribedAt: productUpdatesUnsubscribedAt ?? null,
                updatedAt: FieldValue.serverTimestamp(),
                createdAt: snap.exists
                    ? (snap.get("createdAt") ?? FieldValue.serverTimestamp())
                    : FieldValue.serverTimestamp(),
            }, { merge: true });
        });
        const updated = await this.get(uid);
        return updated ?? buildDefaultPreferences();
    }
    async unsubscribeProductUpdates(uid) {
        const ref = this.ref(uid);
        await ref.set({
            emailProductUpdates: false,
            productUpdatesUnsubscribedAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async listEmailProductUpdateRecipients() {
        const snap = await this.firestore
            .collectionGroup("notificationPreferences")
            .where("emailProductUpdates", "==", true)
            .get();
        return snap.docs
            .map((doc) => {
            const uid = doc.ref.parent.parent?.id?.trim() || "";
            if (!uid) {
                return null;
            }
            const data = doc.data();
            return {
                uid,
                preferredLocale: normalizeLocale(data.preferredLocale),
            };
        })
            .filter((value) => value != null);
    }
    async listPushProductUpdateRecipients() {
        const snap = await this.firestore
            .collectionGroup("notificationPreferences")
            .where("pushProductUpdates", "==", true)
            .get();
        return snap.docs
            .map((doc) => {
            const uid = doc.ref.parent.parent?.id?.trim() || "";
            if (!uid) {
                return null;
            }
            const data = doc.data();
            return {
                uid,
                preferredLocale: normalizeLocale(data.preferredLocale),
            };
        })
            .filter((value) => value != null);
    }
}
export function buildDefaultPreferences() {
    return {
        emailTransactional: true,
        emailProductUpdates: false,
        pushTransactional: true,
        pushProductUpdates: false,
        marketingConsentAt: null,
        productUpdatesUnsubscribedAt: null,
        preferredLocale: null,
    };
}
function buildDefaultPreferencesDocument() {
    return {
        ...buildDefaultPreferences(),
        createdAt: FieldValue.serverTimestamp(),
        updatedAt: FieldValue.serverTimestamp(),
    };
}
function mapPreferences(data) {
    const defaults = buildDefaultPreferences();
    return {
        emailTransactional: typeof data.emailTransactional === "boolean"
            ? data.emailTransactional
            : defaults.emailTransactional,
        emailProductUpdates: typeof data.emailProductUpdates === "boolean"
            ? data.emailProductUpdates
            : defaults.emailProductUpdates,
        pushTransactional: typeof data.pushTransactional === "boolean"
            ? data.pushTransactional
            : defaults.pushTransactional,
        pushProductUpdates: typeof data.pushProductUpdates === "boolean"
            ? data.pushProductUpdates
            : defaults.pushProductUpdates,
        marketingConsentAt: asTimestamp(data.marketingConsentAt),
        productUpdatesUnsubscribedAt: asTimestamp(data.productUpdatesUnsubscribedAt),
        preferredLocale: normalizeLocale(data.preferredLocale),
    };
}
function asTimestamp(value) {
    return value instanceof Timestamp ? value : null;
}
function normalizeLocale(value) {
    if (typeof value !== "string") {
        return null;
    }
    const normalized = value.trim();
    return normalized.length > 0 && normalized.length <= 16 ? normalized : null;
}
//# sourceMappingURL=FirestoreNotificationPreferencesRepository.js.map