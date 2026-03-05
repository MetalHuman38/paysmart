import { FieldValue } from "firebase-admin/firestore";
export class SetMfaEnrollmentPromptState {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid, hasSkippedMfaEnrollmentPrompt) {
        await this.repo.createIfMissing(uid);
        await this.repo.update(uid, {
            hasSkippedMfaEnrollmentPrompt,
            updatedAt: FieldValue.serverTimestamp(),
        });
    }
}
//# sourceMappingURL=SetMfaEnrollmentPromptState.js.map