# Remaining Migration Checklist

This checklist is split into two tracks:

- `Release blocker`: work that should be finished or explicitly waived before calling the app stable for a release build
- `Post-release migration`: architecture cleanup that can continue after a stable release candidate exists

See also: `app/docs/next_10_migration_commits.md`

## Current Module State

- [x] `:core:common`
- [x] `:core:navigation`
- [x] `:core:ui`
- [x] `:core:security`
- [x] `:core:database`
- [x] `:core:firebase`
- [x] `:data:auth`
- [x] `:data:user`
- [x] `:data:invoice`
- [x] `:data:wallet` — data + UI/viewmodel layer
- [x] `:data:notifications` — inbox, installation, store repositories
- [x] `:feature:home` UI slice
- [x] `:feature:wallet` — add-money + funding-account + managed-cards UI/viewmodel
- [x] `:feature:notifications` so notification center UI no longer lives in `:app`
- [ ] finish `:feature:profile` by extracting the wallet/capability-coupled profile surfaces that still belong to `:app`
- [ ] finish `:feature:account` UI and remaining viewmodels
- [ ] reduce `:app` to shell only

## Release Blockers

- [x] run `:app:assembleDebug`
- [x] run `:app:testDebugUnitTest`
- [x] run `:app:assembleRelease`
- [x] run `:app:lintDebug`
- [x] centralize startup loading so the app root does not render home beneath the loading screen
- [x] run affected Android test compile targets and resolve or explicitly waive any known baseline failures
- [ ] smoke test account creation, sign-in, home, add money, notifications, invoice flow, passkey, and biometric unlock on device
- [ ] confirm no moved feature module is still depending on an app-owned implementation class
- [ ] confirm release signing, release network security config, and production Firebase config are aligned with the migrated module graph

## Remaining Migration Work

## Strict Order

### 1. Finish `:feature:home`

- [x] create `:feature:home` module
- [x] move pure home state/support/search files
- [x] move home detail viewmodels that no longer depend on `:app`
- [x] abstract app-owned notification implementation behind a feature-owned home gateway
- [x] abstract app-owned recent recipient implementation behind a feature-owned home gateway
- [x] abstract app-owned country capability implementation behind a feature-owned home gateway
- [x] move remaining home headless viewmodels that still depends on other app-owned implementations
- [x] move home routes/screens/components into `:feature:home`
- [x] remove duplicate home state and viewmodel ownership from `:app`

### 2. Finish `:feature:profile`

- [x] create `:feature:profile` module
- [x] move profile repository/storage/viewmodel headless slice
- [x] move pure account statement formatter helpers
- [x] move portable profile types and helpers that no longer depend on app-owned implementations
- [x] move the safe profile UI slice into `:feature:profile`
  - safe slice now owned by `:feature:profile`:
    - profile shell UI
    - about screens
    - photo picker UI
    - profile details UI
    - security/privacy UI
- [ ] move the remaining app-owned display helpers that still depend on app-owned language/capability catalogs
- [ ] move the remaining profile screens/cards/components that are still coupled to wallet/capability/account feature code
  - still app-owned for now:
    - account information
    - account limits
    - account statement
    - connected accounts

### 3. Finish `:feature:account`

- [x] create `:feature:account` module
- [x] move account headless security/auth/passkey/recovery slice
- [x] rebalance shared headless account security services back into `:core:security`
- [x] move runtime-config dependencies out of `:app`
- [x] move the safe account UI slice into `:feature:account`
  - safe slice now owned by `:feature:account`:
    - passcode cards/components
    - `SetPasscodeScreen`
    - `VerifyPasscodeScreen`
    - change-passcode flow
      - `ChangePasscodeScreen`
      - `ChangePasscodeBiometricGateScreen`
      - `ChangePasscodeViewModel`
    - password screens
      - `CreatePasswordScreen`
      - `EnterPasswordScreen`
    - passkey cards/components/utils
    - `PasskeySetupScreen`
    - `ProfilePasskeySettingsScreen`
    - `MfaNudgeScreen`
    - `MfaSignInChallengeScreen`
- [x] share the account brand footer and logo through `:core:ui`
- [x] abstract `PasscodePrompt` away from the app-owned `SecurityViewModel`
- [x] abstract the email verification notification dependency behind a feature-owned gateway
- [x] move the email verification sent-state viewmodel into `:feature:account`
- [ ] move remaining account viewmodels only after app-only resources and feature helpers are shared
- [ ] move the remaining app-owned account UI/screens that are still coupled to app branding, onboarding, or address/login flows
  - still app-owned for now:
    - email verification sent/success screens
    - login and recovery screens
    - create-account / OTP / onboarding screens
    - address setup flow

### 4. Build `:feature:wallet`

- [x] create `:feature:wallet`
- [x] move the first safe add-money state/resolver slice
- [x] move add-money, funding-account, and managed-cards UI/viewmodel surface into `:feature:wallet`
- [x] move wallet remote/data ownership for those surfaces into `:data:wallet`
- [ ] keep the native-bridge / wallet-balance boundary deferred until JNI-backed ownership is ready

### 5. Build `:data:notifications`

- [x] move inbox/installations/preferences repositories out of `:app`
- [x] move clean gateway adapters and DI modules into `:data:notifications`
- [x] leave FCM/runtime notification glue in `:app` by design
- [x] move notification center UI/viewmodel/card ownership into `:feature:notifications`

### 6. Reduce `:app` to Shell

- [ ] keep only `Application`, manifest, launcher resources, root nav host, startup orchestration, and top-level DI bootstrap
- [ ] remove feature implementation ownership from `:app`
- [ ] verify no feature module imports an app-owned class

## Current Release-Stable Position

- local validation is green for:
  - `:feature:profile:compileDebugKotlin`
  - `:app:compileDebugKotlin`
  - `:app:testDebugUnitTest`
  - `:app:lintDebug`
  - `:app:assembleRelease`
- the active migration blockers are now architectural cleanup, not build stability
- the next high-value cleanup is:
  - finish the blocked `:feature:profile` wallet/capability surfaces
  - finish the blocked `:feature:account` onboarding/login/address surfaces
  - then reduce `:app` to a shell

## Validation Gate After Each Slice

- [x] `:app:compileDebugKotlin`
- [x] `:app:testDebugUnitTest`
- [x] affected Android test compile target where applicable
- [ ] device smoke test for auth, home, notifications, invoice, and passkey flows
