import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import { BeforeCreatePolicyUsecase } from "../application/usecase/BeforeCreatePolicyUsecase.js";
import { buildAuthContainer } from "../Infra/di/authContainer.js";
import { logEvent } from "../utils.js";

type UidLookupOutcome =
  | "skipped"
  | "found_with_phone"
  | "found_without_phone"
  | "not_found"
  | "lookup_error";

function maskPhone(phone?: string | null): string | null {
  if (!phone) return null;
  const last4 = phone.slice(-4);
  return `***${last4}`;
}

export async function beforeCreatePolicy(event: AuthBlockingEvent) {
  const { authService, securitySettingsRepo } = buildAuthContainer();
  const policyUseCase = new BeforeCreatePolicyUsecase(
    authService,
    securitySettingsRepo
  );
  const uid = event.data?.uid ?? null;
  const user = event.data;
  const phoneNumber = user?.phoneNumber ?? null;
  const email = user?.email ?? null;
  const providerData = (user?.providerData ?? []).map((p) => p.providerId);
  const providerId = event.credential?.providerId ?? null;
  const signInMethod = event.credential?.signInMethod ?? null;
  let uidLookupOutcome: UidLookupOutcome = "skipped";
  let uidLookupPhone: string | null = null;

  if (uid) {
    try {
      const lookup = await authService.getUserByUid(uid);
      if (!lookup) {
        uidLookupOutcome = "not_found";
      } else if (lookup.phoneNumber) {
        uidLookupOutcome = "found_with_phone";
        uidLookupPhone = maskPhone(lookup.phoneNumber);
      } else {
        uidLookupOutcome = "found_without_phone";
      }
    } catch {
      uidLookupOutcome = "lookup_error";
    }
  }

  logEvent("before-create:entry", {
    uid,
    phoneNumber,
    email,
    providerData,
    providerId,
    signInMethod,
    uidLookupOutcome,
    uidLookupPhone,
  });
  try {
    await policyUseCase.execute(event);
    logEvent("before-create:success", {
      uid,
      phoneNumber,
      email,
      providerData,
      providerId,
      signInMethod,
      uidLookupOutcome,
      uidLookupPhone,
    });
  } catch (error: any) {
    logEvent("before-create:error", {
      uid,
      phoneNumber,
      email,
      providerData,
      providerId,
      signInMethod,
      uidLookupOutcome,
      uidLookupPhone,
      code: error?.code,
      message: error?.message,
    });
    throw error;
  }
}
