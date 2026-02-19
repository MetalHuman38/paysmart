// infrastructure/di/authContainer.ts
import { initDeps } from "../../dependencies.js";
import { FirebaseAuthService } from "../auth/FirebaseAuthService.js";
import { GetUIDFromAuthHeader } from "../../application/usecase/GetUIDFromAuthHeader.js";
import { CheckPhoneAvailability } from "../../application/usecase/CheckPhoneAvailability.js";
import { FirestoreSecuritySettingsRepository } from "../firestore/FirestoreSecuritySettingsRepository.js";
import { FirestoreUserRepository } from "../firestore/FirestoreUserRepository.js";

export function authContainer() {
  const { auth, firestore } = initDeps();

  const authService = new FirebaseAuthService(auth);
  const securityRepo = new FirestoreSecuritySettingsRepository(firestore);
  const userRepo = new FirestoreUserRepository(firestore);

  return {
    getUIDFromAuthHeader: new GetUIDFromAuthHeader(authService),
    checkPhoneAvailability: new CheckPhoneAvailability(authService),
    authService,
    securitySettings: securityRepo,
    userRepo,
  };
}
