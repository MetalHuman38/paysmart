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
- [ ] Move remaining account viewmodels/helpers that still sit in `:app`
- [ ] Keep only root onboarding/auth shell wiring in `:app`
- [ ] Move the remaining app-owned account screens after their branding/onboarding/address dependencies are shared

## 9. Build `:feature:wallet` + `:data:notifications`

Status: `in progress`

- [x] Create `:feature:wallet`
- [x] Move the first safe add-money state/resolver slice:
  - `AddMoneyUiState`
  - `resolvePreferredAddMoneyProvider`
  - `addMoneyUnavailableMessage`
  - `addMoneyUnavailableSupportingText`
  - `AddMoneySessionVisualState`
  - `resolveAddMoneySessionVisualState`
  - `shouldShowStandaloneSessionInfo`
- [ ] Move add-money / FX / funding-account / managed-card UI state and screens
- [ ] Extract `:data:notifications`
- [ ] Keep FCM service in `:app` until activity/resource coupling is removed

## 10. Reduce `:app` to shell and run final verification

- `:app` keeps:
  - `Application`
  - manifest
  - launcher resources
  - root nav host
  - startup orchestration
  - top-level DI bootstrap
- Final validation:
  - `:app:assembleDebug`
  - `:app:testDebugUnitTest`
  - `:app:compileDebugAndroidTestKotlin`
  - focused device smoke test for auth, home, wallet, notifications, invoice, and passkeys

C:\Users\Metal\paysmart\app\docs\remaining_migration_checklist.md
