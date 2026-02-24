import { GoogleAuth } from "google-auth-library";

export interface CloudAccessTokenProvider {
  getAccessToken(scopes: string[]): Promise<string>;
}

export class GoogleCloudAccessTokenProvider implements CloudAccessTokenProvider {
  private readonly authByScope = new Map<string, GoogleAuth>();

  async getAccessToken(scopes: string[]): Promise<string> {
    const normalizedScopes = [...scopes].sort();
    const scopeKey = normalizedScopes.join(" ");

    let auth = this.authByScope.get(scopeKey);
    if (!auth) {
      auth = new GoogleAuth({ scopes: normalizedScopes });
      this.authByScope.set(scopeKey, auth);
    }

    const client = await auth.getClient();
    const tokenResult = await client.getAccessToken();
    const token =
      typeof tokenResult === "string"
        ? tokenResult
        : tokenResult?.token ?? null;

    if (!token) {
      throw new Error("Failed to acquire Google access token");
    }

    return token;
  }
}
