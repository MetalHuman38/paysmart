export type TokenVerifier = {
  verifyIdToken(idToken: string): Promise<{ uid: string }>;
};

export class GetUIDFromAuthHeader {
  constructor(
    private readonly auth: TokenVerifier
  ) {}

  async execute(authHeader: string | null | undefined): Promise<string> {
    if (!authHeader?.startsWith("Bearer ")) {
      throw new Error("Missing or invalid Authorization header");
    }
    const idToken = authHeader.substring("Bearer ".length);
    const decoded = await this.auth.verifyIdToken(idToken);
    return decoded.uid;
  }
}
