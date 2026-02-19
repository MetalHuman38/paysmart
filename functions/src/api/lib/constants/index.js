import { FieldValue } from "firebase-admin/firestore";
export const getDefaultSecuritySettings = () => ({
    allowFederatedLinking: false,
    passcodeEnabled: false,
    passwordEnabled: false,
    biometricsRequired: true,
    biometricsEnabled: false,
    biometricsEnabledAt: null,
    emailToVerify: null,
    emailVerificationSentAt: null,
    emailVerificationAttemptsToday: 0,
    lockAfterMinutes: 5,
    onboardingRequired: {},
    onboardingCompleted: {},
    hasVerifiedEmail: false,
    hasAddedHomeAddress: false,
    hasVerifiedIdentity: false,
    localPassCodeSetAt: null,
    localPasswordSetAt: null,
    updatedAt: FieldValue.serverTimestamp(),
});
//# sourceMappingURL=index.js.map