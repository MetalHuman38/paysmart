# Minimal Admin Panel Roadmap

_Last updated: 2026-03-03_

## Goal
Ship a minimal but production-safe admin surface for tester operations without introducing a parallel backend stack.

## Current Priority
Enable operator visibility first:
- inspect per-user session lock/session version state
- pull an activity timeline across identity, payments, and wallet events
- keep read paths isolated from mutation/admin override paths

## Can we use the already deployed Firebase Hosting?
Yes, with strict boundaries:
- Host an admin-only frontend under a dedicated path (`/admin`) or subdomain (`admin.pay-smart.net`).
- Require Firebase Auth sign-in and server-enforced admin claims for every privileged action.
- Keep all mutations in Functions Gen2 endpoints; do not write privileged data directly from client SDK calls.
- Enforce App Check (web provider) and rate limits on admin mutation endpoints.
- Add explicit audit logs for every admin action.

## Target Scope (MVP)
1. Identity review queue:
   - list pending identity sessions
   - view review metadata
   - apply `verified` / `rejected` decision + reason
2. User security overrides:
   - force session lock
   - temporary kill-switch activate/deactivate
3. Payments operations view:
   - search add-money sessions
   - inspect Stripe status mirror and webhook settlement state

## Architecture Fit
- Frontend: static admin web app on Firebase Hosting.
- AuthN/AuthZ: Firebase Auth + custom claims (`admin=true`) + optional allowlist.
- API: Functions Gen2 admin route group (`/admin/*`) with strict middleware.
- Data: Firestore remains source-of-truth; all writes are auditable.

## Monitoring Slice (Implemented First)
### ADMIN-000 (Read-only Monitoring Baseline)
- Middleware:
  - `requireAdmin` verifies Firebase ID token and permits:
    - `admin=true` custom claim, or
    - `ADMIN_ALLOWLIST_EMAILS` allowlist fallback
- Endpoints:
  - `GET /admin/ping`
  - `GET /admin/monitor/users/{uid}/session`
  - `GET /admin/monitor/users/{uid}/activity?limit=25`
- Activity feed sources:
  - `users/{uid}/authSessionState/*`
  - `users/{uid}/identityUploads/*`
  - `users/{uid}/identityProviderSessions/*`
  - `users/{uid}/payments/add_money/sessions/*`
  - `users/{uid}/walletTransactions/*`
- Guardrails:
  - read-only contracts
  - bounded page size (`max=100`)
  - no privileged writes from admin UI in this slice

### Session Snapshot Contract
`GET /admin/monitor/users/{uid}/session`

Response shape:
- `uid`
- `sessionState`:
  - `activeSid`
  - `sessionVersion`
  - `lastIssuedAtMs`
  - `updatedAtMs`
- `securitySettings`:
  - `sessionLocked`
  - `killSwitchActive`
  - `lockAfterMinutes`
  - `biometricsEnabled`
  - `passwordEnabled`
  - `passcodeEnabled`
  - `updatedAtMs`

### Activity Feed Contract
`GET /admin/monitor/users/{uid}/activity?limit=25`

Response shape:
- `uid`
- `count`
- `perSourceLimit`
- `items[]` sorted newest-first:
  - `id`
  - `source` (`session_state`, `identity_upload`, `identity_provider`, `add_money`, `wallet_transaction`)
  - `status`
  - `summary`
  - `timestampMs`
  - `metadata` (source-specific)

## Delivery Phases

### ADMIN-001 (Foundation)
- Add admin claims middleware to Functions.
- Add `/admin/ping` and `/admin/me` contracts.
- Add audit log write helper (`adminAudit/{id}`).

### ADMIN-002 (Identity Queue)
- `GET /admin/identity/pending`
- `POST /admin/identity/{verificationId}/decision`
- Enforce decision reason taxonomy and immutable decision timestamps.

### ADMIN-003 (Security Controls)
- `POST /admin/users/{uid}/force-lock`
- `POST /admin/users/{uid}/kill-switch`
- Log actor uid, target uid, reason, and request correlation id.

### ADMIN-004 (Payments Ops)
- `GET /admin/payments/add-money/search`
- `GET /admin/payments/add-money/{sessionId}`
- Add reconciliation status field for webhook/app polling parity.

## Acceptance and Safety Checklist
- No admin API callable without verified admin claim.
- Every mutation writes an audit record.
- No public hosting route leaks PII without auth.
- Firestore rules continue to deny privileged writes from non-admin clients.
- Admin frontend has clear environment banner (`DEV`, `STAGING`, `PROD`) to prevent operator mistakes.

## Next Safe Steps
1. Add `GET /admin/me` with explicit role/capability matrix for UI gating.
2. Add correlation IDs to API responses and write-side audit logs.
3. Add Cloud Logging sink (BigQuery) for immutable long-term operator analytics.
4. Add read-only dashboard cards:
   - active lock ratio
   - pending identity count
   - payment pending vs settled ratio
