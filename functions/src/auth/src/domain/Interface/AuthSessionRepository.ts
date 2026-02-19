export type AuthSessionWriteInput = {
  uid: string;
  sid: string;
  provider: string;
  providerIds: string[];
  signInAtSeconds: number;
  ipAddress?: string;
  userAgent?: string;
};

export type AuthSessionWriteResult = {
  sid: string;
  sv: number;
};

export interface AuthSessionRepository {
  recordSignInSession(input: AuthSessionWriteInput): Promise<AuthSessionWriteResult>;
}
