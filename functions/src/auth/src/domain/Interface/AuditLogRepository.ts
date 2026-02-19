export interface AuditLogRepository {
  log(uid: string, event: string, details?: Record<string, any>): Promise<void>;
}