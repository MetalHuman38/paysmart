import { FieldValue, Firestore } from "firebase-admin/firestore";
import { createHash } from "node:crypto";
import {
  PasskeyCredentialOwner,
  PasskeyChallengeKind,
  StoredPasskeyChallenge,
  StoredPasskeyCredential,
  StoredPasskeySignInChallenge,
} from "../../domain/model/passkey.js";
import { PasskeyRepository } from "../../domain/repository/PasskeyRepository.js";

const CHALLENGE_DOC_ID = "passkey";
const PASSKEY_CREDENTIAL_INDEX_COLLECTION = "passkeyCredentialIndex";
const PASSKEY_SIGNIN_CHALLENGES_COLLECTION = "passkeySignInChallenges";

type ChallengeDoc = {
  registrationChallenge?: string;
  registrationExpiresAtMs?: number;
  authenticationChallenge?: string;
  authenticationExpiresAtMs?: number;
};

type PasskeyCredentialIndexDoc = {
  uid?: string;
  createdAtMs?: number;
  updatedAtMs?: number;
};

type SignInChallengeDoc = {
  challenge?: string;
  expiresAtMs?: number;
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
    const credentialRef = this.credentialsCollection(uid).doc(credential.credentialId);
    const indexRef = this.credentialIndexCollection().doc(credential.credentialId);
    await this.firestore.runTransaction(async (tx) => {
      tx.set(credentialRef, credential, { merge: true });
      tx.set(
        indexRef,
        {
          uid,
          createdAtMs: credential.createdAtMs,
          updatedAtMs: credential.updatedAtMs,
        },
        { merge: true }
      );
    });
  }

  async getCredentialOwner(credentialId: string): Promise<PasskeyCredentialOwner | null> {
    const candidates = buildCredentialIdCandidates(credentialId);

    for (const candidate of candidates) {
      const owner = await this.resolveOwnerFromIndex(candidate);
      if (owner) {
        return owner;
      }
    }

    for (const candidate of candidates) {
      const owner = await this.resolveOwnerFromCollectionGroup(candidate);
      if (owner) {
        await this.backfillCredentialIndex(owner.uid, owner.credential);
        return owner;
      }
    }

    return null;
  }

  async deleteCredential(uid: string, credentialId: string): Promise<void> {
    const credentialRef = this.credentialsCollection(uid).doc(credentialId);
    const indexRef = this.credentialIndexCollection().doc(credentialId);
    await this.firestore.runTransaction(async (tx) => {
      tx.delete(credentialRef);
      tx.delete(indexRef);
    });
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

  async saveSignInChallenge(challenge: string, expiresAtMs: number): Promise<void> {
    const ref = this.signInChallengeDoc(challenge);
    await ref.set(
      {
        challenge,
        expiresAtMs,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async consumeSignInChallenge(
    challenge: string
  ): Promise<StoredPasskeySignInChallenge | null> {
    const ref = this.signInChallengeDoc(challenge);
    return this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(ref);
      if (!snap.exists) return null;

      const data = snap.data() as SignInChallengeDoc;
      const storedChallenge = (data.challenge || "").trim();
      const expiresAtMs = Number(data.expiresAtMs || 0);
      if (!storedChallenge || !expiresAtMs || storedChallenge !== challenge) {
        tx.delete(ref);
        return null;
      }

      tx.delete(ref);
      return {
        challenge: storedChallenge,
        expiresAtMs,
      };
    });
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

  private credentialIndexCollection() {
    return this.firestore.collection(PASSKEY_CREDENTIAL_INDEX_COLLECTION);
  }

  private async resolveOwnerFromIndex(
    credentialId: string
  ): Promise<PasskeyCredentialOwner | null> {
    const indexSnap = await this.credentialIndexCollection().doc(credentialId).get();
    if (!indexSnap.exists) return null;

    const indexData = indexSnap.data() as PasskeyCredentialIndexDoc;
    const uid = (indexData.uid || "").trim();
    if (!uid) return null;

    const credential = await this.getCredential(uid, credentialId);
    if (!credential) {
      await this.credentialIndexCollection().doc(credentialId).delete();
      return null;
    }

    return { uid, credential };
  }

  private async resolveOwnerFromCollectionGroup(
    credentialId: string
  ): Promise<PasskeyCredentialOwner | null> {
    const snap = await this.firestore
      .collectionGroup("passkeys")
      .where("credentialId", "==", credentialId)
      .limit(1)
      .get();

    if (snap.empty) return null;

    const doc = snap.docs[0];
    const uid = doc.ref.parent.parent?.id?.trim() || "";
    if (!uid) return null;

    const credential = doc.data() as StoredPasskeyCredential;
    return {
      uid,
      credential,
    };
  }

  private async backfillCredentialIndex(
    uid: string,
    credential: StoredPasskeyCredential
  ): Promise<void> {
    const id = credential.credentialId?.trim();
    if (!id) return;

    await this.credentialIndexCollection().doc(id).set(
      {
        uid,
        createdAtMs: credential.createdAtMs || Date.now(),
        updatedAtMs: credential.updatedAtMs || Date.now(),
      },
      { merge: true }
    );
  }

  private signInChallengeDoc(challenge: string) {
    return this.firestore
      .collection(PASSKEY_SIGNIN_CHALLENGES_COLLECTION)
      .doc(hashChallenge(challenge));
  }
}

function hashChallenge(challenge: string): string {
  return createHash("sha256").update(challenge, "utf8").digest("base64url");
}

function buildCredentialIdCandidates(input: string): string[] {
  const trimmed = input.trim();
  if (!trimmed) return [];

  const variants = new Set<string>();
  variants.add(trimmed);

  if (/^[A-Za-z0-9+/_=-]+$/.test(trimmed)) {
    try {
      variants.add(Buffer.from(trimmed, "base64url").toString("base64url"));
    } catch {
      // Keep best-effort variants only.
    }
    try {
      variants.add(Buffer.from(trimmed, "base64").toString("base64url"));
    } catch {
      // Keep best-effort variants only.
    }
  }

  return Array.from(variants).filter((value) => value.length > 0);
}
