import { FieldValue } from "firebase-admin/firestore";
export class FirestoreAuditLogRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    async log(uid, event, details) {
        const logEntry = {
            uid,
            event,
            details: details || {},
            timestamp: FieldValue.serverTimestamp(),
        };
        await this.firestore.collection("auditLogs").add(logEntry);
    }
    async logEMailVerified(uid, email, providerIds) {
        await this.firestore.collection("auditLogs").add({
            uid,
            event: "email_verified",
            details: {
                email,
                providerIds,
            },
            timestamp: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=FirestoreAuditLogRepository.js.map