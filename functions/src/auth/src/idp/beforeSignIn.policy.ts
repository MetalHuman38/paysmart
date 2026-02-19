import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import { buildAuthContainer } from "../Infra/di/authContainer.js";
import { BeforeSignInPolicyUsecase } from "../application/usecase/BeforeSignInPolicyUsecase.js";
import { logEvent } from "../utils.js";

export async function beforeSignInPolicy(event: AuthBlockingEvent) {
  const { securitySettingsRepo, auditLogRepo, authService, authSessionRepo } = buildAuthContainer();
  const usecase = new BeforeSignInPolicyUsecase(
    securitySettingsRepo,
    auditLogRepo,
    authService,
    authSessionRepo
  );

  

  const uid = event.data?.uid ?? null;
  const providerIds = (event.data?.providerData ?? []).map((p) => p.providerId);
  // Log the entry of the beforeSignInPolicy function with relevant details
  logEvent("before-signin:entry", {
    uid,
    providerIds,
    timestamp: new Date().toISOString(),
  });

  // Execute the use case and log the outcome
  try {
    const result = await usecase.execute(event);
    logEvent("before-signin:success", { uid, providerIds });
    return result;
  } catch (error) {
    const errorCode = error instanceof Error && 'code' in error ? (error as any).code : 'UNKNOWN';
    const errorMessage = error instanceof Error ? error.message : 'Unknown error';
    logEvent("before-signin:error", {
      uid,
      providerIds,
      code: errorCode,
      message: errorMessage,
    });
    throw error;
  }
}
