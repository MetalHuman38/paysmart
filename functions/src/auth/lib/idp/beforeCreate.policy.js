import { BeforeCreatePolicyUsecase } from "../application/usecase/BeforeCreatePolicyUsecase.js";
import { buildAuthContainer } from "../Infra/di/authContainer.js";
import { logEvent } from "../utils.js";
function maskPhone(phone) {
    if (!phone)
        return null;
    const last4 = phone.slice(-4);
    return `***${last4}`;
}
function maskEmail(email) {
    if (!email)
        return null;
    const trimmed = email.trim();
    const at = trimmed.indexOf("@");
    if (at <= 0)
        return "***";
    const local = trimmed.slice(0, at);
    const domain = trimmed.slice(at + 1);
    const localMasked = local.length <= 2 ? `${local[0] ?? "*"}*` : `${local[0]}***${local.slice(-1)}`;
    return `${localMasked}@${domain}`;
}
export async function beforeCreatePolicy(event) {
    const { authService, securitySettingsRepo } = buildAuthContainer();
    const policyUseCase = new BeforeCreatePolicyUsecase(authService, securitySettingsRepo);
    const uid = event.data?.uid ?? null;
    const user = event.data;
    const phoneNumber = user?.phoneNumber ?? null;
    const email = user?.email ?? null;
    const maskedPhoneNumber = maskPhone(phoneNumber);
    const maskedEmail = maskEmail(email);
    const providerData = (user?.providerData ?? []).map((p) => p.providerId);
    const providerId = event.credential?.providerId ?? null;
    const signInMethod = event.credential?.signInMethod ?? null;
    let uidLookupOutcome = "skipped";
    let uidLookupPhone = null;
    if (uid) {
        try {
            const lookup = await authService.getUserByUid(uid);
            if (!lookup) {
                uidLookupOutcome = "not_found";
            }
            else if (lookup.phoneNumber) {
                uidLookupOutcome = "found_with_phone";
                uidLookupPhone = maskPhone(lookup.phoneNumber);
            }
            else {
                uidLookupOutcome = "found_without_phone";
            }
        }
        catch {
            uidLookupOutcome = "lookup_error";
        }
    }
    logEvent("before-create:entry", {
        uid,
        phoneNumber: maskedPhoneNumber,
        email: maskedEmail,
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
            phoneNumber: maskedPhoneNumber,
            email: maskedEmail,
            providerData,
            providerId,
            signInMethod,
            uidLookupOutcome,
            uidLookupPhone,
        });
    }
    catch (error) {
        logEvent("before-create:error", {
            uid,
            phoneNumber: maskedPhoneNumber,
            email: maskedEmail,
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
//# sourceMappingURL=beforeCreate.policy.js.map