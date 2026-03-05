# PaySmart Implementation Backlog

_Last updated: 2026-03-05_

## Status legend
- `done`: implemented and validated in this slice
- `next`: queued for next slice

## Tickets

### API-010 (`done`)
- **Scope**: create Stripe sandbox add-money session endpoint.
- **Functions modules**:
  - `functions/src/api/src/http/payment.route.ts`
  - `functions/src/api/src/handlers/createAddMoneySession.ts`
  - `functions/src/api/src/application/usecase/CreateAddMoneySession.ts`
  - `functions/src/api/src/domain/model/addMoney.ts`
  - `functions/src/api/src/domain/repository/AddMoneyRepository.ts`
  - `functions/src/api/src/infrastructure/firestore/FirestoreAddMoneyRepository.ts`
  - `functions/src/api/src/services/stripePaymentsService.ts`
  - `functions/src/api/src/infrastructure/di/authContainer.ts`
  - `functions/src/api/src/config/configuration.ts`
  - `functions/src/api/src/server.ts`
  - `functions/src/api/src/index.ts`
- **API contract**:
  - `POST /payments/add-money/session`
  - auth: `Authorization: Bearer <idToken>`
  - request:
    - `amountMinor` (int, required)
    - `currency` (ISO 4217, required)
    - `idempotencyKey` (string, optional)
  - response `200`:
    - `sessionId`, `checkoutUrl`, `amountMinor`, `currency`, `status`, `expiresAtMs`
  - errors:
    - `400` invalid/unsupported payload
    - `401` missing token
    - `503` Stripe config missing
- **Acceptance tests**:
  - `functions/src/api/src/handlers/createAddMoneySession.test.ts`

### API-012 (`done`)
- **Scope**: webhook settlement + session status for wallet credit and idempotent write-through.
- **Functions modules**:
  - `functions/src/api/src/handlers/stripeWebhook.ts`
  - `functions/src/api/src/handlers/getAddMoneySessionStatus.ts`
  - `functions/src/api/src/application/usecase/HandleStripeWebhook.ts`
  - `functions/src/api/src/application/usecase/GetAddMoneySessionStatus.ts`
  - `functions/src/api/src/infrastructure/firestore/FirestoreAddMoneyRepository.ts`
  - `functions/src/api/src/services/stripePaymentsService.ts`
  - `functions/src/api/src/http/payment.route.ts`
  - `functions/src/api/src/utils.ts`
- **API contract**:
  - `POST /payments/stripe/webhook`
    - body: Stripe event payload (`application/json`)
    - header: `Stripe-Signature` (validated when webhook secret configured)
    - response `200`: `{ ok, handled, sessionId, uid, status }`
  - `GET /payments/add-money/session/:sessionId`
    - auth: `Authorization: Bearer <idToken>`
    - response `200`: session status payload
  - settlement behavior:
    - credits `users/{uid}/wallet/current.balancesByCurrency`
    - mirrors `balancesByCurrency` on root `users/{uid}`
    - writes `users/{uid}/walletTransactions/{sessionId}`
    - guarded by `walletAppliedAt` idempotency gate
- **Acceptance tests**:
  - `functions/src/api/src/handlers/stripeWebhook.test.ts`
  - `functions/src/api/src/handlers/getAddMoneySessionStatus.test.ts`

### APP-010 (`done`)
- **Scope**: Kotlin Add Money UI flow wired to new API endpoints and wallet sync.
- **Android modules**:
  - `app/src/main/java/net/metalbrain/paysmart/core/auth/AuthApiConfig.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/addmoney/AddMoneyModels.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/addmoney/AddMoneyRepository.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/viewmodel/AddMoneyViewModel.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/screen/AddMoneyScreen.kt`
  - `app/src/main/res/values*/strings.xml` (new add-money keys in all locales)
- **Client flow contract**:
  - create checkout session from screen
  - open returned `checkoutUrl` externally
  - refresh session status from screen
  - on `succeeded`, trigger wallet sync (`server -> Room -> local`)
- **Acceptance checks**:
  - `./gradlew :app:mergeDebugResources :app:compileDebugKotlin --no-daemon` passes

