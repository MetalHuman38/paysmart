import { FieldValue } from "firebase-admin/firestore";
import { CommitIdentityUploadInput } from "../../domain/model/identityUpload.js";
import { IdentityUploadRepository } from "../../domain/repository/IdentityUploadRepository.js";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class CommitIdentityUpload {
  constructor(
    private readonly identityUploads: IdentityUploadRepository,
    private readonly securitySettings: SecuritySettingsRepository
  ) {}

  async execute(uid: string, input: CommitIdentityUploadInput) {
    const result = await this.identityUploads.commitSession(uid, input);

    await this.securitySettings.createIfMissing(uid);
    await this.securitySettings.update(uid, {
      hasVerifiedIdentity: false,
      kycStatus: "pending_review",
      updatedAt: FieldValue.serverTimestamp() as any,
    });

    return result;
  }
}
