# PaySmart

Last updated: 2026-05-27

PaySmart is a security-first Android product built by **VoltService Ltd**. The current release track is invoice-led: free weekly invoice creation is the first public value surface, while regulated payment, wallet, identity, and provider testing paths remain separated behind security and compliance controls.

Current app version metadata is tracked in `version.properties` and resolved by `app/build.gradle.kts`:

- `PAYSMART_VERSION_CODE=16`
- computed `versionName`: `1.16.0-rc.2`
- Android package: `net.metalbrain.paysmart`
- SDKs: `minSdk 33`, `targetSdk 36`, `compileSdk 36`

## Product Snapshot

- Kotlin Android app using Jetpack Compose, Hilt, Room, Firebase, Credential Manager, CameraX, Stripe Android, and native C++ security/storage bridges.
- Invoice-first user path for UK weekly workers: worker profile, venue setup, postcode-led address lookup, weekly shifts, invoice finalization, and PDF actions.
- Policy-driven feature gates for sensitive actions, including invoice creation, add-money, send-money, and receive-money paths.
- Account security surfaces for passcode, password, passkeys, biometrics, recovery, and 2-step verification prompts.
- Identity verification pipeline with encrypted upload, attestation, OCR/review handling, and provider-ready boundaries.
- Add-money/provider testing through Stripe sandbox and Flutterwave API paths.
- Firebase Functions Gen 2 backend split into API and auth codebases.
- Public hosted site and docs in `public/`, currently positioned around free invoice tools and product preview.

## Architecture

The app is being reduced toward a thin shell. Shared platform code is moving into `core`, data ownership into `data`, and feature UI/viewmodels into `feature`.

Current Gradle modules:

- `:app` - Android application shell, root navigation, top-level DI, and app-owned flows not yet extracted.
- `:core:common`, `:core:navigation`, `:core:ui`, `:core:security`, `:core:database`, `:core:firebase`, `:core:invoice-models`, `:core:models`
- `:data:auth`, `:data:user`, `:data:wallet`, `:data:invoice`, `:data:notifications`
- `:feature:account`, `:feature:profile`, `:feature:home`, `:feature:notifications`, `:feature:wallet`

Large flows still app-owned until later extraction include identity, invoicing UI, transactions, send-money, and parts of onboarding/login/address setup.

Backend shape:

- `functions/src/api` - Express API handlers, payment/provider paths, passkeys, identity, invoice, and domain use cases.
- `functions/src/auth` - Firebase Auth blocking triggers and security policy synchronization.
- `functions/package.json` - root helper scripts for install, build, test, serve, deploy, and logs.

## Repository Layout

- `app/` - Android app module, app-owned flows, resources, tests, native C++ bridge, and Android docs.
- `core/` - reusable Android platform modules.
- `data/` - repository/data modules for auth, user, wallet, invoice, and notifications.
- `feature/` - extracted feature UI/viewmodel modules.
- `functions/` - Firebase Functions Gen 2 backend codebases.
- `public/` - hosted public site, public docs, policy pages, and update pages.
- `app/docs/` - release plans, architecture notes, migration plans, and implementation backlogs.

## Important Docs

- `app/docs/uk_weekly_invoicing_launch_plan.md` - invoice-led product direction and launch plan.
- `app/docs/remaining_migration_checklist.md` - current module state, release blockers, and shell-reduction work.
- `app/docs/architecture_pitch.md` - end-to-end architecture narrative.
- `app/docs/identity-upload-pipeline.md` - encrypted identity upload and review model.
- `app/docs/phone-verification-pipeline.md` - OTP and phone verification design.
- `app/docs/in_app_update_testing.md` - in-app update testing notes.
- `app/docs/playstore_release_notes.md` - current Play Store release-note draft.
- `app/docs/admin_panel_roadmap.md` - admin panel scope and hardening model.
- `functions/continuous-deployment.md` - GitHub to GCP continuous deployment setup for Firebase Functions and Cloud Run alternatives.

## Local Development

### Prerequisites

- Android Studio, current stable
- JDK 21
- Node.js 22
- Firebase CLI
- Google Cloud CLI for cloud configuration and deployment work

On this Windows workspace, Gradle is usually run with the explicit JDK override:

