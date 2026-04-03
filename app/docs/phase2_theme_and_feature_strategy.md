# Phase 2 Strategy: Theme Stabilization and First Feature Modules

## Objective

Phase 1 proved the module graph is viable:

- `:app:compileDebugKotlin` passes
- `:app:compileDebugUnitTestKotlin` passes
- the remaining Android test failure is a pre-existing baseline issue, not a modularization regression

Phase 2 should not immediately expand the module graph again. The next priority is to stabilize the newly extracted `:core:ui` module so it becomes the design-system foundation for later feature modules.

The right next move is to emulate the stable parts of Builders' theme architecture inside PaySmart:

- tokenized spacing, radius, elevation, border, typography, width, and height
- a theme config object
- a small accessor surface for tokens inside composables
- compatibility aliases so existing PaySmart screens do not break while the design system is normalized

After the theme system is stable, Phase 2 should extract the first full feature modules.

## Builders vs PaySmart: What To Copy

### Builders pattern worth copying

Builders is stable because its theme layer has:

- a `ThemeConfig` model
- a `ThemePack` that resolves colors, typography, and shapes
- token objects for spacing, radius, elevation, border, motion, width, and height
- composition locals for color tokens and typography tokens
- a small accessor object so feature code does not reach into raw implementation details

Relevant Builders files:

- `builders/core/models/src/commonMain/kotlin/net/metalbrain/builders/core/models/data/theme/ThemeConfig.kt`
- `builders/core/ui/src/commonMain/kotlin/net/metalbrain/builders/theme/BuildersTheme.kt`
- `builders/core/ui/src/commonMain/kotlin/net/metalbrain/builders/theme/BuildersThemePack.kt`
- `builders/core/ui/src/commonMain/kotlin/net/metalbrain/builders/theme/BuildersTypography.kt`
- `builders/core/ui/src/commonMain/kotlin/net/metalbrain/builders/theme/tokens/*`

### What not to copy literally

PaySmart already has:

- `AppThemeVariant`
- `AppThemeMode`
- `AppThemePreferenceRepository`
- `AppThemeViewModel`
- `AppThemePack`

So the target is not a rename. The target is to upgrade PaySmart's current theme implementation to the same structure quality as Builders while preserving PaySmart naming and launch identity.

Relevant PaySmart files:

- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Theme.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/ThemePack.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Dimens.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Type.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/theme/data/AppThemeMode.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/theme/data/AppThemePreferenceRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/theme/viewmodel/AppThemeViewModel.kt`

## Theme Gaps In PaySmart Today

### 1. Spacing is alias-heavy

Current `Dimens.kt` mixes:

- new spacing names
- legacy aliases
- screen padding
- button dimensions
- card dimensions

This makes it harder to reason about the spacing system and encourages feature code to keep using mixed naming.

### 2. Typography is good but not tokenized

`Type.kt` already has a strong typography scale, but there is no separate typography token object comparable to Builders' `DSTypographyTokens`.

That means features use `MaterialTheme.typography` directly, but there is no stable intermediate layer for semantic usage such as:

- display
- heading
- body
- label
- caption

### 3. Theme pack is style-heavy but token-light

`ThemePack.kt` contains:

- color schemes
- button style flags
- security style flags
- background palettes

That is useful, but it is not yet a full design-token system. There are no first-class token objects for:

- spacing
- radius
- elevation
- border
- width
- height
- motion

### 4. Dynamic color can introduce drift

`Theme.kt` still supports dynamic color for the PaySmart variant. For a design-system migration, dynamic color increases unpredictability while the token system is being normalized.

For launch-facing consistency, the safer default is:

- keep the capability
- disable it by default during the Phase 2 stabilization pass

## Phase 2 Plan

### Phase 2A: Stabilize `:core:ui`

Goal: make `:core:ui` behave like a design system module, not just a folder of moved files.

#### Add token objects under `core/ui`

Create:

- `ui/theme/tokens/DSColorTokens.kt`
- `ui/theme/tokens/DSSpacingTokens.kt`
- `ui/theme/tokens/DSRadiusTokens.kt`
- `ui/theme/tokens/DSElevationTokens.kt`
- `ui/theme/tokens/DSBorderTokens.kt`
- `ui/theme/tokens/DSTypographyTokens.kt`
- `ui/theme/tokens/DSWidthTokens.kt`
- `ui/theme/tokens/DSHeightTokens.kt`
- `ui/theme/tokens/DSMotionTokens.kt`

These should mirror the Builders structure, but with PaySmart values and naming where appropriate.

#### Add a PaySmart theme config object

Create:

- `ui/theme/AppThemeConfig.kt`

Suggested shape:

- `variant: AppThemeVariant`
- `mode: AppThemeMode`
- `fontSet: AppThemeFontSet`
- optional `dynamicColorEnabled: Boolean`

This lets theme selection become one coherent model instead of being split across separate inputs.

#### Add a token accessor object

Refactor `Theme.kt` so it exposes a stable accessor object similar to Builders:

- `PaysmartTheme.colorTokens`
- `PaysmartTheme.typographyTokens`
- `PaysmartTheme.spacing`
- `PaysmartTheme.radius`
- `PaysmartTheme.elevation`
- `PaysmartTheme.border`
- `PaysmartTheme.motion`
- `PaysmartTheme.width`
- `PaysmartTheme.height`

This gives feature code a cleaner API than raw `Dimens` plus scattered style objects.

#### Convert `Dimens.kt` into a compatibility layer

Keep `Dimens` for now, but change its role:

- `Dimens` becomes a compatibility wrapper over `DSSpacingTokens`, `DSHeightTokens`, and `DSWidthTokens`
- stop introducing new feature code directly against `Dimens`
- new code should use token accessors

#### Split shape and typography semantics

Keep Material3 `Typography` and `Shapes`, but derive them from tokens:

- typography built from `DSTypographyTokens`
- shapes built from `DSRadiusTokens`

This keeps Material3 integration clean while making the underlying system more explicit.

#### Move theme state into `:core:ui` or `:core:models`

At minimum, move:

- `AppThemeMode.kt`

Potentially also move:

- `AppThemePreferenceRepository.kt`
- `AppThemeViewModel.kt`

The safer Phase 2A split is:

- move `AppThemeMode.kt` into `:core:ui` or `:core:models`
- keep repository and ViewModel in `:app` until feature modules are extracted

#### Dynamic color decision

For Phase 2A:

- default PaySmart to token-defined colors, not dynamic colors
- keep dynamic color available behind an explicit opt-in if needed later

This reduces visual drift across devices during design-system normalization.

### Phase 2B: Extract `:feature:invoicing`

Goal: extract the first feature module on top of a stable `:core:ui`.

Why invoicing first:

- the package is already cohesive
- the backend contract is stable
- the product direction is invoice-led UK first
- the PDF work and invoice UI polish already raised its priority

Target contents:

- invoice screens
- invoice cards
- invoice viewmodels
- invoice state
- invoice feature navigation entry points

Keep API contracts unchanged.

### Phase 2C: Extract `:feature:notifications`

Goal: isolate the notification center, unread badge, and inbox surfaces.

Why second:

- the domain is now well-defined after Phases 1 to 5 of notifications work
- it is less entangled with onboarding than auth/profile

Target contents:

- notification center screens
- notification viewmodels and state
- notification cards/list items
- notification navigation entry points

### Phase 2D: Extract `:feature:identity`

Goal: isolate the identity info, selection, upload, and pending-review flow.

Why third:

- identity UI was recently redesigned
- it already forms a distinct vertical
- it benefits directly from the stabilized spacing and typography system

Target contents:

- identity screens
- identity bottom sheets
- identity UI state and viewmodels
- identity feature navigation entry points

## Recommended File-Level Theme Work

### Refactor first

- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Dimens.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Type.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Theme.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/ThemePack.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/Shapes.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/ButtonTokens.kt`
- `core/ui/src/main/java/net/metalbrain/paysmart/ui/theme/HomeCardTokens.kt`

### Audit after tokenization

Audit all feature code still using:

- `Dimens.space*`
- `Dimens.smallSpacing`
- `Dimens.mediumSpacing`
- `Dimens.largeSpacing`
- mixed direct `dp` spacing where a token should exist

Replace those incrementally with token-backed values.

## Phase 2 Validation

### After Phase 2A

Must pass:

- `:app:compileDebugKotlin`
- `:app:compileDebugUnitTestKotlin`
- `:app:compileDebugAndroidTestKotlin`

Manual checks:

- startup
- auth screens
- onboarding screens
- profile/account screens
- invoice screens
- identity screens
- notification center

Visual checks:

- spacing rhythm is consistent
- typography hierarchy is stable
- cards use consistent radius/elevation semantics
- button sizing is consistent
- no dynamic-color drift on supported devices unless explicitly enabled

### After each feature extraction

Must pass:

- `:app:compileDebugKotlin`
- affected unit tests
- affected Android tests

Manual smoke test:

- route entry
- route exit
- primary CTA
- error state
- loading state

## Recommendation

Yes, PaySmart should emulate Builders' theme configuration pattern next.

But the correct translation is:

- keep PaySmart naming
- keep PaySmart launch brand
- copy Builders' theme architecture, not its visual identity

That makes Phase 2:

1. stabilize `:core:ui` with Builders-style token architecture
2. then extract `:feature:invoicing`
3. then `:feature:notifications`
4. then `:feature:identity`

This keeps the migration disciplined and gives the next feature modules a stable design-system base instead of forcing each extracted module to carry theme cleanup work with it.
