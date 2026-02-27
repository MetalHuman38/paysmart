# Contributing to PaySmart

Thank you for your interest in contributing to **PaySmart** (built by **VoltService Ltd**).

This project spans Android (Kotlin/Compose), Firebase Functions (TypeScript), and hosted public content.  
Contributions are welcome across product, engineering, QA, docs, and security.

## Before You Start

1. Read `README.md` for architecture and setup context.
2. Review current planning docs in `app/docs/`:
   - `app/docs/implementation_backlog.md`
   - `app/docs/architecture_pitch.md`
   - `app/docs/identity-upload-pipeline.md`
   - `app/docs/send_money_port_trace.md`
3. Prefer small, focused contributions tied to one ticket/task.

## Ways to Contribute

- Fix bugs and regressions.
- Implement backlog tickets.
- Improve tests and CI stability.
- Improve docs and developer onboarding.
- Improve accessibility, UX clarity, and localization quality.

## Creating Good Issues

Open one issue per problem/request and include:

- **Type**: bug, enhancement, docs, security-hardening, test gap.
- **Scope**: affected module/path (example: `app/src/main/...`, `functions/src/api/...`).
- **Context**: what you were doing and why it matters.
- **Expected vs actual behavior**.
- **Repro steps** (numbered, deterministic).
- **Environment**:
  - OS + device/emulator
  - app build type (debug/release)
  - local/emulator/cloud backend
- **Artifacts**: logs, screenshots, traces, failing command output.
- **Acceptance criteria**: objective done conditions.

If the issue is security-sensitive, do **not** post exploit details publicly. Use private disclosure (see Security section below).

## Pull Request Guidelines

### Branch and scope

- Keep PRs narrowly scoped (single feature/fix where possible).
- Use descriptive branch names, for example:
  - `feat/send-002-method-picker`
  - `fix/identity-ocr-failure-mapping`
  - `docs/readme-contributor-tasks`
- Reference ticket IDs in PR title and commits when available (example: `SEND-003`, `SEC-001`).

### PR description checklist

Include the following:

- Problem statement.
- Proposed solution and tradeoffs.
- Files/modules touched.
- Risk and rollback notes.
- Test evidence (commands + result summary).
- Screenshots/video for UI changes.

### Quality bar

- No secrets or credentials in code, logs, screenshots, or test fixtures.
- Keep naming clear and behavior explicit.
- Add/adjust tests for behavior changes.
- Avoid unrelated refactors in the same PR.
- Update docs when changing contracts, flow behavior, or architecture.

## Local Validation Commands

Run relevant checks before opening a PR.

### Android

```bash
./gradlew :app:compileDebugKotlin --no-configuration-cache
./gradlew :app:testDebugUnitTest --no-configuration-cache
./gradlew :app:lintDebug --no-configuration-cache
```

### Functions

```bash
cd functions
npm run install:all
npm run build
npm run test
```

For API-only work:

```bash
cd functions/src/api
npm run build
npm run test
```

## Security Reporting

For vulnerabilities, please avoid public exploit disclosure in Issues/PRs.

- Open a private GitHub security advisory if available, or
- Contact maintainers through repository owner channels and share:
  - impact
  - affected paths
  - minimal repro
  - suggested mitigation

## Community and Behavior Expectations

We expect all contributors to engage professionally and respectfully.

- Be constructive and factual in technical discussions.
- Challenge ideas, not people.
- Keep feedback specific, actionable, and evidence-based.
- No harassment, discrimination, or abusive behavior.

Reference standard: [Contributor Covenant Code of Conduct](https://www.contributor-covenant.org/version/2/1/code_of_conduct/).

## External References

- Android passkeys: <https://developer.android.com/identity/passkeys/create-passkeys>
- Google Cloud Vision setup: <https://docs.cloud.google.com/vision/docs/setup>
- Firebase Functions Gen2: <https://firebase.google.com/docs/functions/2nd-gen-upgrade>
- Stripe Android SDK: <https://docs.stripe.com/sdks/android>

## License

By contributing, you agree that your contributions are provided under the repository license (`LICENSE`).
