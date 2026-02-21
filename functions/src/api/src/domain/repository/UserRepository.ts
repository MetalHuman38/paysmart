export interface UserProfile {
  uid: string;
  tenantId?: string;
}

export interface UserRepository {
  getById(uid: string): Promise<UserProfile | null>;
  updatePhoneNumber(uid: string, phoneNumber: string): Promise<void>;
  logAuditEvent(data: Record<string, unknown>): Promise<void>;
}
