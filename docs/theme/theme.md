# Theme Defects And Fix

Date: 2026-07-29

## Defects Found

1. The app defaulted to the Obsidian variant.
   - `AppThemeConfig` defaulted to `AppThemeVariant.OBSIDIAN`.
   - `AppThemeViewModel` also started from `AppThemeVariant.OBSIDIAN`.
   - Result: the app loaded a separate visual pack instead of the requested PaySmart theme.

2. Theme config exposed more choices than the product needs.
   - The app had `SYSTEM`, `LIGHT`, and `DARK` modes plus a separate variant selector.
   - Requirement is two themes only: premium dark and light.

3. Dynamic color was still part of theme resolution.
   - Dynamic Material color could override brand surfaces when enabled.
   - That made the visual system less predictable.

4. Backgrounds and home account cards used gradients.
   - `PaySmartAppBackground` rendered a vertical gradient.
   - Home balance/reward cards rendered green-tinted linear gradients.
   - Result: the dark surface looked decorative and heavy instead of clean.

5. Typography contributed to the zoomed feel.
   - The removed Obsidian typography scale increased several display and headline sizes.
   - Some typography used negative letter spacing, which made compact layouts feel tighter.

6. App-level zoom was not found.
   - Source review found no root `LocalDensity`, `fontScale`, `Density`, or display-metric override in the app shell.
   - The provided screenshot is 1080 x 2176 px. On many Android devices that maps to a compact-width Compose viewport, so large display/font settings on the device can make the UI feel zoomed.
   - Fix applied in app: remove the enlarged theme scale and use a tighter default type scale while keeping `sp` units so Android accessibility settings still work.

## Fix Applied

- Removed `AppThemeVariant` and the theme-variant DataStore/ViewModel flow.
- Collapsed `AppThemeMode` to `LIGHT` and `DARK`; stale/unknown stored values fall back to `DARK`.
- Rebuilt `PaysmartTheme` around one PaySmart pack with two fixed schemes.
- Removed dynamic color from active theme resolution.
- Replaced Obsidian color/typography tokens with clean light and premium dark PaySmart tokens.
- Replaced the shared app background gradient with a solid token background.
- Removed green gradients from the home balance and reward cards.
- Neutralized the home account-information card gradients by passing same-color brushes.
- Removed the profile theme-pack row; Appearance now toggles only between Light and Premium dark.
- Updated theme labels and removed stale Obsidian resource strings.

## Verification

Command:

```powershell
.\gradlew.bat :core:ui:compileDebugKotlin :feature:home:compileDebugKotlin :feature:profile:compileDebugKotlin :app:compileDebugKotlin
```

Result: build successful.
