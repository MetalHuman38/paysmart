import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsModel } from "../domain/model/SecuritySettingsModel.js";

export const DEFAULT_ONBOARDING_REQUIRED: Record<string, boolean> = {
  set_passcode: true,
  enable_biometrics: true,
  accept_tos: true,
};

export const getDefaultSecuritySettings = (): SecuritySettingsModel => ({
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
