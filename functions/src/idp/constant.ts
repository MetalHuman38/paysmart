export const DEFAULT_SECURITY = {
  passcodeEnabled: false,
  passwordEnabled: false,
  biometricsRequired: true,
  lockAfterMinutes: 5,
  onboardingRequired: {
    set_passcode: true,
    enable_biometrics: true,
    accept_tos: true,
  },
  onboardingCompleted: {},
  hasVerifiedEmail: false,
  hasAddedHomeAddress: false,
  hasVerifiedIdentity: false,
  localPassCodeSetAt: null,
  localPasswordSetAt: null,
};