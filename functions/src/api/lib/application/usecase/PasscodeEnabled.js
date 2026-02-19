import { FieldValue } from "firebase-admin/firestore";
export class PasscodeEnabled {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            passcodeEnabled: true,
            localPassCodeSetAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=PasscodeEnabled.js.map