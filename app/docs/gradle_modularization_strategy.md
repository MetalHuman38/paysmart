# PaySmart Gradle Modularization Strategy

_Last updated: 2026-04-02_

## 1) Objective
PaySmart already enables Gradle build cache, configuration cache, non-transitive `R`, and parallel workers. The current build bottleneck is not missing flags. It is that most runtime code still compiles through `:app`.

This document defines the migration strategy to move PaySmart toward the same practical outcome achieved in `C:\Users\Metal\builders`:
- real Gradle parallelism
- smaller invalidation radius
- cleaner separation of concern
- safer feature ownership

This is an Android-first modularization plan. It does not copy Builders' multiplatform setup wholesale.

## 2) What Builders Is Doing That PaySmart Is Not
Builders exposes many small projects in `settings.gradle.kts`:
- `:androidApp`
- `:composeApp`
- `:core:*`
- `:feature:*`
- `:server`
- `:shared`

Those modules have narrow dependency edges, so Gradle can schedule and cache work independently.

PaySmart now exposes a broader modular graph:
- `:app`
- `:core:common`
- `:core:navigation`
- `:core:ui`
- `:core:security`
- `:core:database`
- `:core:firebase`
- `:core:invoice-models`
- `:core:models`
- `:data:auth`
- `:data:user`
- `:data:wallet`
- `:data:invoice`
- `:data:notifications`
- `:feature:account`
- `:feature:profile`
- `:feature:home`
- `:feature:notifications`
- `:feature:wallet`

That is already a meaningful improvement over the earlier single-module-heavy state, but `:app` still owns too much runtime wiring and account-flow implementation. The next wins come from tightening boundaries, not from keeping standalone experimental modules around.

That means almost every Android edit dirties `:app`, even though the codebase is already logically separated into packages such as:
- `core/features/*`
- `navigator/*`
- `ui/*`
- `domain/*`
- `data/*`
- `room/*`

## 3) Current State Summary

### Android
- Build flags are already enabled in `gradle.properties`.
- `:app` still owns most UI, navigation, security, Firebase integration, Room, and feature orchestration.
- Navigation has already been split by file under `app/src/main/java/net/metalbrain/paysmart/navigator`, which is a good precursor for module extraction.
- Shared UI already exists as a recognizable layer under `ui/theme`, `ui/components`, and some reusable feature cards.

### Functions
The server is already cleanly layered under:
- `application`
- `domain`
- `handlers`
- `http`
- `infrastructure`
- `services`
- `workers`

That server structure should remain stable while the Android app is modularized. The mobile module migration must not change the external contract of:
- user profile shape
- security settings shape
- invoice routes and invoice document shape
- notification routes and notification document shape

## 4) Migration Principles
1. Keep package names stable during extraction.
   Move source roots into new modules first. Do not rename Kotlin packages in Phase 1.

2. Extract low-risk shared code before feature code.
   Shared UI, common helpers, navigation contracts, and security engines should move before full features.

3. Keep `:app` as the shell.
   `:app` should compose modules, wire Hilt, host the manifest, and own top-level startup only.

4. Do not move server contracts during Android modularization.
   Android module extraction is an internal refactor. API routes and Firestore document shapes must remain stable.

5. Extract feature modules only after the core modules are in place.
   Invoicing, notifications, identity, home, wallet, and profile become much easier once `:core:ui`, `:core:common`, `:core:navigation`, and `:core:security` exist.

## 5) Recommended Target Module Graph

### App shell
- `:app`

### Core
- `:core:models`
- `:core:invoice-models`
- `:core:common`
- `:core:ui`
- `:core:navigation`
- `:core:security`
- `:core:database`
- `:core:firebase`

### Features
- `:feature:onboarding`
- `:feature:home`
- `:feature:wallet`
- `:feature:invoicing`
- `:feature:identity`
- `:feature:notifications`
- `:feature:profile`
- `:feature:help`

### Optional later data split
- `:data:auth`
- `:data:user`
- `:data:wallet`
- `:data:invoice`
- `:data:notifications`

## 6) Phased Rollout

### Phase 1
Create the shared modules that reduce `:app` coupling without moving major features:
- `:core:ui`
- `:core:common`
- `:core:navigation`
- `:core:security`

