import { FieldValue, Firestore } from "firebase-admin/firestore";
import {
  PasskeyChallengeKind,
  StoredPasskeyChallenge,
  StoredPasskeyCredential,
} from "../../domain/model/passkey.js";
import { PasskeyRepository } from "../../domain/repository/PasskeyRepository.js";

const CHALLENGE_DOC_ID = "passkey";

type ChallengeDoc = {
  registrationChallenge?: string;
  registrationExpiresAtMs?: number;
  authenticationChallenge?: string;
  authenticationExpiresAtMs?: number;
};

export class FirestorePasskeyRepository implements PasskeyRepository {
  constructor(private readonly firestore: Firestore) {}

  async listCredentials(uid: string): Promise<StoredPasskeyCredential[]> {
    const snap = await this.credentialsCollection(uid).get();
    return snap.docs.map((doc) => doc.data() as StoredPasskeyCredential);
  }

  async getCredential(
    uid: string,
    credentialId: string
  ): Promise<StoredPasskeyCredential | null> {
    const snap = await this.credentialsCollection(uid).doc(credentialId).get();
    if (!snap.exists) return null;
    return snap.data() as StoredPasskeyCredential;
  }

  async upsertCredential(uid: string, credential: StoredPasskeyCredential): Promise<void> {
    await this.credentialsCollection(uid)
      .doc(credential.credentialId)
      .set(credential, { merge: true });
  }

  async updateCounter(uid: string, credentialId: string, counter: number): Promise<void> {
    await this.credentialsCollection(uid).doc(credentialId).set(
      {
        counter,
        updatedAtMs: Date.now(),
      },
      { merge: true }
    );
  }

  async saveChallenge(uid: string, challenge: StoredPasskeyChallenge): Promise<void> {
    const ref = this.challengeDoc(uid);
    const payload =
      challenge.kind === "registration" ?
        {
          registrationChallenge: challenge.challenge,
          registrationExpiresAtMs: challenge.expiresAtMs,
        } :
        {
          authenticationChallenge: challenge.challenge,
          authenticationExpiresAtMs: challenge.expiresAtMs,
        };

    await ref.set(
      {
        ...payload,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async consumeChallenge(
    uid: string,
    kind: PasskeyChallengeKind
  ): Promise<StoredPasskeyChallenge | null> {
    const ref = this.challengeDoc(uid);
    return this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(ref);
      if (!snap.exists) return null;

      const data = snap.data() as ChallengeDoc;
      const challenge =
        kind === "registration" ?
          data.registrationChallenge :
          data.authenticationChallenge;
      const expiresAtMs =
        kind === "registration" ?
          data.registrationExpiresAtMs :
          data.authenticationExpiresAtMs;
      if (!challenge || !expiresAtMs) return null;

      const clearPayload =
        kind === "registration" ?
          {
            registrationChallenge: FieldValue.delete(),
            registrationExpiresAtMs: FieldValue.delete(),
          } :
          {
            authenticationChallenge: FieldValue.delete(),
            authenticationExpiresAtMs: FieldValue.delete(),
          };
      tx.set(
        ref,
        {
          ...clearPayload,
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      return {
        kind,
        challenge,
        expiresAtMs,
      };
    });
  }

  private credentialsCollection(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("passkeys");
  }

  private challengeDoc(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("authSessionState")
      .doc(CHALLENGE_DOC_ID);
  }
}
