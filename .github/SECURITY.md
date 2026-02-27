# Security Policy

## Supported Versions

PaySmart is under active development. Security fixes are prioritized for:

| Branch           | Status              |
|------------------|---------------------|
| `main`           | Supported           |
| `develop`        | Best-effort support |
| Feature branches | Not supported       |

## Reporting a Vulnerability

Please do not disclose security vulnerabilities in public issues or pull requests.

Preferred process:

1. Open a private GitHub Security Advisory for this repository.
2. Include:
   - affected component/path
   - impact and attack scenario
   - reproducible steps or proof of concept
   - suggested mitigation (if available)
3. Wait for triage before public disclosure.

If private advisory is unavailable, contact repository maintainers through the configured owner channel and mark the message as `Security: Confidential`.

## What to Expect

- Initial triage target: within 3 business days.
- Status updates provided during investigation and fix rollout.
- Coordinated disclosure after patch validation.

## Scope Notes

Security-sensitive areas include:

- Authentication/session state and token validation
- Identity upload, encryption, OCR, and attestation flows
- Payment/session settlement and webhook handling
- Firestore/Storage security rules and data access paths

Please include logs, environment details, and exact API/screen flows where possible.