### APP-020 (`next`)
- **Scope**: Live exchange-rate wiring ported from Flutter/Go contract.
- **Mirror source contracts**:
  - Flutter:
    - `C:/Users/Metal/voltpay/lib/features/rates/domain/quote.dart`
    - `C:/Users/Metal/voltpay/lib/features/rates/services/get_quotes_service.dart`
    - `C:/Users/Metal/voltpay/lib/features/rates/providers/quote_provider.dart`
  - Go:
    - `C:/Users/Metal/voltpay/voltpay_go/internal/handlers/quotes.go`
    - `C:/Users/Metal/voltpay/voltpay_go/internal/handlers/upstream.go`
    - `C:/Users/Metal/voltpay/voltpay_go/internal/handlers/ratecache.go`
- **Planned Kotlin modules**:
  - `app/src/main/java/net/metalbrain/paysmart/ui/rates/*` (UI + ViewModel)
  - `app/src/main/java/net/metalbrain/paysmart/data/repository/QuoteRepository.kt`
  - Room cache entity/dao for quote snapshots + cache metadata
- **Target API contract**:
  - `GET /api/quotes?source=USD&target=NGN&amount=100&method=wire`
  - response:
    - `sourceCurrency`, `targetCurrency`, `sourceAmount`, `rate`
    - `recipientAmount`, `fees[]`, `guaranteeSeconds`, `arrivalSeconds`
- **Acceptance tests (planned)**:
  - mapping tests for JSON -> Kotlin quote model
  - repository tests for online + cache fallback
  - ViewModel tests for stale/refresh states

### APP-021 (`done`)
- **Scope**: biometric opt-in route fix with password-ready branching.
- **Android modules**:
  - `app/src/main/java/net/metalbrain/paysmart/ui/NavGraph.kt`
- **Behavior contract**:
  - after biometric success/skip:
    - route to `Home` if `passwordEnabled && localPasswordSetAt != null`
    - otherwise route to `CreatePassword`
  - avoids regressions where flow returned to `ProtectAccount`.

### APP-022 (`done`)
- **Scope**: session idle lock re-trigger fix after first unlock.
- **Android modules**:
  - `app/src/main/java/net/metalbrain/paysmart/core/session/IdleSessionManager.kt`
- **Behavior contract**:
  - reset idle baseline whenever idle watcher is re-enabled.
  - prevents immediate stale-timestamp lock after unlock.

### PERF-001 (`done`)
- **Scope**: Firebase Performance Monitoring integration with cost controls.
- **Android modules**:
  - `app/build.gradle.kts`
  - `app/src/main/AndroidManifest.xml`
  - `app/src/main/java/net/metalbrain/paysmart/App.kt`
  - `app/src/main/java/net/metalbrain/paysmart/core/service/performance/*`
  - `app/src/main/java/net/metalbrain/paysmart/core/features/addmoney/repository/AddMoneyRepository.kt`
  - `app/src/main/java/net/metalbrain/paysmart/core/features/identity/provider/RemoteIdentityUploadRepository.kt`
- **Behavior contract**:
  - collection disabled for local/debug, enabled for release
  - targeted traces for add-money and identity upload paths only
  - no direct Firebase Perf SDK calls from screen layer.

### ADMIN-001 (`next`)
- **Scope**: minimal admin panel foundation on existing Firebase Hosting.
- **Reference doc**:
  - `app/docs/admin_panel_roadmap.md`
- **Planned acceptance checks**:
  - role-gated admin auth
  - audit trail writes for status-changing actions
  - strict endpoint surface (no public mutation APIs).

### REL-003 (`done`)
- **Scope**: release-note slice for current public tester build and docs alignment.
- **Docs modules**:
  - `app/docs/playstore_release_notes.md`
  - `app/docs/implementation_backlog.md`
- **Release summary**:
  - improved identity verification reliability and security across upload, encryption, and commit steps
  - fixed final submission issue in document verification
  - added Flutterwave add-money provider flow (alongside Stripe) for supported regions
  - improved add-money stability with clearer payment/webhook error handling
  - general bug fixes, UI polish, and performance improvements
