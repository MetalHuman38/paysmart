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

export type StoredPasskeyChallenge = {
  kind: PasskeyChallengeKind;
  challenge: string;
  expiresAtMs: number;
};
