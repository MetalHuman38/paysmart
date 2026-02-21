import { Firestore, FieldValue } from "firebase-admin/firestore";
import { UserProfile, UserRepository } from "../../domain/repository/UserRepository.js";

export class FirestoreUserRepository implements UserRepository {
  constructor(private readonly firestore: Firestore) {}

  async getById(uid: string): Promise<UserProfile | null> {
    const snap = await this.firestore.collection("users").doc(uid).get();
    if (!snap.exists) return null;

    const data = snap.data()!;
    return {
      uid,
      tenantId: data.tenantId,
    };
  }

  async logAuditEvent(data: Record<string, unknown>) {
    await this.firestore.collection("audit_logs").add({
      ...data,
      createdAt: FieldValue.serverTimestamp(),
    });
  }

  async updatePhoneNumber(uid: string, phoneNumber: string): Promise<void> {
    await this.firestore.collection("users").doc(uid).set(
      {
        phoneNumber,
        lastSignedIn: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }
}
