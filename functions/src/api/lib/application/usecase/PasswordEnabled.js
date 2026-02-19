import { FieldValue } from "firebase-admin/firestore";
export class PasswordEnabled {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            passwordEnabled: true,
            localPasswordSetAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=PasswordEnabled.js.map