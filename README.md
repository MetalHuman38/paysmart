# PaySmart

Last updated: March 2, 2026

PaySmart is a security-first mobile payments product built by **VoltService Ltd**.  
The platform is focused on trusted onboarding, policy-driven feature access, secure identity verification, add-money rails, and FX-ready transfer flows.

This repository contains the Android client, Firebase Functions Gen2 backend, and the hosted public site.

## Product Snapshot

- Kotlin Android app with offline-first state flow (`server -> Room -> local UI`).
- Firebase Functions Gen2 API for policy, identity, FX, and payments.
- Encrypted identity upload pipeline with attestation and async review worker.
- Stripe sandbox add-money flow with webhook settlement and wallet write-through.
- Capability- and profile-based feature gating to keep compliance and UX aligned.

## Architecture Overview

At a high level:

- **App**: Jetpack Compose UI + ViewModels + Room cache + repository orchestration.
- **Backend**: Express handlers inside Functions Gen2 with use-case/domain layering.
- **Data**: Firestore for state, Storage for encrypted payload objects, KMS for envelope keys.
- **Integrations**: Stripe, Play Integrity, Exchange Rate provider APIs, and Google Cloud services.

See detailed diagrams and flow writes in `app/docs/architecture_pitch.md`.

## Repository Layout

- `app/`: Android application (Compose, ViewModels, Room, feature modules).
- `functions/`: Firebase Functions codebases (`api`, `auth`, `security`, `echo`).
- `public/`: Hosted website, policy pages, investor/architecture content, `.well-known`.
- `app/docs/`: Product and implementation docs used for planning and investor narrative.

## Core Docs

- `app/docs/architecture_pitch.md`: End-to-end system diagrams and pitch-ready architecture narrative.
- `app/docs/identity-upload-pipeline.md`: Identity encryption/upload/attestation/review model.
- `app/docs/phone-verification-pipeline.md`: OTP pipeline and PNV-ready abstraction strategy.
- `app/docs/implementation_backlog.md`: Ticketed backlog across API and Android vertical slices.
- `app/docs/send_money_port_trace.md`: VoltPay-to-PaySmart send-money port trace and rollout plan.

## Local Development

### Prerequisites

- Android Studio (latest stable)
- JDK 21
- Node.js 24
- Firebase CLI
- Google Cloud CLI (for deployment and cloud config tasks)

### Android

```bash
./gradlew :app:compileDebugKotlin --no-configuration-cache
```

### Functions

Run from repo root:

```bash
cd functions
npm run install:all
npm run build
npm run test
```

Run emulators:

```bash
cd functions
npm run serve
```

## Security and Compliance Notes

- Do not commit secrets, API keys, service account JSON, or signing assets.
- Use environment variables/secrets for Stripe, KMS, attestation, and provider credentials.
- Identity documents are expected to follow encrypted upload and attested commit paths.

## Performance and Reliability Baseline

- Firebase Performance Monitoring is wired through a centralized monitor service (`core/service/performance`).
- Collection is cost-controlled:
  - debug/local (`BuildConfig.IS_LOCAL`): disabled
  - release (Play/internal testing): enabled
- Release traces are attached to the highest-risk network paths:
  - `add_money_create_session`
  - `add_money_get_session_status`
  - `identity_create_upload_session`
  - `identity_upload_encrypted_payload`
  - `identity_commit_upload`
- Runtime reliability guardrails now include:
  - hard offline gate screen before app navigation resumes
  - biometric setup routing that resolves to `CreatePassword` or `Home` based on local password readiness
  - idle session watcher reset on unlock to prevent immediate re-lock loops

## Admin Panel Roadmap

- Minimal admin panel scope and hardening model are tracked in:
  - `app/docs/admin_panel_roadmap.md`

## Contributors Needed

PaySmart is actively seeking contributors. The roadmap is implementation-heavy, and high-quality contributors can have immediate impact on production direction.

Priority tracks:

