import { FieldValue } from 'firebase-admin/firestore';
import { SECURITY_SETTINGS_COLLECTION } from '../../constants/index.js';
export class FirestoreSecuritySettingsRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    ref(uid) {
        return this.firestore
            .collection("users")
            .doc(uid).collection("security")
            .doc("settings");
    }
    userRef(uid) {
        return this.firestore.collection("users").doc(uid);
    }
    async createIfMissing(uid) {
        const docRef = this.ref(uid);
        await this.firestore.runTransaction(async (tx) => {
            const doc = await tx.get(docRef);
            if (!doc.exists) {
                const initialData = {
                    ...SECURITY_SETTINGS_COLLECTION,
                    updatedAt: FieldValue.serverTimestamp(),
                };
                tx.set(docRef, initialData);
                return initialData;
            }
            return doc.data();
        });
        return (await docRef.get()).data();
    }
    async get(uid) {
        const doc = await this.ref(uid).get();
        return doc.exists ? doc.data() : null;
    }
    async update(uid, data) {
        await this.ref(uid).set(data, { merge: true });
    }
    async persistProviders(uid, providerIds, options = {}) {
        const payload = {
            providerIds: [...new Set(providerIds)],
            updatedAt: FieldValue.serverTimestamp(),
        };
        // One-time link window should be consumed only after a real new federated link.
        if (options.consumeLinkingGrant) {
            payload.allowFederatedLinking = false;
        }
        await this.ref(uid).set(payload, { merge: true });
    }
    async markEmailAsVerified(uid) {
        await this.ref(uid).set({
            hasVerifiedEmail: true,
            emailToVerify: FieldValue.delete(),
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async upsertUserSignInProfile(uid, data) {
        await this.userRef(uid).set({
            ...data,
            lastSignedIn: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
}
//# sourceMappingURL=FirestoreSecuritySettingsRepo.js.map