import { FieldValue } from "firebase-admin/firestore";
import { SecuritySettingsModel } from "../domain/model/securitySettings.js";

export const getDefaultSecuritySettings = (): SecuritySettingsModel => ({
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
