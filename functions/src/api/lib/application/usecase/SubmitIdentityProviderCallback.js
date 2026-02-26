import { FieldValue } from "firebase-admin/firestore";
export class SubmitIdentityProviderCallback {
    identityProvider;
    securitySettings;
    constructor(identityProvider, securitySettings) {
        this.identityProvider = identityProvider;
        this.securitySettings = securitySettings;
    }
    async execute(uid, input) {
        const session = await this.identityProvider.submitCallback(uid, input);
        await this.syncSecuritySettings(uid, session.status);
        return session;
    }
    async syncSecuritySettings(uid, status) {
        if (status !== "pending_review" &&
            status !== "verified" &&
            status !== "rejected") {
            return;
        }
        await this.securitySettings.createIfMissing(uid);
        if (status === "pending_review") {
            await this.securitySettings.update(uid, {
                hasVerifiedIdentity: false,
                kycStatus: "pending_review",
                updatedAt: FieldValue.serverTimestamp(),
            });
            return;
        }
        if (status === "verified") {
            await this.securitySettings.update(uid, {
                hasVerifiedIdentity: true,
                kycStatus: "verified",
                updatedAt: FieldValue.serverTimestamp(),
            });
            return;
        }
        await this.securitySettings.update(uid, {
            hasVerifiedIdentity: false,
            kycStatus: "rejected",
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=SubmitIdentityProviderCallback.js.map