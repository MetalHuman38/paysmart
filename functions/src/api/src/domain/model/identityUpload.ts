export type IdentityDocumentType =
  | "passport"
  | "drivers_license"
  | "national_id";

export type IdentityUploadStatus =
  | "session_created"
  | "pending_review"
  | "review_processing"
  | "verified"
  | "rejected";

export interface CreateIdentityUploadSessionInput {
  documentType: IdentityDocumentType;
  payloadSha256: string;
  contentType: string;
}

export interface IdentityUploadSession {
  sessionId: string;
  uploadUrl: string;
  objectPath: string;
  associatedData: string;
  attestationNonce: string;
  expiresAtMs: number;
  encryptionKeyBase64: string;
  encryptionSchema: "aes-256-gcm-v1";
  cryptoContractVersion: string;
}

export interface CommitIdentityUploadInput {
  sessionId: string;
  payloadSha256: string;
  attestationJwt: string;
}

export interface IdentityUploadCommitResult {
  verificationId: string;
  status: "pending_review";
}

export interface UploadIdentityEncryptedPayloadInput {
  sessionId: string;
  payloadBase64: string;
  contentType?: string;
}

export interface UploadIdentityEncryptedPayloadResult {
  sessionId: string;
  objectPath: string;
  bytesWritten: number;
}
