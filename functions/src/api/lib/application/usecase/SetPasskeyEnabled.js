import { FieldValue } from "firebase-admin/firestore";
export class SetPasskeyEnabled {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid, passkeyEnabled) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            passkeyEnabled,
            hasSkippedPasskeyEnrollmentPrompt: !passkeyEnabled,
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=SetPasskeyEnabled.js.map