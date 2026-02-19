import { FieldValue } from "firebase-admin/firestore";
export class EnableBiometrics {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            biometricsRequired: false,
            biometricsEnabled: true,
            biometricsEnabledAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=EnableBiometrics.js.map