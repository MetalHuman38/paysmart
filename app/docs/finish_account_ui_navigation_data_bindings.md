# Finish Account UI, Navigation, And Data Bindings

Current status: `app/docs/finish_feature_account_sequential_guide.md` is not archived yet.

Reason: its exit criteria are not complete. `:app` still contains account implementation files and app nav graphs still import account screens/viewmodels directly.

## Goal

Make `:app` a shell for account flows.

After this move, `:app` should own only:

- process/application bootstrap
- root activity and root app composition
- app-level navigation host setup
- build config, signing, manifest, and final packaging

Account should own:

- login, create-account, OTP, recovery, address, email verification, passkey/passcode account UI
- account route wrappers
- account-owned resources
- account-specific DI bindings
- account-specific data/policy clients

## Current Evidence

Verified current structure:

- `app/src/main/java/net/metalbrain/paysmart/core/features/account` still exists
- `app/src/main/java/net/metalbrain/paysmart/core/features/account` still has 44 Kotlin/Java source files
- `feature/account/src/main/java/net/metalbrain/paysmart/core/features/account` exists and has the feature-owned account surface
- `AuthNavGraph.kt` still imports concrete account screens and viewmodels directly, but the first auth-route extraction is complete
- `ProfileNavGraph.kt` still owns the app-side address resolver entry

Do not archive `finish_feature_account_sequential_guide.md` until the exit criteria at the bottom of this file are true.

## Progress Log

### 2026-04-25: Step 2 Complete

Validated source state:

- `AuthNavGraph.kt` now has 27 direct `composable(...)` registrations
- original direct registrations for `Login`, `LoginMfaChallenge`, and `AddEmail` were removed from `AuthNavGraph.kt`
- `AuthNavGraph.kt` now delegates those three routes through `accountAuthRoutes(...)`
- `AuthNavGraph.kt` no longer imports `LoginRoute`, `AddEmailRoute`, `MfaSignInChallengeScreen`, or `MfaSignInViewModel`
- `feature/account/.../navigation/AccountAuthRoutes.kt` owns the `Login`, `LoginMfaChallenge`, and `AddEmail` registrations
- `feature/account/.../navigation/AccountCreationRoutes.kt` exists as the creation route boundary shell
- `feature/account/.../navigation/AccountRecoveryRoutes.kt` exists as the recovery route boundary shell
- `feature/account/.../navigation/AccountAddressRoutes.kt` exists as the address route boundary shell
- `feature/account/.../navigation/AccountFeatureGateRoutes.kt` exists as the feature-gate route boundary shell

Intentional remaining inline registrations:

- `EmailSent`
- `EmailVerified`

Reason:

- both still need `SecurityViewModel.markRecoveryMethodReady()` from app scope
- `SecurityViewModel` still physically lives in `:app`

Important design note:

- `EmailSentRoute` and `EmailVerifiedRoute` already accept callbacks
- they do not need to import or own `SecurityViewModel`
- the next move should pass `onRecoveryMethodReady` from `:app` into the feature-owned route registration, the same way Step 2 passed `clientId` and `currentLanguage` as app-owned composable slots

### 2026-04-25: Step 3 First Sub-Task Complete

Validated source state:

- `AccountAuthRoutes.kt` now owns all five front-door auth routes
- owned routes are `Login`, `LoginMfaChallenge`, `AddEmail`, `EmailSent`, and `EmailVerified`
- `AuthNavGraph.kt` now has 25 direct `composable(...)` registrations
- `AuthNavGraph.kt` no longer imports `EmailSentRoute` or `EmailVerifiedRoute`
- `AuthNavGraph.kt` no longer imports `LoginRoute`, `AddEmailRoute`, `MfaSignInChallengeScreen`, or `MfaSignInViewModel`
- app-owned `SecurityViewModel` is bridged through `onMarkRecoveryReady: @Composable () -> () -> Unit`
- `AccountAuthRoutes.kt` invokes that composable slot inside destination scope and passes the returned plain callback to `EmailSentRoute` and `EmailVerifiedRoute`

This closes the front-door auth-route registration move.

Current remaining inline account registrations in `AuthNavGraph.kt` now start with create-account and recovery surfaces.

Next structural target:

