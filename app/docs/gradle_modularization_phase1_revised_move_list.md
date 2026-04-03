# Revised Phase 1 Move List

## Scope

This document replaces the optimistic Phase 1 extraction list with the files that are actually module-safe today.

The rule for this pass is strict:

- move only files that do not still depend on `Country.kt`, app-owned security models, `AuthApiConfig`, app-check wiring, Room, feature-local navigation, or feature-local ViewModels
- keep package names stable
- compile `:app:compileDebugKotlin` after each module extraction

Phase 1 execution order remains:

1. `:core:common`
2. `:core:navigation`
3. `:core:ui`
4. `:core:security`

## `:core:common`

| File | Decision | Notes |
| --- | --- | --- |
| `app/src/main/java/net/metalbrain/paysmart/utils/FailureCounter.kt` | Move now | Pure Kotlin utility. |
| `app/src/main/java/net/metalbrain/paysmart/utils/LocaleUtils.kt` | Move now | Android locale helper with no feature coupling. |
| `app/src/main/java/net/metalbrain/paysmart/utils/NormalizeProviders.kt` | Move now | Pure normalization helper. |
| `app/src/main/java/net/metalbrain/paysmart/utils/PhoneNumberUtils.kt` | Move now | Depends only on `libphonenumber`. |
| `app/src/main/java/net/metalbrain/paysmart/utils/ppCoroutineScope.kt` | Move now | Shared qualifier annotations. |
| `app/src/main/java/net/metalbrain/paysmart/utils/SessionLockedResponse.kt` | Move now | Shared exception type. |
| `app/src/main/java/net/metalbrain/paysmart/utils/ThrottledLogger.kt` | Move now | Shared logging helper. |
| `app/src/main/java/net/metalbrain/paysmart/utils/ToHex.kt` | Move now | Pure byte formatting helper. |
| `app/src/main/java/net/metalbrain/paysmart/utils/PasswordChecks.kt` | Move now | Reclassify from security to common so `RequirementsList` and `StrengthMeter` can move with `:core:ui`. |
| `app/src/main/java/net/metalbrain/paysmart/utils/CountryDetectionUtils.kt` | Keep for later | Depends on `Country.kt`, which still lives in `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/utils/DebounceUtils.kt` | Keep for later | Compose-scoped utility; move later with a broader UI helper sweep if still needed. |
| `app/src/main/java/net/metalbrain/paysmart/utils/ErrorUtils.kt` | Keep for later | Low payoff utility; not worth expanding Phase 1 scope. |
| `app/src/main/java/net/metalbrain/paysmart/utils/KeyGenUtil.kt` | Keep for later | Security utility; move with `:core:security`. |
| `app/src/main/java/net/metalbrain/paysmart/utils/LaunchBiometric.kt` | Keep for later | Uses app resources and activity-bound prompt copy. |
| `app/src/main/java/net/metalbrain/paysmart/utils/RoomKeyProvider.kt` | Keep for later | Belongs with Room/database extraction. |
| `app/src/main/java/net/metalbrain/paysmart/utils/SecureRoomKeyProvider.kt` | Keep for later | Belongs with Room/database extraction. |

## `:core:navigation`

| File | Decision | Notes |
| --- | --- | --- |
| `app/src/main/java/net/metalbrain/paysmart/navigator/NavControllerExt.kt` | Move now | Pure nav extension layer. |
| `app/src/main/java/net/metalbrain/paysmart/navigator/NavGraph.kt` | Keep for later | Contains `AppNavGraph`, deep-link handling, and launch-interest helpers tied to app state and Firebase. |
| `app/src/main/java/net/metalbrain/paysmart/navigator/AuthNavGraph.kt` | Keep for later | Graph composition depends on feature screens and Hilt ViewModels. |
| `app/src/main/java/net/metalbrain/paysmart/navigator/HomeNavGraph.kt` | Keep for later | Graph composition depends on feature screens and Hilt ViewModels. |
| `app/src/main/java/net/metalbrain/paysmart/navigator/InvoiceNavGraph.kt` | Keep for later | Graph composition depends on feature screens and Hilt ViewModels. |
| `app/src/main/java/net/metalbrain/paysmart/navigator/ProfileNavGraph.kt` | Keep for later | Graph composition depends on feature screens and Hilt ViewModels. |
| `app/src/main/java/net/metalbrain/paysmart/navigator/WalletNavGraph.kt` | Keep for later | Graph composition depends on feature screens and Hilt ViewModels. |

Navigation split rule for Phase 1:

- create a new `Screen.kt` in `:core:navigation` only for pure route declarations and route builders that do not depend on `Country.kt`, `UserUiState`, `FirebaseAuth`, `LocalActivity`, or `LocalContext`
- keep the launch-interest helpers and the `AppNavGraph` composable in `:app`

## `:core:ui`

