export class GetUIDFromAuthHeader {
    auth;
    constructor(auth) {
        this.auth = auth;
    }
    async execute(authHeader) {
        if (!authHeader?.startsWith("Bearer ")) {
            throw new Error("Missing or invalid Authorization header");
        }
        const idToken = authHeader.substring("Bearer ".length);
        const decoded = await this.auth.verifyIdToken(idToken);
        return decoded.uid;
    }
}
//# sourceMappingURL=GetUIDFromAuthHeader.js.map