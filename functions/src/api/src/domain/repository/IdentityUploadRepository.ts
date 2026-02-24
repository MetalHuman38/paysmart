import {
  CommitIdentityUploadInput,
  CreateIdentityUploadSessionInput,
  IdentityUploadCommitResult,
  IdentityUploadSession,
  UploadIdentityEncryptedPayloadInput,
  UploadIdentityEncryptedPayloadResult,
} from "../model/identityUpload.js";

export interface IdentityUploadRepository {
  createSession(
    uid: string,
    input: CreateIdentityUploadSessionInput
  ): Promise<IdentityUploadSession>;

  commitSession(
    uid: string,
    input: CommitIdentityUploadInput
  ): Promise<IdentityUploadCommitResult>;

  uploadEncryptedPayload(
    uid: string,
    input: UploadIdentityEncryptedPayloadInput
  ): Promise<UploadIdentityEncryptedPayloadResult>;
}
