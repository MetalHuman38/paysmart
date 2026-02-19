import { FieldValue } from "firebase-admin/firestore";
export class AllowFederatedLinking {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            allowFederatedLinking: true,
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=AllowFederatedLinking.js.map