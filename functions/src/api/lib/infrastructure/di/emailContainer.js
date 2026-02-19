// infrastructure/di/emailContainer.ts
import { initDeps } from "../../dependencies.js";
import { GenerateEmailVerification } from "../../application/usecase/GenerateEmailVerification.js";
import { CheckEmailVerificationStatus } from "../../application/usecase/CheckEmailVerificationStatus.js";
import { FirestoreSecuritySettingsRepository } from "../firestore/FirestoreSecuritySettingsRepository.js";
import { FirestoreUserRepository } from "../firestore/FirestoreUserRepository.js";
import { FirebaseAuthService } from "../auth/FirebaseAuthService.js";
export function emailContainer() {
    const { firestore, auth, getConfig } = initDeps();
    const cfg = getConfig();
    const authService = new FirebaseAuthService(auth);
    const securityRepo = new FirestoreSecuritySettingsRepository(firestore);
    const userRepo = new FirestoreUserRepository(firestore);
    const generateEmailVerification = new GenerateEmailVerification(securityRepo, userRepo, authService, cfg.getMailer(), {
        allowedTenants: cfg.allowedTenants,
        getVerifyUrl: () => cfg.getVerifyUrl(),
        shouldSendRealEmails: () => cfg.shouldSendRealEmails(),
    });
    const checkEmailVerificationStatus = new CheckEmailVerificationStatus(securityRepo);
    return {
        generateEmailVerification,
        checkEmailVerificationStatus,
    };
}
//# sourceMappingURL=emailContainer.js.map