- move the `CreateAccount` registration first
- do not start with the full OTP/recovery/address batch
- keep the next commit small enough that a failed compile points to one ownership boundary

### 2026-04-25: CreateAccount Route Wiring Validated

Validated source state:

- `AuthNavGraph.kt` now has 24 direct `composable(...)` registrations
- `AuthNavGraph.kt` imports `accountCreationRoutes(...)`
- `AuthNavGraph.kt` no longer imports `CreateAccountScreen`
- `AuthNavGraph.kt` no longer imports `CreateAccountViewModel`
- `AccountCreationRoutes.kt` registers `Screen.CreateAccount.route`
- `AccountCreationRoutes.kt` delegates to `CreateAccountRoute`
- `CreateAccountRoute` owns `hiltViewModel<CreateAccountViewModel>()`
- `feature/account/build.gradle.kts` now includes `libs.libphonenumber`
- `feature/account/src/main/res/values/strings.xml` now includes the CreateAccount strings required by this slice

Cleanup finding:

- route ownership moved correctly
- stale app-side CreateAccount files initially still existed under `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation`
- those stale app files had the same package names as the feature copies
- because `:app` depends on `:feature:account`, duplicate fully qualified classes could break compile, dexing, or release packaging

Clean pass implemented:

- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/screen/CreateAccountScreen.kt`
- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/viewmodel/CreateAccountViewModel.kt`
- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/CreateAccountContent.kt`
- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/CreateAccountHeaderRow.kt`
- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/ConsentRow.kt`
- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/AccountCreationScaffold.kt`
- deleted `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/card/AccountCreationHeroCard.kt`
- made feature-owned `AccountCreationScaffold` public because app-owned post-OTP screens still reference it until they migrate
- made feature-owned `AccountCreationHeroCard` public because app-owned post-OTP/client-info components still reference it until they migrate

Source-level duplicate check:

- `CreateAccountScreen`, `CreateAccountViewModel`, `CreateAccountContent`, `CountryHeaderRow`, `ConsentRow`, `AccountCreationScaffold`, and `AccountCreationHeroCard` now have definitions only in `:feature:account`
- `app/src/main/java/net/metalbrain/paysmart` has no remaining direct references to `CreateAccountScreen` or `CreateAccountViewModel`
- app source still references `AccountCreationScaffold` and `AccountCreationHeroCard` from remaining post-OTP/client-info files, but those references now resolve to the public feature-owned definitions

After cleanup, validate:

```powershell
rg -n "CreateAccountScreen|CreateAccountViewModel" app/src/main/java/net/metalbrain/paysmart
./gradlew :feature:account:compileDebugKotlin
./gradlew :app:compileDebugKotlin
```

Validation result:

- `./gradlew :app:compileDebugKotlin` passed on 2026-04-25
- result was fully up-to-date with configuration cache reused
- this confirms the app shell can currently compile against the feature-owned CreateAccount slice

Release focus for this checkpoint:

- prioritize current-user performance and service availability over more migration movement
- do not start OTP, recovery, address, or feature-gate extraction before release validation completes
- use the build scan to verify whether app lint/package/R8 time improved or stayed stable after the route extraction
- verify Firebase, auth, update, notification, and network-dependent paths before release because those affect service availability directly

## Sequential Move

### 1. Freeze `:app` Account Ownership

From this point onward:

- no new account screen goes into `:app`
- no new account viewmodel goes into `:app`
- no new account string or raw asset goes into `:app`
- no new account-only DI binding goes into `:app`
- no new account navigation side effect goes into `AuthNavGraph.kt` or `ProfileNavGraph.kt`

Any new account work lands in:

- `:feature:account` for UI, route wrappers, account resources, account policy clients
- `:data:auth` for reusable auth data/repository contracts and implementations
- `:core:models` for shared auth/account value models
- `:core:navigation` for route constants or route contracts only

### 2. Create Feature-Owned Navigation Entry Points - Complete

Feature-owned navigation functions now exist in `:feature:account`.

Recommended package:

```text
feature/account/src/main/java/net/metalbrain/paysmart/core/features/account/navigation/
```

Target entry points:

