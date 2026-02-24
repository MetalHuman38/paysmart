import { FieldValue } from "firebase-admin/firestore";
export class SetHomeAddressVerified {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            hasAddedHomeAddress: true,
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=SetHomeAddressVerified.js.map