### Phase 2
Pilot feature extraction with the cleanest seams:
- `:feature:invoicing`
- `:feature:notifications`
- `:feature:identity`

Theme stabilization should happen before those feature extractions. See:
- `app/docs/phase2_theme_and_feature_strategy.md`

### Phase 3
Move infrastructure out of `:app`:
- `:core:database`
- `:core:firebase`

Execution detail and the app-shell target are mapped here:
- `app/docs/phase3_infrastructure_and_app_shell_strategy.md`

### Phase 4
Extract the remaining heavy surfaces:
- `:feature:home`
- `:feature:wallet`
- `:feature:profile`
- `:feature:onboarding`

## 7) Functions Sync Rules During Migration
Android modularization must keep these backend contracts stable.

### User profile
Keep `launchInterest` on `users/{uid}`.

Files:
- `functions/src/api/src/domain/repository/UserRepository.ts`
- `functions/src/api/src/infrastructure/firestore/FirestoreUserRepository.ts`

Rule:
- Android can move profile, onboarding, and home code into modules.
- The canonical user field name remains `launchInterest`.
- Valid values remain `"invoice"` and `"top_up"`.

### Security settings
Keep onboarding progress and security state under `users/{uid}/security/settings`.

Files:
- `functions/src/api/src/domain/model/securitySettings.ts`
- `functions/src/api/src/application/usecase/GenerateEmailVerification.ts`
- `functions/src/api/src/application/usecase/CheckEmailVerificationStatus.ts`
- `functions/src/api/src/application/usecase/EnsureSecuritySettings.ts`

Rule:
- `launchInterest` does not move into security settings.
- `onboardingRequired` and `onboardingCompleted` remain procedural flags only.

### Invoices
Keep invoice routes and invoice document shapes stable while Android moves invoicing into a feature module.

Files:
- `functions/src/api/src/http/invoice.route.ts`
- `functions/src/api/src/domain/model/invoice.ts`
- `functions/src/api/src/infrastructure/firestore/FirestoreInvoiceRepository.ts`
- `functions/src/api/src/services/invoicePdfRenderer.ts`

Rule:
- Android modularization must not change:
  - `POST /invoices/finalize`
  - `GET /invoices`
  - `GET /invoices/:invoiceId`
  - `POST /invoices/:invoiceId/pdf`
  - `GET /invoices/:invoiceId/pdf/download`
- `pay-smart-invoice-v2` remains the active PDF template version during module extraction.

### Notifications
Keep notification installation and preferences endpoints stable while notification code is extracted.

Files:
- `functions/src/api/src/http/notification.route.ts`
- `functions/src/api/src/infrastructure/firestore/FirestoreNotificationPreferencesRepository.ts`

Rule:
- Android modularization must not change:
  - `/notifications/installations/register`
  - `/notifications/preferences`
  - `/email/unsubscribe`

## 8) Exact Phase 1 Module Extraction List

For the audited, executable Phase 1 file list, see:

- `app/docs/gradle_modularization_phase1_revised_move_list.md`

Phase 1 should not move whole business features yet. It should create the shared foundations the later feature modules will depend on.

### 8.1 `:core:ui`
Create `core/ui/build.gradle.kts` and move the following files as-is.

#### Source files
- `app/src/main/java/net/metalbrain/paysmart/ui/animate/AnimatedLottieBackground.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/AccountSwitchPrompt.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/AuthProviderButton.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/AuthText.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/BackendErrorModal.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/BiometricToggleRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/CatalogSelectionBottomSheet.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/CheckboxWithText.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/CircularProgressWithText.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/CustomInput.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/DontHaveAnAccount.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/EmailInputField.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/EmailSignBtn.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/EmailVerificationBtn.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/FaceBookSignInBtn.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/FilterTab.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/FilterTabs.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/GoogleSignInBtn.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/HaveAnAccount.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/LanguageSelector.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/OtpTextFieldRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/OutlinedButton.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/PasscodeField.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/PasskeySignBtn.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/PasswordInput.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/PhoneAlreadyRegisteredSheet.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/PhoneNumberInput.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/PrimaryButton.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/RequirementsList.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/SecondaryButton.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/SetUpStep.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/SmallButton.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/StrengthMeter.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/components/TermsAndPrivacy.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/AppBackground.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/ButtonTokens.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/Color.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/Dimens.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/HomeCardTokens.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/Shapes.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/Theme.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/ThemePack.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/theme/Type.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/ShakeAnimation.kt`

