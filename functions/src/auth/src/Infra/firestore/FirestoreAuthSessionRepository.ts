import { createHash } from "node:crypto";
import { FieldValue, Firestore } from "firebase-admin/firestore";
import {
  AuthSessionRepository,
  AuthSessionWriteInput,
  AuthSessionWriteResult,
} from "../../domain/Interface/AuthSessionRepository.js";

export class FirestoreAuthSessionRepository implements AuthSessionRepository {
  constructor(private readonly firestore: Firestore) {}

  private sessionRef(uid: string, sid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("authSessions")
      .doc(sid);
  }

  private stateRef(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("authSessionState")
      .doc("current");
  }

  async recordSignInSession(input: AuthSessionWriteInput): Promise<AuthSessionWriteResult> {
    const sid = input.sid;
    const sessionRef = this.sessionRef(input.uid, sid);
    const stateRef = this.stateRef(input.uid);

    const result = await this.firestore.runTransaction<AuthSessionWriteResult>(async (tx) => {
      const stateSnap = await tx.get(stateRef);
      const existingSv = stateSnap.exists
        ? Number((stateSnap.data() as Record<string, unknown>)?.sessionVersion)
        : NaN;
      const sv = Number.isFinite(existingSv) && existingSv > 0 ? Math.floor(existingSv) : 1;

      tx.set(
        sessionRef,
        {
          sid,
          uid: input.uid,
          sv,
          provider: input.provider,
          providerIds: [...new Set(input.providerIds)],
          signInAtSeconds: input.signInAtSeconds,
          ipHash: this.hashSignal(input.ipAddress),
          userAgentHash: this.hashSignal(input.userAgent),
          createdAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      tx.set(
        stateRef,
        {
          activeSid: sid,
          sessionVersion: sv,
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      return { sid, sv };
    });

    return result;
  }

  private hashSignal(value?: string): string | null {
    if (!value) {
      return null;
    }
    const salt = process.env.SESSION_SIGNAL_SALT ?? "";
    return createHash("sha256")
      .update(`${salt}:${value}`)
      .digest("hex");
  }
}
