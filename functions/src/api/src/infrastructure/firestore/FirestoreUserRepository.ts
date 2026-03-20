import { Firestore, FieldValue } from "firebase-admin/firestore";
import {
  FinalizePhoneSignupInput,
  UserProfile,
  UserRepository,
} from "../../domain/repository/UserRepository.js";

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

  async upsertVerifiedPhoneSignup(input: FinalizePhoneSignupInput): Promise<void> {
    const docRef = this.firestore.collection("users").doc(input.uid);
    const sanitizedDisplayName = input.displayName?.trim();
    const sanitizedPhotoUrl =
      typeof input.photoURL === "string" && input.photoURL.startsWith("http")
        ? input.photoURL
        : null;

    await this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(docRef);
      const payload: Record<string, unknown> = {
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