#### Resource families to move with `:core:ui`
Move only shared design resources in Phase 1:
- shared theme colors
- shared dimensions
- shared typography resources
- shared vector assets used by the reusable UI components above

Keep feature-specific strings and icons in `:app` until the relevant feature module is extracted.

### 8.2 `:core:common`
Create `core/common/build.gradle.kts` and move the following files as-is.

#### Source files
- `app/src/main/java/net/metalbrain/paysmart/utils/CountryDetectionUtils.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/DebounceUtils.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/ErrorUtils.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/FailureCounter.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/LocaleUtils.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/NormalizeProviders.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/PhoneNumberUtils.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/ppCoroutineScope.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/SessionLockedResponse.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/ThrottledLogger.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/ToHex.kt`

#### Keep in `:app` for now
These should not move in Phase 1:
- `app/src/main/java/net/metalbrain/paysmart/utils/KeyGenUtil.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/LaunchBiometric.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/PasswordChecks.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/RoomKeyProvider.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/SecureRoomKeyProvider.kt`

They should move later with `:core:security` or `:core:database`.

### 8.3 `:core:navigation`
Phase 1 navigation extraction is intentionally partial. Do not move all graph composition files yet, because they still depend directly on feature screens inside `:app`.

Create `core/navigation/build.gradle.kts`.

#### Move as-is
- `app/src/main/java/net/metalbrain/paysmart/navigator/NavControllerExt.kt`

#### Split before move
Split `app/src/main/java/net/metalbrain/paysmart/navigator/NavGraph.kt` into:
- a new `Screen.kt` file in `:core:navigation`
- a new `ScreenOrigin.kt` file in `:core:navigation` if kept separate
- optional small route helper files in `:core:navigation`

Keep these files in `:app` in Phase 1:
- `app/src/main/java/net/metalbrain/paysmart/navigator/NavGraph.kt`
- `app/src/main/java/net/metalbrain/paysmart/navigator/AuthNavGraph.kt`
- `app/src/main/java/net/metalbrain/paysmart/navigator/HomeNavGraph.kt`
- `app/src/main/java/net/metalbrain/paysmart/navigator/InvoiceNavGraph.kt`
- `app/src/main/java/net/metalbrain/paysmart/navigator/ProfileNavGraph.kt`
- `app/src/main/java/net/metalbrain/paysmart/navigator/WalletNavGraph.kt`

Reason:
- moving the full graph files now would invert the dependency direction and make `:core:navigation` depend on feature screens

### 8.4 `:core:security`
Create `core/security/build.gradle.kts`.

Phase 1 security extraction should move the shared security engine, policies, repositories, and non-screen contracts. Keep security screens in `:app` until later feature extraction.

