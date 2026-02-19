import { FieldValue } from "firebase-admin/firestore";
export class EnsureSecuritySettings {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        // Update the updatedAt field to trigger any listeners that depend on the security settings document
        await this.repo.update(uid, {
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=EnsureSecuritySettings.js.map