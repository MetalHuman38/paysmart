export class ResumeIdentityProviderSession {
    identityProvider;
    constructor(identityProvider) {
        this.identityProvider = identityProvider;
    }
    async execute(uid, input) {
        return this.identityProvider.resumeSession(uid, input);
    }
}
//# sourceMappingURL=ResumeIdentityProviderSession.js.map