```powershell
.\gradlew.bat "-Dorg.gradle.java.home=C:\Program Files\Java\jdk-21" --no-daemon --no-configuration-cache :app:compileDebugKotlin
```

### Android Commands

Compile the app:

```powershell
.\gradlew.bat "-Dorg.gradle.java.home=C:\Program Files\Java\jdk-21" --no-daemon --no-configuration-cache :app:compileDebugKotlin
```

Run unit tests:

```powershell
.\gradlew.bat "-Dorg.gradle.java.home=C:\Program Files\Java\jdk-21" --no-daemon --no-configuration-cache :app:testDebugUnitTest
```

Run Android lint:

```powershell
.\gradlew.bat "-Dorg.gradle.java.home=C:\Program Files\Java\jdk-21" --no-daemon --no-configuration-cache :app:lintDebug
```

Build the release artifact:

```powershell
.\gradlew.bat "-Dorg.gradle.java.home=C:\Program Files\Java\jdk-21" --no-daemon --no-configuration-cache :app:assembleRelease
```

### Functions Commands

Run from `functions/`:

```bash
npm run install:all
npm run build
npm run test
```

Run local emulators:

```bash
npm run serve
```

Deploy Functions:

```bash
npm run deploy
```

## Local Configuration

Do not commit secrets, signing files, API keys, service account JSON, or local emulator data.

Common local values are read from Gradle properties, environment variables, `version.properties`, or `local.properties` depending on the setting:

- `LOCAL_DEV=true` switches debug builds to local emulator URLs.
- `LOCAL_API_BASE_URL` defaults to `http://10.0.2.2:5001/paysmart-7ee79/europe-west2/api`.
- `LOCAL_FUNCTION_API_URL` defaults to `http://10.0.2.2:5001/paysmart-7ee79/europe-west2`.
- `REMOTE_API_BASE_URL` defaults to `https://europe-west2-paysmart-7ee79.cloudfunctions.net/api`.
- `REMOTE_FUNCTION_API_URL` defaults to `https://europe-west2-paysmart-7ee79.cloudfunctions.net`.
- `MAPS_API_KEY` or `ADDRESS_VALIDATION_API_KEY` supplies the Android manifest maps/address placeholder.
- `STRIPE_PUBLISHABLE_KEY` supplies the Android Stripe publishable key.
- `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, and `RELEASE_KEY_PASSWORD` enable release signing.

Release builds enable App Check enforcement and Firebase Performance collection. Debug builds disable those and can run against local emulators.

## Current Release Status

The current migration checklist records these local release gates as green:

- `:app:compileDebugKotlin`
- `:app:testDebugUnitTest`
- `:app:lintDebug`
- `:app:assembleRelease`

The remaining release checks are operational rather than compile blockers:

- Device smoke test for account creation, sign-in, home, add money, notifications, invoice flow, passkey, and biometric unlock.
- Confirm no moved feature module depends on an app-owned implementation class.
- Confirm release signing, release network security config, and production Firebase config before bundling.

## Current Direction

Near-term work:

- Finish the invoice-led release path and keep invoice creation clearly separate from regulated payment initiation.
- Continue shell reduction by moving remaining profile/account surfaces and creating `:feature:identity`, `:feature:invoicing`, `:feature:transactions`, and `:feature:sendmoney`.
- Harden invoice PDF, share/download, venue address lookup, and payment-status visibility.
- Keep security prompts consistent across passkeys, biometrics, MFA, and transaction-critical routes.
- Keep lint/build gates green before each release candidate.

## Contributor Tracks

High-value contribution areas:

| Track | Current Focus | Start Here |
| --- | --- | --- |
| Android | Invoicing polish, feature extraction, device smoke tests, accessibility | `app/docs/remaining_migration_checklist.md` |
| Backend | Invoice PDF tasks, provider paths, identity review, passkey security | `functions/src/api/src` |
| Security | App Check, passkeys, MFA, encrypted identity upload, release signing checks | `app/docs/identity-upload-pipeline.md` |
| QA | End-to-end Android smoke tests and emulator parity | Android test suites and `functions/src/**/*.test.ts` |
| Product/Docs | Invoice-first copy, release notes, public site consistency | `app/docs/uk_weekly_invoicing_launch_plan.md` |

PaySmart is being built toward a trusted invoice and payment product where security, compliance, and user-visible clarity shape the product surface from the first screen.