| File | Decision | Notes |
| --- | --- | --- |
| `app/src/main/java/net/metalbrain/paysmart/ui/animate/AnimatedLottieBackground.kt` | Move now | Shared visual primitive; move matching shared raw assets with it. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/AccountSwitchPrompt.kt` | Move now | Reusable auth CTA component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/AuthProviderButton.kt` | Move now | Reusable button shell. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/AuthText.kt` | Move now | Reusable text primitives. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/BackendErrorModal.kt` | Move now | Shared error surface. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/BiometricToggleRow.kt` | Move now | Shared settings row. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/CatalogSelectionBottomSheet.kt` | Move now | Generic bottom-sheet UI shell. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/CheckboxWithText.kt` | Move now | Pure reusable component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/CircularProgressWithText.kt` | Move now | Pure reusable component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/CustomInput.kt` | Move now | Pure reusable component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/DontHaveAnAccount.kt` | Move now | Reusable auth CTA wrapper. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/EmailInputField.kt` | Move now | Pure reusable component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/EmailSignBtn.kt` | Move now | Reusable button variant. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/FaceBookSignInBtn.kt` | Move now | Reusable button variant. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/FilterTab.kt` | Move now | Tab shell is reusable. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/HaveAnAccount.kt` | Move now | Reusable auth CTA wrapper. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/OtpTextFieldRow.kt` | Move now | Pure reusable component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/OutlinedButton.kt` | Move now | Shared button wrapper. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/PasscodeField.kt` | Move now | Shared input component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/PasskeySignBtn.kt` | Move now | Reusable button variant. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/PasswordInput.kt` | Move now | Pure reusable input field. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/PhoneAlreadyRegisteredSheet.kt` | Move now | Shared sheet UI. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/PrimaryButton.kt` | Move now | Shared button primitive. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/RequirementsList.kt` | Move now | Safe after `PasswordChecks.kt` moves to `:core:common`. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/SecondaryButton.kt` | Move now | Shared button primitive. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/SetUpStep.kt` | Move now | Shared onboarding/security step component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/SmallButton.kt` | Move now | Shared button primitive. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/StrengthMeter.kt` | Move now | Safe after `PasswordChecks.kt` moves to `:core:common`. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/TermsAndPrivacy.kt` | Move now | Shared legal copy component. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/AppBackground.kt` | Move now | Shared theme token. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/ButtonTokens.kt` | Move now | Shared theme token. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/Color.kt` | Move now | Shared theme token. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/Dimens.kt` | Move now | Shared theme token. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/HomeCardTokens.kt` | Move now | Shared design token set. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/Shapes.kt` | Move now | Shared theme token. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/Theme.kt` | Move now | Move together with `AppThemeVariant.kt`. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/ThemePack.kt` | Move now | Move together with `AppThemeVariant.kt`. |
| `app/src/main/java/net/metalbrain/paysmart/ui/theme/Type.kt` | Move now | Shared typography. |
| `app/src/main/java/net/metalbrain/paysmart/utils/ShakeAnimation.kt` | Move now | Compose animation utility. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/theme/data/AppThemeVariant.kt` | Move now | Supporting prerequisite for `Theme.kt` and `ThemePack.kt`; keep package name stable. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/EmailVerificationBtn.kt` | Keep for later | Depends on `NavController` and `Screen`. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/FilterTabs.kt` | Keep for later | Depends on transaction feature types. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/GoogleSignInBtn.kt` | Keep for later | Depends on auth-provider code. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/LanguageSelector.kt` | Keep for later | Depends on capability/language feature data. |
| `app/src/main/java/net/metalbrain/paysmart/ui/components/PhoneNumberInput.kt` | Keep for later | Depends on `Country.kt`, which still lives in `:app`. |

Resource rule for `:core:ui`:

- move shared theme colors, shared dimensions, shared typography, and only the drawables/raw assets directly used by the moved files above
- keep feature-specific strings and icons in `:app` until the relevant feature module is extracted

## `:core:security`

The original security list was too broad for Phase 1. A large part of security still depends on app-owned models, Room, `AuthApiConfig`, app-check wiring, Firebase auth/session wiring, and address/profile repositories. The safe Phase 1 extraction is the pure contract and local engine slice below.

| File | Decision | Notes |
| --- | --- | --- |
| `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateDecision.kt` | Move now | Pure decision model. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureKey.kt` | Move now | Pure enum contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureRequirement.kt` | Move now | Pure enum contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/SecurityStrengthMethod.kt` | Move now | Pure enum contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/SecurityStrengthScore.kt` | Move now | Pure value object. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/BcryptPasswordHasher.kt` | Move now | Pure hashing interface. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordHasher.kt` | Move now | Pure bcrypt implementation. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordRepository.kt` | Move now | Pure repository contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/CryptoFile.kt` | Move now | Local secure file helper; move together with `SecureKeyAlias.kt`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordCryptoFile.kt` | Move now | Local secure file helper. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/provider/BiometricHelper.kt` | Move now | Standalone biometric engine; depends only on Android biometric APIs and `FailureCounter`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/repository/PasskeyCredentialManager.kt` | Move now | Standalone Android Credentials wrapper. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaEnrollmentChallenge.kt` | Move now | Pure MFA contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaEnrollmentStatus.kt` | Move now | Pure MFA contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaSignInChallenge.kt` | Move now | Pure MFA contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaSignInFactorOption.kt` | Move now | Pure MFA contract. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/MfaEnrollmentProvider.kt` | Move now | Contract only. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/MfaSignInProvider.kt` | Move now | Contract only. |
| `app/src/main/java/net/metalbrain/paysmart/core/security/SecureKeyAlias.kt` | Move now | Supporting prerequisite for `CryptoFile.kt`. |
| `app/src/main/java/net/metalbrain/paysmart/utils/KeyGenUtil.kt` | Move now | Pure security helper. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureAccessPolicy.kt` | Keep for later | Depends on `LocalSecuritySettingsModel`, which still lives in `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/data/SecurityMigrationFlags.kt` | Keep for later | Keep with the current security model layer until those models leave `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/data/SecurityParity.kt` | Keep for later | Depends on `SecuritySettingsModel` and `LocalSecuritySettingsModel`, both still in `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/data/SecurityPreference.kt` | Keep for later | Depends on app-owned security model layer. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/di/CoreSecurityModule.kt` | Keep for later | Depends on `RoomKeyManager`, which still lives under the Room layer in `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/manager/DefaultSecurityManager.kt` | Keep for later | Depends on repositories that still live in `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/manager/SecuritySyncManager.kt` | Keep for later | Depends on Room and auth session logging. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/remote/SecuritySettingsHandler.kt` | Keep for later | Depends on app-network policy client still in `:app`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/remote/SecuritySettingsPolicy.kt` | Keep for later | Depends on `AuthApiConfig`, app-check wiring, and `Env`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/repository/SecurityRepository.kt` | Keep for later | Depends on app auth, address, and Room abstractions. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/repository/SecurityRepositoryInterface.kt` | Keep for later | Keep with the concrete repository until the security model layer moves. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/remote/BiometricPolicyClient.kt` | Keep for later | Depends on `AuthApiConfig` and app network config. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/remote/BiometricPolicyHandler.kt` | Keep for later | Depends on `AuthApiConfig` and `Env`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/repository/BiometricRepository.kt` | Keep for later | Depends on `SecurityPreference`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/remote/PassCodePolicyClient.kt` | Keep for later | Depends on `AuthApiConfig`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/remote/PassCodePolicyHandler.kt` | Keep for later | Depends on `AuthApiConfig` and `Env`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/repository/PasscodeRepository.kt` | Keep for later | Depends on `CryptoUseCase` and `SecurityPreference`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordPolicyClient.kt` | Keep for later | Depends on `AuthApiConfig`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordPolicyHandler.kt` | Keep for later | Depends on `AuthApiConfig`. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/SecurePasswordRepository.kt` | Keep for later | Depends on `UserManager`, `SecurityPreference`, and server policy wiring. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/repository/PasskeyApiRepository.kt` | Keep for later | Depends on app auth/session/app-check wiring. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/FirebaseMfaEnrollmentProvider.kt` | Keep for later | Depends on Firebase/session wiring. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/FirebaseMfaSignInProvider.kt` | Keep for later | Depends on Firebase/session wiring. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/remote/MfaEnrollmentPromptPolicyClient.kt` | Keep for later | Depends on app network config. |
| `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/remote/MfaEnrollmentPromptPolicyHandler.kt` | Keep for later | Depends on app network config. |
| `app/src/main/java/net/metalbrain/paysmart/utils/LaunchBiometric.kt` | Keep for later | Uses app resources and activity-bound prompt copy. |

Keep all security screens, cards, viewmodels, and graph-facing UI in `:app` for Phase 1. They should move later with onboarding/profile/security feature modules, not with the core engine.

## Practical notes

- `:core:common` is still the correct first extraction. It validates the module wiring pattern with the least risk.
- `:core:navigation` should stay intentionally small in Phase 1. Do not move graph composition yet.
- `:core:ui` is safe only with the exclusions above. The original list was too broad.
- `:core:security` is the smallest safe slice of the four. A larger security extraction should wait until the security model layer, auth/network layer, and Room layer are no longer owned by `:app`.

## Validation after each module

Run after each extraction step:

- `:app:compileDebugKotlin`

Run after the full Phase 1 slice:

- `:app:compileDebugUnitTestKotlin`
- `:app:compileDebugAndroidTestKotlin`
