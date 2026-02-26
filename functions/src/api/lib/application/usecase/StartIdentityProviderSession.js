export class StartIdentityProviderSession {
    identityProvider;
    constructor(identityProvider) {
        this.identityProvider = identityProvider;
    }
    async execute(uid, input) {
        return this.identityProvider.startSession(uid, input);
    }
}
//# sourceMappingURL=StartIdentityProviderSession.js.map