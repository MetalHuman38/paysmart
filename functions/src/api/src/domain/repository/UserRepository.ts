export interface UserProfile {
  uid: string;
  tenantId?: string;
}

export interface FinalizePhoneSignupInput {
  uid: string;
  email?: string;
  phoneNumber: string;
  isAnonymous: boolean;
  providerIds: string[];
  tenantId?: string | null;
  photoURL?: string;
  displayName?: string;
}

export interface UserRepository {
  getById(uid: string): Promise<UserProfile | null>;
  upsertVerifiedPhoneSignup(input: FinalizePhoneSignupInput): Promise<void>;
  logAuditEvent(data: Record<string, unknown>): Promise<void>;
}
