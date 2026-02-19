export type AuthUserByUid = {
  uid: string;
  phoneNumber?: string | null;
  providerIds?: string[];
};

export interface FirebaseAuthServiceInterface {
  getUserByPhone(phoneNumber: string): Promise<{ uid: string } | null>;
  getUserByEmail(email: string): Promise<{ uid: string; phoneNumber?: string | null } | null>;
  getUserByUid(uid: string): Promise<AuthUserByUid | null>;
}
