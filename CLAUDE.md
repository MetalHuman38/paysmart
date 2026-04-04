# Commit 9 Migration Plan

Scope:

- finish `:feature:wallet`
- extract `:data:notifications`
- reduce `:app` toward a shell without forcing broken boundaries

This commit is intentionally narrower than a full final migration. It should not reopen earlier feature work or mix in theme, native, or backend refactors.

See also:

- `app/docs/next_10_migration_commits.md`
- `app/docs/remaining_migration_checklist.md`

## Goal

After commit 9:

- wallet UI and wallet-facing viewmodels should no longer live in `:app`
- notification data/repository ownership should no longer live in `:app`
- `:app` should only keep shell responsibilities plus explicitly deferred Android entrypoint/runtime classes

## Global Rules

### Do Change

- move code only when the target module can own it cleanly
- keep package names unchanged while moving files between modules
- add feature-owned or data-owned interfaces when a moved class still depends on an app implementation
- keep each module self-declaring; do not introduce convention plugins
- validate after each sub-slice with Gradle before moving to the next slice

### Do Not Change

- do not change native build config, NDK version, or CMake setup
- do not change release signing, `network_security_config_*`, or Firebase project configuration
- do not change onboarding/auth flow scope beyond the wallet and notifications boundaries described here
- do not move code into `:core:common` or `:core:ui` just to “make the build pass” unless it is genuinely shared and stable
- do not change package names or public route names unless the current package is impossible to keep
- do not mix theme redesign, loader work, or backend/API refactors into this commit

## Execution Order

1. finish `:feature:wallet`
2. extract `:data:notifications`
3. reduce `:app` to shell-only ownership for everything already migrated

Each step should leave the app compiling.

## Step 1: Finish `:feature:wallet`

Current state:

- `:feature:wallet` exists
- the first safe add-money state/resolver slice is already in `:feature:wallet`
- substantial wallet UI and wallet data ownership still live in `:app`

### Change

#### 1A. Move wallet UI-facing state and viewmodels into `:feature:wallet`

Move these app-owned wallet files into `:feature:wallet`:

- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/viewmodel/AddMoneyViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/screen/AddMoneyScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/screen/AddMoneyContent.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/screen/AddMoneyActionSection.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/card/AddMoneyAvailabilityCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/card/AddMoneyErrorCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/card/AddMoneyInputCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/card/AddMoneySessionStatusCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/card/AddMoneySummaryCard.kt`

Move these funding account files into `:feature:wallet`:

- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/viewmodel/FundingAccountViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/state/FundingAccountUiState.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/screen/FundingAccountScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/screen/FundingAccountRoute.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/screen/FundingAccountContent.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/card/FundingAccountActionRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/card/FundingAccountDetailsCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/card/FundingAccountHeroCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/card/FundingAccountStateCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/card/FundingAccountSurfaceCard.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/util/FundingAccountFormatters.kt`

Move these managed-cards UI files into `:feature:wallet`:

- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/viewmodel/ManagedCardsViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/state/ManagedCardsUiState.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/state/ManagedCardsScreenPhase.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/components/ManagedCardRow.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/components/ManagedCardsEmptyState.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/components/ManagedCardsList.kt`

#### 1B. Move wallet data ownership into `:data:wallet`

Move these funding-account data files into `:data:wallet`:

- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/data/FundingAccountErrorModels.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/data/FundingAccountModels.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/repository/FundingAccountApiException.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/repository/FundingAccountApiParsers.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/repository/FundingAccountGateway.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/fundingaccount/repository/FundingAccountRepository.kt`

Move these managed-cards data files into `:data:wallet`:

- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/data/ManagedCardData.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/data/ManagedCardErrorCode.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/repository/ManagedCardsApiException.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/repository/ManagedCardsApiParsers.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/repository/ManagedCardsGateway.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/repository/ManagedCardsRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/cards/di/ManagedCardsModule.kt`

#### 1C. Keep navigation ownership shallow

- root navigation wiring stays in `:app`
- route helpers/screens/components can move to `:feature:wallet`
- `:app` may still host the top-level graph entry until all feature destinations are stable

### Do Not Change

- do not move `WalletBalanceRepository` yet if it still depends on app-owned native bridge or JNI-backed code
- do not move `RoomNativeBridge`, JNI wrappers, or low-level native-room code during this step
- do not change Stripe / Flutterwave request semantics or payment session behavior
- do not move transaction history persistence out of its current stable boundary during this step
- do not pull wallet code into `:core:*` modules unless it is already clearly shared

### Exit Criteria

- `:feature:wallet` owns add-money, funding-account, and managed-cards UI/state/viewmodel surface
- `:data:wallet` owns wallet remote/data layer for those surfaces
- app-only wallet code is reduced to nav/bootstrap glue only

### Validation

- `:app:compileDebugKotlin`
- `:app:testDebugUnitTest`
- `:app:compileDebugAndroidTestKotlin`
- targeted wallet smoke checks:
  - add-money screen opens
  - funding-account screen opens
  - managed-cards UI renders

## Step 2: Extract `:data:notifications`

Current state:

- home and account already depend on feature-owned notification contracts
- app still owns notification repositories and installation sync implementation

### Change

Create `:data:notifications` and move these repository/data classes out of `:app`:

- `app/src/main/java/net/metalbrain/paysmart/core/notifications/NotificationInboxRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/notifications/NotificationInstallationRegistrar.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/notifications/NotificationInstallationStore.kt`

Move simple adapters/bindings if they become data-owned cleanly:

- `app/src/main/java/net/metalbrain/paysmart/core/notifications/HomeNotificationGatewayAdapter.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/notifications/EmailVerificationNotificationGatewayAdapter.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/HomeNotificationModule.kt`
- `app/src/main/java/net/metalbrain/paysmart/di/EmailVerificationNotificationModule.kt`

Only move the adapters/modules if their target contracts are already feature-owned and they no longer need app-only classes.

### Do Not Change

- do not move `PaySmartFirebaseMessagingService.kt`
- do not move `NotificationBootstrapper.kt`
- do not move `NotificationChannelRegistrar.kt`
- do not move `NotificationChannels.kt`
- do not move `PushMessageNotifier.kt`

Reason:

- those classes are still Android entrypoint/runtime code with `Context`, notification channels, app resources, and activity coupling

### Exit Criteria

- notification data ownership is no longer in `:app`
- feature modules consume notification data through contracts, not app classes
- FCM/runtime notification glue remains temporarily in `:app` by design

### Validation

- `:app:compileDebugKotlin`
- notification-related unit tests
- smoke test:
  - unread badge still updates
  - email verification local inbox item still appears
  - app update local notification item still appears

## Step 3: Reduce `:app` to a Shell

This step is only for code that already has a clean owner elsewhere. It is not a license to shove unfinished feature code around.

### `:app` Should Keep

- `Application` / app entrypoint
- manifest
- launcher resources
- `MainActivity`
- root nav host / top-level graph composition
- startup orchestration
- top-level Hilt bootstrap
- Android service/receiver/runtime classes that still depend on app resources or manifest registration

### `:app` Should Stop Owning

- wallet feature implementation
- home feature implementation
- profile feature implementation that already has a target owner
- account feature implementation that already has a target owner
- notification data/repository ownership

### Change

After steps 1 and 2 are green:

- remove app dependencies on feature implementation classes that now live in `:feature:*` or `:data:*`
- keep only adapters, entrypoints, and root composition in `:app`
- collapse leftover DI in `:app/di` so it binds interfaces to external module implementations instead of owning business logic

### Do Not Change

- do not move remaining blocked account onboarding/login/address flows in this sub-step if they still require app-only resources
- do not move blocked profile surfaces until wallet/capability boundaries are fully stable
- do not rewrite navigation architecture during shell reduction
- do not change package names just to make the shell look cleaner

### Exit Criteria

- `:app` owns shell/runtime only
- feature modules own feature implementation
- data modules own repositories and gateways
- no feature module imports an app-owned implementation class

### Validation

- `:app:assembleDebug`
- `:app:testDebugUnitTest`
- `:app:compileDebugAndroidTestKotlin`
- `:app:assembleRelease`
- `:app:lintDebug`
- device smoke test:
  - sign in
  - home
  - add money
  - funding account
  - notifications
  - invoice flow
  - passkey
  - biometric unlock

## Explicit Non-Goals for Commit 9

- no Spring/Go/backend restructuring
- no theme redesign
- no loader refactor
- no native/JNI rewrite
- no release-signing changes
- no Firebase project/environment changes
- no attempt to finish every remaining profile/account residue in the same commit

## Recommended Commit Breakdown

1. wallet data boundary into `:data:wallet`
2. wallet UI/viewmodel boundary into `:feature:wallet`
3. notification data boundary into `:data:notifications`
4. `:app` shell cleanup for code already moved
5. final validation commit if needed

Each commit should compile independently.
