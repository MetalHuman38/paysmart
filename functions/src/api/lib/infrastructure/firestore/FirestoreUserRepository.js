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
        };
    }
    async logAuditEvent(data) {
        await this.firestore.collection("audit_logs").add({
            ...data,
            createdAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=FirestoreUserRepository.js.map