#### Move as-is
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureAccessPolicy.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateDecision.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureKey.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureRequirement.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/SecurityStrengthMethod.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/SecurityStrengthScore.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/data/SecurityMigrationFlags.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/data/SecurityParity.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/data/SecurityPreference.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/di/CoreSecurityModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/manager/DefaultSecurityManager.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/manager/SecuritySyncManager.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/remote/SecuritySettingsHandler.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/remote/SecuritySettingsPolicy.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/repository/SecurityRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/repository/SecurityRepositoryInterface.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/provider/BiometricHelper.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/remote/BiometricPolicyClient.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/remote/BiometricPolicyHandler.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/repository/BiometricRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/remote/PassCodePolicyClient.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/remote/PassCodePolicyHandler.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/repository/PasscodeRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/BcryptPasswordHasher.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/CryptoFile.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordCryptoFile.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordHasher.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordPolicyClient.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordPolicyHandler.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/PasswordRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/repository/SecurePasswordRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/repository/PasskeyApiRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/repository/PasskeyCredentialManager.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaEnrollmentChallenge.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaEnrollmentStatus.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaSignInChallenge.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/data/MfaSignInFactorOption.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/FirebaseMfaEnrollmentProvider.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/FirebaseMfaSignInProvider.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/MfaEnrollmentProvider.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/provider/MfaSignInProvider.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/remote/MfaEnrollmentPromptPolicyClient.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/remote/MfaEnrollmentPromptPolicyHandler.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/KeyGenUtil.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/LaunchBiometric.kt`
- `app/src/main/java/net/metalbrain/paysmart/utils/PasswordChecks.kt`

#### Keep in `:app` for now
Do not move these in Phase 1:
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateRequirementRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateSheetCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateStrengthCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/featuregate/FeatureGateText.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/screen/BiometricOptInScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/screen/BiometricSessionUnlock.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/biometric/viewmodel/BiometricOptInViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/card/BrandFooter.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/card/ChangePasscodeCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/card/PasscodeMessageCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/card/PasscodeSetupCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/component/NumberPad.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/component/NumberPadKey.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/component/PasscodeIndicatorRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/screen/ChangePasscodeBiometricGateScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/screen/ChangePasscodeScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/screen/PasscodePrompt.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/screen/SetPasscodeScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/screen/VerifyPasscodeScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/viewmodel/ChangePasscodeViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/viewmodel/PassCodeViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/passcode/viewmodel/VerifyPasscodeViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/screen/CreatePasswordScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/screen/EnterPasswordScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/viewmodel/CreatePasswordViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/authorization/password/viewmodel/EnterPasswordViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/cards/PasskeySurfaceCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/CredentialRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeyBackButton.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeyGlowSwitch.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeySecurityComponents.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeySecurityIcon.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeySecurityPanel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/screen/PasskeySetupScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/screen/ProfilePasskeySettingsScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/state/PasskeySetupUiState.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/utils/passkeyContentPadding.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/viewmodel/PasskeySetupViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/di/MfaModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/screen/MfaNudgeScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/screen/MfaSignInChallengeScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/viewmodel/MfaNudgeViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/mfa/viewmodel/MfaSignInViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/security/viewmodel/SecurityViewModel.kt`

Reason:
- these files are still screen- and feature-facing
- they should move later with onboarding/profile/security feature modules, not with the core engine

## 9) Root Build Changes Required For Phase 1

### Update `settings.gradle.kts`
Add:
- `include(":core:common")`
- `include(":core:ui")`
- `include(":core:navigation")`
- `include(":core:security")`

Optional but recommended:
- `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`

### Add new module build files
Create:
- `core/common/build.gradle.kts`
- `core/ui/build.gradle.kts`
- `core/navigation/build.gradle.kts`
- `core/security/build.gradle.kts`

### Update `app/build.gradle.kts`
Replace direct ownership with:
- `implementation(project(":core:common"))`
- `implementation(project(":core:ui"))`
- `implementation(project(":core:navigation"))`
- `implementation(project(":core:security"))`

## 10) Validation Checklist For Phase 1
Phase 1 is complete only when the following pass:
- `:app:compileDebugKotlin`
- `:app:compileDebugUnitTestKotlin`
- `:app:compileDebugAndroidTestKotlin`

Manual checks:
- account creation still works
- login still works
- passcode, password, biometric, and passkey flows still work
- feature gates still route correctly
- startup, splash, and app loading still render correctly

Functions checks:
- no route changes required in `functions/src/api`
- `launchInterest` still persists on `users/{uid}`
- `security/settings` still updates without schema change
- invoice finalize and PDF flows still work
- notification registration still works

## 11) What Must Stay In `:app` After Phase 1
To keep the first slice safe, these areas should remain in `:app` after Phase 1:
- graph composition files
- onboarding screens
- login/create-account screens
- home screens
- wallet screens
- invoicing screens
- identity screens
- profile screens
- Room database and DAOs
- Firebase and API modules in `di/*`

That restraint is what keeps Phase 1 small enough to finish without breaking the runtime graph.

## 12) Recommended Next Step After Phase 1
After Phase 1 stabilizes, the first full feature module should be `:feature:invoicing`.

Reason:
- the invoicing package is already internally cohesive
- it has clear route boundaries
- it has a stable backend contract under `/invoices/*`
- it is central to the UK invoice-led product direction
