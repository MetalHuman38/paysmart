import { initDeps } from "../../dependencies.js";
import { FirebaseAuthService } from "../auth/FirebaseAuthService.js";
import { FirestoreAuditLogRepository } from "../firestore/FirestoreAuditLogRepository.js";
import { FirestoreAuthSessionRepository } from "../firestore/FirestoreAuthSessionRepository.js";
import { FirestoreSecuritySettingsRepository } from "../firestore/FirestoreSecuritySettingsRepo.js";
import { logEvent } from "../../utils.js";

export function buildAuthContainer() {
  const { auth, firestore } = initDeps();
    const authService = new FirebaseAuthService(auth);
    const auditLogRepo = new FirestoreAuditLogRepository(firestore);
    const authSessionRepo = new FirestoreAuthSessionRepository(firestore);
    const securitySettingsRepo = new FirestoreSecuritySettingsRepository(firestore);
    logEvent("Auth container built", { timestamp: new Date().toISOString() });

    return {
        authService,
        auditLogRepo,
        authSessionRepo,
        securitySettingsRepo,
    };
}
