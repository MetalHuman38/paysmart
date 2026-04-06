# Next 10 Migration Commits

This sequence is intentionally strict. Each commit should leave the app compiling and should not mix unrelated cleanup.

## 1. Unblock `:feature:home` with wallet balance contract

Status: `done`

- Add `WalletBalanceGateway` to `:data:wallet`
- Bind the current app-owned `WalletBalanceRepository` to that interface
- Update home and wallet consumers to inject the interface instead of the concrete app class

## 2. Move home detail viewmodels into `:feature:home`

Status: `done`

- Move `BalanceDetailsViewModel`
- Move `RewardDetailsViewModel`
- Keep package names unchanged
- Validate:
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest`

## 3. Extract notification contracts for home

Status: `done`

- Add a notifications data boundary for:
  - inbox item stream
  - unread count
  - latest notification
- Stop `HomeViewModel` depending directly on app-owned notification implementation

## 4. Extract recent recipient contract for home

Status: `done`

- Move or abstract `RecentSendRecipientRepository`
- Keep send-money feature ownership separate from the home module
- Make `HomeViewModel` depend only on a stable contract

## 5. Move `HomeViewModel` and remaining headless home support

Status: `done`

- Move `HomeViewModel`
- Move any remaining non-UI home mappers/helpers
- Remove duplicate `HomeUiState` ownership from `:app`

## 6. Move home UI routes and composables

Status: `done`

- Move home routes, cards, and components that no longer depend on app-only classes
- Keep root nav graph wiring in `:app`
- Leave only shell-level integration in app navigation

## 7. Finish `:feature:profile`

Status: `in progress`

- [x] Move portable profile helpers and types:
  - `ProfileAboutActionItem`
  - `KycDocumentType`
  - `KycCountryDocuments`
  - `ProfileInfoDivider`
  - `resolveTierLabel`
  - `PermissionState`
- [x] Move the safe profile UI slice:
  - `ProfileScreen`
  - `ProfileDetailsScreen`
  - `ProfilePrivacySettingsScreen`
  - `ProfileSecurityPrivacyScreen`
  - `ProfileAbout*`
  - `ProfilePhotoPicker*`
- [x] Make `:feature:profile` resource-complete for the safe UI slice
- [ ] Move the last app-owned display helpers still tied to app-owned language/capability catalogs
- [ ] Move the remaining wallet/capability/account-information profile surfaces after `:feature:wallet` and shared capability boundaries exist
- [ ] Remove app-owned profile implementation leftovers once those blocked surfaces leave `:app`

## 8. Finish `:feature:account`

Status: `in progress`

- [x] Turn `:feature:account` into a Compose-capable UI module
- [x] Share account brand/footer resources through `:core:ui`
- [x] Move the safe account UI slice:
  - passcode cards/components
  - `SetPasscodeScreen`
  - `VerifyPasscodeScreen`
  - change-passcode flow:
    - `ChangePasscodeScreen`
    - `ChangePasscodeBiometricGateScreen`
    - `ChangePasscodeViewModel`
  - password screens:
    - `CreatePasswordScreen`
    - `EnterPasswordScreen`
  - passkey cards/components/utils
  - `PasskeySetupScreen`
  - `ProfilePasskeySettingsScreen`
  - `MfaNudgeScreen`
  - `MfaSignInChallengeScreen`
- [x] Make `:feature:account` resource-complete for the moved passkey/MFA surfaces
- [x] Abstract `PasscodePrompt` so it no longer depends on the app-owned `SecurityViewModel`
- [x] Abstract the email verification notification dependency behind a feature-owned gateway
- [x] Move the email verification sent-state viewmodel into `:feature:account`
- [x] Rebalance shared headless account security services into `:core:security`
- [ ] Move remaining account viewmodels/helpers that still sit in `:app`
- [ ] Keep only root onboarding/auth shell wiring in `:app`
- [ ] Move the remaining app-owned account screens after their branding/onboarding/address dependencies are shared

## 9. Build `:feature:wallet` + `:data:notifications`

Status: `done`

- [x] Create `:feature:wallet`
- [x] Move the first safe add-money state/resolver slice:
  - `AddMoneyUiState`
  - `resolvePreferredAddMoneyProvider`
  - `addMoneyUnavailableMessage`
  - `addMoneyUnavailableSupportingText`
  - `AddMoneySessionVisualState`
  - `resolveAddMoneySessionVisualState`
  - `shouldShowStandaloneSessionInfo`
- [x] Move add-money / funding-account / managed-card UI state and screens
- [x] Extract `:data:notifications`
- [x] Move clean notification gateway adapters + DI modules into `:data:notifications`
- [x] Keep FCM/runtime notification glue in `:app` until activity/resource coupling is removed
- [x] Validate:
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest`
  - `:app:compileDebugAndroidTestKotlin`
  - `:app:assembleRelease`
  - `:app:lintDebug`

## 10. Build `:feature:notifications`, then resume shell reduction

Status: `done`

- Create `:feature:notifications`
- Move notification center UI/viewmodel/state/cards out of `:app`
- Leave these in `:app` by design:
  - `NotificationBootstrapper`
  - `NotificationChannelRegistrar`
  - `PaySmartFirebaseMessagingService`
  - `PushMessageNotifier`
- Validation:
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest`
  - `:app:lintDebug`
  - `:app:assembleRelease`

## Next Cleanup

Status: `next`

- Finish the blocked `:feature:profile` surfaces:
  - account information
  - account limits
  - account statement
  - connected accounts
- Finish the blocked `:feature:account` surfaces:
  - login and recovery screens
  - create-account / OTP / onboarding screens
  - email verification sent/success screens
  - address setup flow
- Reduce `:app` to shell-only ownership after those surfaces leave
- Run device smoke coverage for auth, home, wallet, notifications, invoice, passkeys, and biometric unlock
