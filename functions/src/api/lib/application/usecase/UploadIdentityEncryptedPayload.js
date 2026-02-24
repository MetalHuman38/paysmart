export class UploadIdentityEncryptedPayload {
    identityUploads;
    constructor(identityUploads) {
        this.identityUploads = identityUploads;
    }
    async execute(uid, input) {
        return this.identityUploads.uploadEncryptedPayload(uid, input);
    }
}
//# sourceMappingURL=UploadIdentityEncryptedPayload.js.map