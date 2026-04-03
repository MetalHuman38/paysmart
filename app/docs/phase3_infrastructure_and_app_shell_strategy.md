# Phase 3 Strategy: Infrastructure Separation and App Shell Reduction

## Objective

Phase 2 stabilized the design-system layer in `:core:ui` and proved the first extraction wave is coherent.

Phase 3 should do two things:

1. extract the remaining heavy infrastructure owned directly by `:app`
2. reduce `:app` into a shell that composes features instead of implementing them

This is the phase where PaySmart starts behaving like Builders structurally, while staying Android-first and repo-local in configuration.

## Entry Conditions

Phase 3 assumes:

- Phase 1 modules compile and run
- Phase 2 theme accessors are the default for shared surfaces
- shared buttons, text, backgrounds, and setup states already resolve through token-backed theme values
- Phase 2A design-system tokens exist, but feature-level screen adoption is not yet complete
- first extracted feature modules can depend on `:core:common`, `:core:navigation`, `:core:ui`, and `:core:security`

## Target Module Shape

### Infrastructure modules

- `:core:database`
  - Room database
  - DAOs
  - entities
  - migrations
  - local persistence helpers

- `:core:firebase`
  - Firebase Auth wrappers
  - Firestore/Functions entry points
  - App Check / token bootstrap helpers
  - shared Firebase config and providers

- `:core:network`
  - API config
  - request signing / auth headers
  - serialization helpers
  - common HTTP clients

### Data modules

- `:data:auth`
- `:data:user`
- `:data:invoice`
- `:data:wallet`
- `:data:notifications`

These should own repositories that bridge `core` infrastructure to feature/domain code.

### Feature modules to finish

- `:feature:home`
- `:feature:wallet`
- `:feature:profile`
- `:feature:account`

By the end of Phase 3, `:app` should stop owning feature implementation directly.

## Execution Order

### Phase 3A: `:core:database`

Move:

- `room/EncryptedAppDatabase.kt`
- `room/DbMigrations.kt`
- DAOs
- entities
- database managers and helpers that do not depend on feature code

Why first:

- many repositories are blocked on Room still living in `:app`
- this removes one of the biggest invalidation hotspots from the app module

### Phase 3B: `:core:firebase`

Move:

- Firebase modules/providers
- shared auth/bootstrap wrappers
- common Firestore/Functions entry points

Why second:

- security, notifications, identity, and profile all touch Firebase
- feature modules should not own SDK initialization details

Theme migration track in this slice:

- complete the rollout of `PaysmartTheme` token usage across remaining high-traffic feature screens still in `:app`
- remove raw `MaterialTheme.colorScheme` usage where it causes theme-switch drift, especially in light theme
- prioritize account/profile, add-money, funding account, help, and any remaining onboarding/account surfaces that still hardcode color or typography behavior
- treat this as part of the Phase 3 baseline before broad `:data:*` extraction, so future module moves inherit a stable premium theme instead of dragging theme cleanup across multiple phases

### Phase 3C: `:data:*`

Split repositories by domain boundary:

- auth/session repositories to `:data:auth`
- user/profile repositories to `:data:user`
- invoice repositories to `:data:invoice`
- wallet/add-money/fx repositories to `:data:wallet`
- inbox/installations/preferences repositories to `:data:notifications`

Why third:

- feature modules need stable repository boundaries
- this prevents `:feature:*` modules from depending directly on Room and Firebase implementations

### Phase 3D: complete feature extraction

Finish extracting:

- `:feature:home`
- `:feature:wallet`
- `:feature:profile`
- `:feature:account`

These are broader and more coupled than the earlier feature candidates, so they should come only after infra and data are extracted.

### Phase 3E: reduce `:app` to shell

By the end of the phase, `:app` should mainly own:

- manifest
- `Application`
- top-level Hilt bootstrap
- root navigation host
- startup orchestration
- app icon / launcher resources

Everything else should be delegated to `:core:*`, `:data:*`, or `:feature:*`.

## Dependency Rules

- `:app` may depend on any feature module and shared core/data modules
- `:feature:*` may depend on `:core:*` and domain-facing `:data:*`
- `:feature:*` must not depend on other `:feature:*` modules directly
- `:data:*` may depend on `:core:database`, `:core:firebase`, and `:core:network`
- `:core:*` must not depend on `:feature:*`

## Validation

After each Phase 3 slice:

- run `:app:compileDebugKotlin`
- run `:app:compileDebugUnitTestKotlin`
- run the affected Android test compile target where applicable
- smoke test auth, home, notifications, invoice flow, and passkey setup on device

Additional checks:

- no feature module should import Room entities from `:app`
- no feature module should initialize Firebase SDK clients directly
- no repository should require an `:app` package import to compile
- theme switch should keep text contrast, background hierarchy, and primary CTA styling consistent across light and dark mode on the migrated high-traffic screens

## Success Criteria

Phase 3 is complete when:

- `:app` is composition-only
- Room and Firebase are no longer implemented inside `:app`
- feature modules consume domain/data contracts rather than app-owned infrastructure
- build invalidation is materially reduced for feature-only changes
- the module graph is ready for Phase 4 launch hardening without another architectural reset

See also:

- `app/docs/remaining_migration_checklist.md`
