import {
  UploadIdentityEncryptedPayloadInput,
  UploadIdentityEncryptedPayloadResult,
} from "../../domain/model/identityUpload.js";
import { IdentityUploadRepository } from "../../domain/repository/IdentityUploadRepository.js";

export class UploadIdentityEncryptedPayload {
  constructor(private readonly identityUploads: IdentityUploadRepository) {}

  async execute(
    uid: string,
    input: UploadIdentityEncryptedPayloadInput
  ): Promise<UploadIdentityEncryptedPayloadResult> {
    return this.identityUploads.uploadEncryptedPayload(uid, input);
  }
}
