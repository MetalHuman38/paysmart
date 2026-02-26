import { FieldValue } from "firebase-admin/firestore";
import {
  IdentityProviderCallbackInput,
  IdentityProviderSessionResume,
} from "../../domain/model/identityProvider.js";
import { IdentityProviderRepository } from "../../domain/repository/IdentityProviderRepository.js";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class SubmitIdentityProviderCallback {
  constructor(
    private readonly identityProvider: IdentityProviderRepository,
    private readonly securitySettings: SecuritySettingsRepository
  ) {}

  async execute(
    uid: string,
    input: IdentityProviderCallbackInput
  ): Promise<IdentityProviderSessionResume> {
    const session = await this.identityProvider.submitCallback(uid, input);
    await this.syncSecuritySettings(uid, session.status);
    return session;
  }

  private async syncSecuritySettings(uid: string, status: string): Promise<void> {
    if (
      status !== "pending_review" &&
      status !== "verified" &&
      status !== "rejected"
    ) {
      return;
    }

    await this.securitySettings.createIfMissing(uid);
    if (status === "pending_review") {
      await this.securitySettings.update(uid, {
        hasVerifiedIdentity: false,
        kycStatus: "pending_review",
        updatedAt: FieldValue.serverTimestamp() as any,
      });
      return;
    }

    if (status === "verified") {
      await this.securitySettings.update(uid, {
        hasVerifiedIdentity: true,
        kycStatus: "verified",
        updatedAt: FieldValue.serverTimestamp() as any,
      });
      return;
    }

    await this.securitySettings.update(uid, {
      hasVerifiedIdentity: false,
      kycStatus: "rejected",
      updatedAt: FieldValue.serverTimestamp() as any,
    });
  }
}
