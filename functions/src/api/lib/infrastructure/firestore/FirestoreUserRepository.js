import { FieldValue } from "firebase-admin/firestore";
export class FirestoreUserRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    async getById(uid) {
        const snap = await this.firestore.collection("users").doc(uid).get();
        if (!snap.exists)
            return null;
        const data = snap.data();
        return {
            uid,
            tenantId: data.tenantId,
            email: typeof data.email === "string" ? data.email : undefined,
            displayName: typeof data.displayName === "string" ? data.displayName : undefined,
            launchInterest: data.launchInterest === "invoice" || data.launchInterest === "top_up"
                ? data.launchInterest
                : undefined,
        };
    }
    async logAuditEvent(data) {
        await this.firestore.collection("audit_logs").add({
            ...data,
            createdAt: FieldValue.serverTimestamp(),
        });
    }
    async upsertVerifiedPhoneSignup(input) {
        const docRef = this.firestore.collection("users").doc(input.uid);
        const sanitizedDisplayName = input.displayName?.trim();
        const sanitizedPhotoUrl = typeof input.photoURL === "string" && input.photoURL.startsWith("http")
            ? input.photoURL
            : null;
        await this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(docRef);
            const payload = {
                authProvider: "phone",
                email: input.email ?? FieldValue.delete(),
                isAnonymous: input.isAnonymous,
                providerIds: input.providerIds,
                lastSignedIn: FieldValue.serverTimestamp(),
                displayName: sanitizedDisplayName || FieldValue.delete(),
                photoURL: sanitizedPhotoUrl ?? FieldValue.delete(),
                phoneNumber: input.phoneNumber,
                tenantId: input.tenantId ?? FieldValue.delete(),
            };
            if (!snap.exists) {
                payload.createdAt = FieldValue.serverTimestamp();
            }
            tx.set(docRef, payload, { merge: true });
        });
    }
}
//# sourceMappingURL=FirestoreUserRepository.js.map