# Sequential Guide: Finish `:feature:account`

This guide now starts from the current validated state, not from zero.

It is focused on one outcome:

- keep `:app` as a thin shell
- move remaining account implementation ownership out of `:app`
- keep releases safe while invoice-facing work continues to grow

See also:

- `app/docs/app_shell_completion_plan.md`
- `app/docs/remaining_migration_checklist.md`
- `app/docs/auth_flow_stabilization_strategy.md`
- `app/docs/create_account_recovery_execution_plan.md`

## Validated State

Validated on 2026-04-24 with:

```powershell
./gradlew :core:ui:compileDebugKotlin
./gradlew :feature:account:compileDebugKotlin
./gradlew :app:compileDebugKotlin
```

Result:

- `:core:ui` compiles cleanly and reuses configuration cache
- `:feature:account` compiles cleanly
- `:app` compiles cleanly
- current module split compiles

## Already Done

These are no longer the main blockers for this slice:

- `:feature:account` now depends on `:core:navigation`, `navigation-compose`, and Google Maps
- `Country` moved to `:core:models`
- country helpers now live outside `:app`
- `DebounceUtils` moved to `:core:ui`
- generic auth infrastructure moved out of `:app` into `:data:auth`
- account-specific auth policy clients moved into `:feature:account`
- `:app` already depends on `:feature:account`, `:data:auth`, `:core:models`, and `:core:ui`
- moved feature-owned helper UI out of app-global `net.metalbrain.paysmart.ui.*` package names
- feature-owned email verification routes now exist in `:feature:account`
- `LoginViewModel`, `EmailLinkUseCase`, and `LoginRoute` now live in `:feature:account`
- `LinkFederatedAccountRoute` now lives in `:feature:account`

This means the remaining work is now mostly about the create-account and recovery routes, shared UI/resource ownership, and leftover DI bindings.

## Current Shell Assessment

`MainActivity` and `SecureApp` are already reasonably close to the target shell boundary.

They still own the right kinds of concerns:

- root activity hosting
- startup / loading orchestration
- connectivity gate
- update coordinator integration
- root secure-navigation intent handling
- root nav host setup

Do not spend this slice restructuring them unless a direct blocker appears.

## What Is Still Blocking A Thin App Shell

### 1. Route ownership is only partially in `:feature:account`

Feature-owned route wrappers already exist for the front-door slice:

- `LoginRoute`
- `AddEmailRoute`
- `EmailSentRoute`
- `EmailVerifiedRoute`
- `LinkFederatedAccountRoute`

`AuthNavGraph.kt` still directly imports and wires account implementation screens or app-owned routes for:

- `CreateAccountScreen`
- `OtpVerificationScreen`
- `ReauthOtpScreen`
- `RecoverAccountScreen`
- `ChangePasswordRecoveryScreen`
- `ChangePhoneRecoveryScreen`
- `ClientInformationScreen`
- `PostOtpCapabilitiesScreen`
- `PostOtpSecurityStepsScreen`
- `FeatureGateScreen`

`ProfileNavGraph.kt` still directly imports and owns the app-side address flow entry:

- `AddressSetupResolverScreen`

### 2. A large account surface still physically lives in `:app`

Remaining account implementation under `:app` still includes:

- `core/features/account/creation/*`
- `core/features/account/creation/phone/*`
- `core/features/account/recovery/*`
- `core/features/account/address/*`
- `core/features/featuregate/*`

The account helper UI that used to sit under app-global `ui/*` now lives in `:feature:account`.

### 3. Front-door cleanup is mostly done, but not fully clean yet

Current remaining coupling in the front-door slice:

- `LoginHeaderRow` still imports `LanguageSelector`
- `AuthNavGraph.kt` still owns the `SecurityViewModel` state that feeds federated-linking completion
- startup auto-passkey still uses `LoginViewModel` directly from `AuthNavGraph.kt`
- `:feature:account` had a stray dependency on `:feature:profile`; that edge has been removed and should stay out of the compile graph

This means the next post-release work should move to the create-account and recovery route surfaces rather than reopening the login move.

### 4. Resource ownership is still mostly in `:app`

`feature/account/src/main/res` is no longer empty.

It now contains:

- `values/strings.xml`
- `raw/loader.json`
- `raw/shield.json`
- Google sign-in logo drawables

Even after that move, account strings and assets are still mostly defined in `app/src/main/res`, including:

- recovery strings
- OTP strings
- change-phone strings
- address resolver strings
- post-OTP onboarding strings
- account linking copy

### 5. Some DI ownership is still pinned to `:app`

These still need review and probably relocation or slimming once route ownership moves:

- `app/src/main/java/net/metalbrain/paysmart/di/AppModule.kt`
  - still provides `AuthService`
- `app/src/main/java/net/metalbrain/paysmart/di/SocialAuthModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/ClientModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/EmailVerificationNotificationModule.kt`

## Remaining Execution Order

The order below is now the shortest safe path to reduce coupling without destabilizing the app shell.

### 1. Freeze the boundary first

From this point onward:

- no new account UI goes into `:app`
- no new auth helper goes into `:app`
- no new feature-gate UI goes into `:app`

Any new work for account, onboarding, recovery, email linking, or feature gating should land directly in `:feature:account` or `:data:auth`.

Package rule from this point onward:

- if a file is owned by `:feature:account`, its package must start with `net.metalbrain.paysmart.core.features.account`
- only truly shared UI primitives should stay under `net.metalbrain.paysmart.ui.*` in `:core:ui`

### 2. Hold the front-door slice steady and verify the leaner compile graph

This step is now about stabilization, not more movement.

Validated sequence:

1. keep `LoginRoute`, email routes, and `LinkFederatedAccountRoute` in `:feature:account`
2. verify `:feature:account` no longer pulls `:feature:profile` into the compile graph
3. do not widen this front-door slice further before the release-performance check

Why this order:

- the login and federated-linking route extraction is already in place
- the recent dependency cleanup should reduce invalidation and avoid compiling `:feature:profile` when touching `:feature:account`
- release verification is more valuable now than stacking another migration on top of this slice

### 3. Finish the remaining front-door cleanup after the release check

Before touching create-account, remove the last cross-boundary leaks in the front-door slice.

Required cleanup:

- `LoginHeaderRow` must stop importing `LanguageSelector`
- federated-linking completion should stop depending on app-owned `SecurityViewModel` state inside `AuthNavGraph.kt`
- startup auto-passkey should stop making `AuthNavGraph.kt` a direct owner of login behavior

### 4. Create the remaining feature-owned route wrappers

After the release check, add wrappers in `:feature:account` for the still-app-owned flows.

Remaining target wrappers:

- `CreateAccountRoute`
- `OtpVerificationRoute`
- `ReauthOtpRoute`
- `RecoverAccountRoute`
- `ChangePasswordRecoveryRoute`
- `ChangePhoneRecoveryRoute`
- `AddressSetupResolverRoute`
- `FeatureGateRoute`

Each wrapper should own:

- `hiltViewModel()` lookup
- argument decoding
- transient toast / snackbar side effects
- mapping UI callbacks back to app navigation

Each screen should be reduced to:

- rendering
- local UI state
- callback outputs

`AuthNavGraph.kt` and `ProfileNavGraph.kt` should only call route wrappers, not concrete screen internals.

### 5. Move the remaining app-owned account flows in release-safe order

Move in this order:

1. create-account plus OTP flows
2. post-OTP onboarding flows
3. recovery flows
4. address resolver flow
5. feature-gate UI

Why this order:

- the front door is now mostly extracted and should be release-tested
- create-account and OTP are the next onboarding dependency
- post-OTP onboarding should stay grouped so country and security setup do not split across modules
- recovery and address are lower-frequency but still important
- feature-gate UI can move late once its navigation targets are stable

### 6. Move the account-owned resources only after wrappers are in place

Before moving resources, inventory the actual references from the moved code.

Focus on moving only what the account slice owns:

- OTP strings
- recovery strings
- account linking copy
- address resolver strings
- post-OTP onboarding strings
- any raw assets only used by account flows
- any account-only drawables

Do not duplicate shared brand assets into `:feature:account`.

### 7. Remove leftover DI ownership from `:app`

After route wrappers compile and the moved screens are feature-owned, clean up the remaining app DI bindings.

Expected follow-up:

- move the `AuthService` provider out of `AppModule.kt`
- move or delete `SocialAuthModule.kt`
- keep `ClientModule.kt` in `:app` only if non-account shell code still needs it
- move `EmailVerificationNotificationModule.kt` to the owning module if it only serves account flows

### 8. Reduce `AuthNavGraph.kt` and `ProfileNavGraph.kt` to shell wiring only

End-state requirement:

- `AuthNavGraph.kt` keeps route registration and shell navigation only
- `ProfileNavGraph.kt` keeps route registration and shell navigation only
- neither graph should import app-owned account implementation files
- neither graph should contain feature-specific transient side-effect logic once wrappers exist

## Release-Safe Validation

Use this as the minimum validation loop after each slice:

```powershell
./gradlew :core:ui:compileDebugKotlin
./gradlew :feature:account:compileDebugKotlin
./gradlew :app:compileDebugKotlin
```

Before closing the slice:

```powershell
./gradlew :app:testDebugUnitTest
./gradlew :app:compileDebugAndroidTestKotlin
```

Manual smoke coverage required:

- startup to login
- startup to create account
- OTP resend and OTP success
- post-OTP onboarding
- email verification send and resume
- federated linking
- recover account
- change phone recovery
- address resolver
- feature-gate continue path
- invoice entry path still works after auth completion

## Exit Criteria

This slice is done when all of the following are true:

- no remaining login, create-account, OTP, recovery, address, or feature-gate implementation files live under `:app`
- `AuthNavGraph.kt` no longer imports concrete account screen implementations
- `ProfileNavGraph.kt` no longer imports the app-owned address resolver screen
- moved account screens do not depend on `NavController` directly
- moved account screens do not depend on app-owned helper UI directly
- app DI no longer owns bindings that only exist for moved account flows
- `MainActivity` and `SecureApp` remain shell-only and do not grow new feature logic
- invoice-facing flows still build and remain reachable

## Do Not Roll Into This Slice

Keep these out of scope unless they become direct blockers:

- `SecurityViewModel` extraction
- `RequireSessionUnlock` extraction
- `BiometricOptInScreen`
- `BiometricSessionUnlock`
- full language-module extraction
- broader auth-product redesign
- invoice feature extraction beyond what is needed to keep release safety