| Track                          | Needed Now                                                                         | Start Here                                                 |
|--------------------------------|------------------------------------------------------------------------------------|------------------------------------------------------------|
| Android (Kotlin/Compose)       | Send-money vertical slices, passkey UX hardening, accessibility polishing          | `app/docs/send_money_port_trace.md`                        |
| Backend (TypeScript/Functions) | Identity provider contracts, FX reliability, settlement hardening                  | `app/docs/implementation_backlog.md`                       |
| Security/Trust                 | App Check enforcement strategy, attestation verification depth, threat-model tests | `app/docs/identity-upload-pipeline.md`                     |
| QA/Automation                  | End-to-end regression packs, emulator parity checks, CI reliability                | `functions/src/api/src/**/*.test.ts` + Android test suites |
| Product/Docs                   | Clear product education, investor updates, architecture communication              | `app/docs/architecture_pitch.md`                           |

## Good First Tasks by Role

The tasks below are scoped for new contributors and mapped to backlog/port-trace IDs.

### Android (Kotlin/Compose)

| Ticket     | Task                                                            | Estimate | Done When                                                                     |
|------------|-----------------------------------------------------------------|----------|-------------------------------------------------------------------------------|
| `SEND-001` | Add recipient domain models + Room draft cache repository       | 1-2 days | Recipient draft persists across app restart and restores into ViewModel state |
| `SEND-002` | Build method picker/details/review screens + NavGraph wiring    | 2-3 days | End-to-end recipient step flow works from Home -> Send Money                  |
| `SEND-003` | Couple send flow to live FX quote state + quote snapshot resume | 1-2 days | Quote refresh and resume snapshot restore work after process kill             |
| `APP-020`  | Port VoltPay quote contract parity into Kotlin quote UI state   | 1-2 days | Quote mapping covers contract fields and cache fallback behavior              |

### Backend (Functions/TypeScript)

| Ticket     | Task                                                                          | Estimate  | Done When                                                                         |
|------------|-------------------------------------------------------------------------------|-----------|-----------------------------------------------------------------------------------|
| `SEND-004` | Add transfer API contracts (`recipient/validate`, transfer quote/create path) | 2-3 days  | Handlers/use-cases/repository tests pass and contracts are documented             |
| `SEC-001`  | Harden identity OCR review outcomes and failure reason mapping                | 1 day     | Review worker emits deterministic `reviewDecisionReason` codes for OCR edge cases |
| `SEC-002`  | Extend passkey endpoint tests for challenge expiry/replay scenarios           | 0.5-1 day | Negative-path tests pass for expired/missing/reused challenges                    |

### Security/Trust

| Ticket    | Task                                                    | Estimate  | Done When                                                            |
|-----------|---------------------------------------------------------|-----------|----------------------------------------------------------------------|
| `SEC-003` | Add threat-model checklist for identity upload pipeline | 1 day     | Checklist committed under `app/docs` and linked to test/backlog work |
| `SEC-004` | Add App Check enforcement matrix (dev/staging/prod)     | 0.5-1 day | Environment matrix documented with recommended defaults              |

### QA/Automation

| Ticket   | Task                                                         | Estimate  | Done When                                                                 |
|----------|--------------------------------------------------------------|-----------|---------------------------------------------------------------------------|
| `QA-001` | Add emulator E2E smoke for identity upload fallback endpoint | 1-2 days  | Script validates session -> payload upload -> commit -> review transition |
| `QA-002` | Add add-money session status regression matrix               | 0.5-1 day | Tests cover pending/succeeded/failed/expired status behavior              |

### Product/Docs

| Ticket    | Task                                                                  | Estimate | Done When                                                           |
|-----------|-----------------------------------------------------------------------|----------|---------------------------------------------------------------------|
| `DOC-001` | Keep root README + architecture pitch in sync per milestone           | 0.5 day  | `README.md` and `app/docs/architecture_pitch.md` updated in same PR |
| `DOC-002` | Publish monthly build update in `public/updates` with shipped tickets | 0.5 day  | Update references ticket IDs and merged change links                |

If you want to contribute:

1. Pick one ticket above and comment intent/scope.
2. Open a focused PR with tests and a short rollback note.
3. Reference the ticket ID in PR title and commit message.

## Current Direction

Near-term focus:

- Complete send-money recipient and quote-driven flow.
- Expand compliance-grade identity verification and provider handoff.
- Harden payment and wallet state transitions for test-user pilots.
- Improve onboarding clarity while keeping feature access policy-driven.

PaySmart is being built to become a trusted payment gate for sending, receiving, and paying online with security and compliance as first-class product behavior.
