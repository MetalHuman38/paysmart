import {
  PasskeyCredentialOwner,
  PasskeyChallengeKind,
  StoredPasskeyChallenge,
  StoredPasskeyCredential,
  StoredPasskeySignInChallenge,
} from "../model/passkey.js";

export interface PasskeyRepository {
  listCredentials(uid: string): Promise<StoredPasskeyCredential[]>;
  getCredential(
    uid: string,
    credentialId: string
  ): Promise<StoredPasskeyCredential | null>;
  upsertCredential(uid: string, credential: StoredPasskeyCredential): Promise<void>;
  getCredentialOwner(credentialId: string): Promise<PasskeyCredentialOwner | null>;
  deleteCredential(uid: string, credentialId: string): Promise<void>;
  updateCounter(uid: string, credentialId: string, counter: number): Promise<void>;
  saveSignInChallenge(challenge: string, expiresAtMs: number): Promise<void>;
  consumeSignInChallenge(challenge: string): Promise<StoredPasskeySignInChallenge | null>;
  saveChallenge(uid: string, challenge: StoredPasskeyChallenge): Promise<void>;
  consumeChallenge(
    uid: string,
    kind: PasskeyChallengeKind
  ): Promise<StoredPasskeyChallenge | null>;
}
