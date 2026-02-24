import { FieldValue } from "firebase-admin/firestore";
export class CommitIdentityUpload {
    identityUploads;
    securitySettings;
    constructor(identityUploads, securitySettings) {
        this.identityUploads = identityUploads;
        this.securitySettings = securitySettings;
    }
    async execute(uid, input) {
        const result = await this.identityUploads.commitSession(uid, input);
        await this.securitySettings.createIfMissing(uid);
        await this.securitySettings.update(uid, {
            hasVerifiedIdentity: false,
            kycStatus: "pending_review",
            updatedAt: FieldValue.serverTimestamp(),
        });
        return result;
    }
}
//# sourceMappingURL=CommitIdentityUpload.js.map