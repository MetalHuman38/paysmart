import { Firestore, FieldValue } from "firebase-admin/firestore";
import { AuditLogRepository } from "../../domain/Interface/AuditLogRepository.js";

export class FirestoreAuditLogRepository implements AuditLogRepository {
  constructor(private readonly firestore: Firestore) {}

    async log(uid: string, event: string, details?: Record<string, any>): Promise<void> {
        const logEntry = {
            uid,
            event,
            details: details || {},
            timestamp: FieldValue.serverTimestamp(),
        };
        await this.firestore.collection("auditLogs").add(logEntry);
    }

    async logEMailVerified(uid: string, email: string, providerIds: string[]): Promise<void> {
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
