export class CreateIdentityUploadSession {
    identityUploads;
    constructor(identityUploads) {
        this.identityUploads = identityUploads;
    }
    async execute(uid, input) {
        return this.identityUploads.createSession(uid, input);
    }
}
//# sourceMappingURL=CreateIdentityUploadSession.js.map