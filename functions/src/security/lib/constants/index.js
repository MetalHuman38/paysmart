import { FieldValue } from "firebase-admin/firestore";
export const DEFAULT_ONBOARDING_REQUIRED = {
    set_passcode: true,
    enable_biometrics: true,
    accept_tos: true,
};
export const getDefaultSecuritySettings = () => ({
    allowFederatedLinking: false,
    passcodeEnabled: false,
    passwordEnabled: false,
    biometricsRequired: true,
    biometricsEnabled: false,
    lockAfterMinutes: 5,
    onboardingRequired: DEFAULT_ONBOARDING_REQUIRED,
    onboardingCompleted: {},
    hasVerifiedEmail: false,
    hasAddedHomeAddress: false,
    hasVerifiedIdentity: false,
    localPassCodeSetAt: null,
    localPasswordSetAt: null,
    updatedAt: FieldValue.serverTimestamp(),
});
//# sourceMappingURL=index.js.map