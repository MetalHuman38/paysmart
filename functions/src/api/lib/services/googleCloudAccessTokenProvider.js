import { GoogleAuth } from "google-auth-library";
export class GoogleCloudAccessTokenProvider {
    authByScope = new Map();
    async getAccessToken(scopes) {
        const normalizedScopes = [...scopes].sort();
        const scopeKey = normalizedScopes.join(" ");
        let auth = this.authByScope.get(scopeKey);
        if (!auth) {
            auth = new GoogleAuth({ scopes: normalizedScopes });
            this.authByScope.set(scopeKey, auth);
        }
        const client = await auth.getClient();
        const tokenResult = await client.getAccessToken();
        const token = typeof tokenResult === "string"
            ? tokenResult
            : tokenResult?.token ?? null;
        if (!token) {
            throw new Error("Failed to acquire Google access token");
        }
        return token;
    }
}
//# sourceMappingURL=googleCloudAccessTokenProvider.js.map