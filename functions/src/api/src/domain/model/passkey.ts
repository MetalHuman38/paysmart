export type PasskeyChallengeKind = "registration" | "authentication";

export type StoredPasskeyCredential = {
  credentialId: string;
  publicKeyBase64Url: string;
  counter: number;
  transports: string[];
  deviceType?: string;
  backedUp?: boolean;
  createdAtMs: number;
  updatedAtMs: number;
};

export type PasskeyCredentialSummary = Pick<
  StoredPasskeyCredential,
  | "credentialId"
  | "deviceType"
  | "backedUp"
  | "transports"
  | "createdAtMs"
  | "updatedAtMs"
>;

export type StoredPasskeyChallenge = {
  kind: PasskeyChallengeKind;
  challenge: string;
  expiresAtMs: number;
};

export type StoredPasskeySignInChallenge = {
  challenge: string;
  expiresAtMs: number;
};

export type PasskeyCredentialOwner = {
  uid: string;
  credential: StoredPasskeyCredential;
};