- `accountAuthRoutes(...)`
- `accountRecoveryRoutes(...)`
- `accountCreationRoutes(...)`
- `accountAddressRoutes(...)`
- `accountFeatureGateRoutes(...)`

The app graph should call these entry points and pass navigation callbacks.

Do not pass `NavController` into account screens. Keep `NavController` at the app or route-wrapper boundary.

### 3. Add Missing Route Wrappers Before Moving More Screens

Completed first sub-task:

- `accountAuthRoutes(...)` now registers `EmailSent` and `EmailVerified`
- `SecurityViewModel` lookup stays in `:app` for now through a composable callback slot
- inline `EmailSent` and `EmailVerified` registrations were removed from `AuthNavGraph.kt`
- `AuthNavGraph.kt` dropped from 27 to 25 direct `composable(...)` registrations
- `AuthNavGraph.kt` has zero direct imports of `EmailSentRoute` and `EmailVerifiedRoute`

Immediate next logic step:

1. Move only the `CreateAccount` entry slice from `:app` to `:feature:account`.
2. Create `CreateAccountRoute` in `feature/account/src/main/java/net/metalbrain/paysmart/core/features/account/creation/route/`.
3. Let `CreateAccountRoute` own `hiltViewModel<CreateAccountViewModel>()`, selected country/dial-code lookup, and screen wiring.
4. Expose plain callbacks from `CreateAccountRoute`: verification continue, help, sign-in, and back.
5. Fill `accountCreationRoutes(...)` with the `Screen.CreateAccount.route` registration only.
6. Replace the inline `composable(Screen.CreateAccount.route)` block in `AuthNavGraph.kt` with the `accountCreationRoutes(...)` call.
7. Recount `AuthNavGraph.kt`; it should drop from 25 direct `composable(...)` registrations to 24.
8. Confirm `AuthNavGraph.kt` has zero direct imports of `CreateAccountScreen` and `CreateAccountViewModel`.

Move these app files only if they are required by the `CreateAccount` entry slice:

- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/screen/CreateAccountScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/viewmodel/CreateAccountViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/CreateAccountContent.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/CreateAccountHeaderRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/ConsentRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/AccountCreationScaffold.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/card/AccountCreationHeroCard.kt`

Move resources only when compile requires them for this slice. Do not bulk-move OTP, recovery, address, or feature-gate resources in the same step.

Why this comes before OTP/recovery/address migration:

- it closes the front-door auth extraction started in Step 2
- it starts the next visible account flow at its actual entry point
- it keeps `OtpVerification`, `ReauthOtp`, recovery, address, and feature-gate behavior untouched while the create-account boundary is proven
- it should reduce one more account-owned registration from `AuthNavGraph.kt` without forcing a broad resource migration

Create route wrappers in `:feature:account` for the remaining app-owned account flows.

Required wrappers:

- `CreateAccountRoute`
- `OtpVerificationRoute`
- `ReauthOtpRoute`
- `ClientInformationRoute`
- `PostOtpCapabilitiesRoute`
- `PostOtpSecurityStepsRoute`
- `RecoverAccountRoute`
- `ChangePasswordRecoveryRoute`
- `ChangePhoneRecoveryRoute`
- `AddressSetupResolverRoute`
- `FeatureGateRoute`

Each route wrapper owns:

- `hiltViewModel()` lookup
- route argument decoding
- lifecycle side effects
- toast/snackbar side effects
- mapping UI callbacks into app-level navigation callbacks

Each screen owns only:

- rendering
- local UI state
- user event callbacks

### 4. Flip `AuthNavGraph.kt` To Feature Route Calls

Replace direct account screen wiring in `AuthNavGraph.kt` with feature-owned route calls.

Remove direct imports for account implementation classes such as:

- `CreateAccountScreen`
- `OtpVerificationScreen`
- `ReauthOtpScreen`
- `ClientInformationScreen`
- `PostOtpCapabilitiesScreen`
- `PostOtpSecurityStepsScreen`
- `RecoverAccountScreen`
- `ChangePasswordRecoveryScreen`
- `ChangePhoneRecoveryScreen`
- account viewmodels used only by those screens
- `FeatureGateScreen`

Allowed imports from `:feature:account` after this step:

- route wrapper functions
- route registration functions
- small navigation callback contracts

### 5. Move Address Ownership Out Of `ProfileNavGraph.kt`

Move the address resolver entry into `:feature:account`.

Target outcome:

- `ProfileNavGraph.kt` does not import `AddressSetupResolverScreen`
- address route state and side effects live in `AddressSetupResolverRoute`
- profile graph only calls the feature-owned address route entry or navigates to its route string

### 6. Remove Duplicate App Account Files

After app navigation calls feature-owned routes, delete app-side account implementation copies.

Delete only after compile confirms the feature-owned version is used.

Target removals:

- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/phone`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/recovery`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/address`
- app-owned account protection screen files once routed from feature
- app-owned feature-gate UI once moved to `:feature:account`

