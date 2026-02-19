export interface UserProfile {
  uid: string;
  tenantId?: string;
}

export interface UserRepository {
  getById(uid: string): Promise<UserProfile | null>;
  logAuditEvent(data: Record<string, unknown>): Promise<void>;
}