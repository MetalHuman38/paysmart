import {
  PasskeyChallengeKind,
  StoredPasskeyChallenge,
  StoredPasskeyCredential,
} from "../model/passkey.js";

export interface PasskeyRepository {
  listCredentials(uid: string): Promise<StoredPasskeyCredential[]>;
  getCredential(
    uid: string,
    credentialId: string
  ): Promise<StoredPasskeyCredential | null>;
  upsertCredential(uid: string, credential: StoredPasskeyCredential): Promise<void>;
  updateCounter(uid: string, credentialId: string, counter: number): Promise<void>;
  saveChallenge(uid: string, challenge: StoredPasskeyChallenge): Promise<void>;
  consumeChallenge(
    uid: string,
    kind: PasskeyChallengeKind
  ): Promise<StoredPasskeyChallenge | null>;
}