Do not delete shared models or policies until their imports are proven unused from `:app`.

### 7. Move Account Resources After Code References Flip

Move resources only after the route wrappers compile from `:feature:account`.

Move account-owned resources from `app/src/main/res` to `feature/account/src/main/res`.

Expected resource groups:

- create-account strings
- OTP strings
- recovery strings
- change-phone strings
- address resolver strings
- post-OTP onboarding strings
- feature-gate strings
- account linking strings
- account-only raw animations
- account-only auth/provider drawables

Keep shared brand, launcher, theme, startup, and app-shell resources in `:app` or `:core:ui`.

### 8. Move Account DI Bindings Out Of `:app`

Only move DI after the corresponding implementation files no longer live in `:app`.

Review and relocate or shrink:

- `app/src/main/java/net/metalbrain/paysmart/di/AppModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/SocialAuthModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/ClientModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/EmailVerificationNotificationModule.kt`

Target ownership:

- reusable auth repository bindings belong in `:data:auth`
- account-only orchestration belongs in `:feature:account`
- app shell keeps only process-wide bindings required before feature navigation starts

### 9. Trim `:app` Dependencies

After imports are removed from `:app`, trim app dependencies that become feature-owned.

Candidates to remove from `app/build.gradle.kts` if unused by shell code:

- Google Maps
- Credential Manager
- biometric
- Firebase Auth
- Firestore
- DataStore preferences
- account-specific Play Services dependencies
- account-only Compose/Lottie/image dependencies

Do this with compile feedback. Remove one dependency group at a time.

### 10. Re-Run Build Scan And Compare App Cost

After each major slice, run:

```powershell
./gradlew :feature:account:compileDebugKotlin
./gradlew :app:compileDebugKotlin
./gradlew :feature:account:lintDebug
./gradlew :app:lintDebug
```

Before closing the move, run:

```powershell
./gradlew build --scan
./gradlew :app:assembleRelease
```

Compare these scan tasks against the previous baseline:

- `:app:lintAnalyzeDebug`
- `:app:lintVitalAnalyzeRelease`
- `:feature:account:lintAnalyzeDebug`
- `:app:minifyReleaseWithR8`

Expected result:

- `:app:minifyReleaseWithR8` may remain high because app is the final release aggregation point
- `:app:lintAnalyzeDebug` should drop as account source/resources leave `:app`
- `:feature:account:lintAnalyzeDebug` may rise because ownership moved there

## Exit Criteria

This move is complete when:

- `app/src/main/java/net/metalbrain/paysmart/core/features/account` no longer exists
- app nav graphs do not import concrete account screens
- app nav graphs do not import account viewmodels for route internals
- `ProfileNavGraph.kt` does not import address resolver implementation
- account route wrappers live in `:feature:account`
- account resources needed by those wrappers live in `:feature:account`
- account-only DI bindings no longer live in `:app`
- `app/build.gradle.kts` no longer declares account-only dependencies
- `:feature:account:compileDebugKotlin` passes
- `:app:compileDebugKotlin` passes
- `:feature:account:lintDebug` passes
- `:app:lintDebug` passes
- `./gradlew build --scan` completes the build
- `./gradlew :app:assembleRelease` succeeds

## Archive Rule

Archive `app/docs/finish_feature_account_sequential_guide.md` only after this move reaches the exit criteria above.

Target archive path:

```text
app/docs/archive/finish_feature_account_sequential_guide.md
```

When archiving, keep this file as the active next-step guide until the app shell contains no account implementation ownership.
