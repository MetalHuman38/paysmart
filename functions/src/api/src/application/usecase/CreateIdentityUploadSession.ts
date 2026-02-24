import { IdentityUploadRepository } from "../../domain/repository/IdentityUploadRepository.js";
import {
  CreateIdentityUploadSessionInput,
  IdentityUploadSession,
} from "../../domain/model/identityUpload.js";

export class CreateIdentityUploadSession {
  constructor(private readonly identityUploads: IdentityUploadRepository) {}

  async execute(
    uid: string,
    input: CreateIdentityUploadSessionInput
  ): Promise<IdentityUploadSession> {
    return this.identityUploads.createSession(uid, input);
  }
}
