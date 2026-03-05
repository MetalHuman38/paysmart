# PaySmart Architecture (Pitch View)

_Last updated: 2026-03-02_

## 1) One-page Summary
PaySmart is a mobile-first payments platform with a security-first core:
- Kotlin Android app using offline-first state (`server -> Room -> local UI`).
- Firebase Functions Gen2 API for policy, KYC, payments, and pricing.
- Secure identity verification pipeline (client-side encryption + attested commit + server-side review worker).
- Stripe sandbox-ready add-money rail with webhook settlement and wallet write-through.
- Live FX quote service with provider caching and fallback behavior.

## 2) System Context (End-to-End)
```mermaid
flowchart LR
    U[User]
    A[PaySmart Android App\nJetpack Compose + ViewModels + Room]
    FA[Firebase Auth + App Check]
    API[Functions Gen2 API\nExpress handlers]

    FS[(Cloud Firestore)]
    ST[(Cloud Storage)]
    KMS[Cloud KMS]
    PI[Play Integrity API]
    STRIPE[Stripe Checkout + Webhooks]
    FX[Exchange Rate Provider API]

    W[Identity Review Worker\nprocessIdentityUploadReview]

    U --> A
    A --> FA
    A --> API

    API --> FS
    API --> ST
    API --> STRIPE
    API --> FX
    API --> PI
    API --> KMS

    FS --> W
    W --> ST
    W --> KMS
    W --> FS
```

## 3) Android Runtime Architecture
```mermaid
flowchart TD
    UI[Compose Screens\nHome / Profile / Add Money / Identity]
    NAV[NavGraph + Feature Gates]
    VM[ViewModels\nstate + orchestration]
    REPO[Repositories\nremote + cache strategy]
    ROOM[Room DAOs + Entities\nencrypted local DB]
    API[Functions API clients\nAuthApiConfig endpoints]

    UI --> NAV --> VM
    VM --> REPO
    REPO --> ROOM
    REPO --> API
    ROOM --> VM
```

### Current high-value app modules
- `ui/featuregate`: intent-based access gating + resume routes.
- `ui/profile/identity`: capture, encrypt, upload, attestation orchestration.
- `ui/home/addmoney`: Stripe session creation + status refresh.
- `ui/home/fx`: live quote models/repository and cache-backed quote state.
- `room/*`: local encrypted persistence and DAOs for wallet/profile/FX caches.

## 4) Backend Runtime Architecture (Functions)
```mermaid
flowchart TD
    R[HTTP Routes\nhealth/policy/fx/payment/auth]
    H[Handlers]
    U[Use Cases]
    D[Domain Models + Repositories]
    I[Infrastructure\nFirestore repositories + DI]
    S[External Services\nStripe / KMS / Integrity / FX]

    R --> H --> U --> D --> I --> S
    I --> F[(Firestore/Storage)]
```

### Implemented route families
- `payment.route.ts`: add-money session + status + Stripe webhook.
- `fx.route.ts`: quote endpoint (`/api/quotes`).
- `policy.route.ts` + auth handlers: onboarding/security/policy operations.
- identity handlers + worker: upload session/commit + async review transition.

## 5) Core Flow: Identity Verification (Compliance Path)
```mermaid
sequenceDiagram
    participant App as Android App
    participant API as Functions API
    participant GCS as Cloud Storage
    participant PI as Play Integrity
    participant KMS as Cloud KMS
    participant Worker as Review Worker
    participant FS as Firestore

    App->>App: Capture document (camera overlay)
    App->>App: On-device authenticity check (pluggable)
    App->>App: Encrypt payload (AES-GCM + AAD)

    App->>API: POST /auth/identity/upload/session
    API->>KMS: Create envelope key
    API-->>App: sessionId + signed uploadUrl + nonce + AAD

    App->>GCS: PUT encrypted payload
    App->>PI: Generate attestation JWT
    App->>API: POST /auth/identity/upload/commit
    API->>FS: status = pending_review

    FS-->>Worker: onDocumentUpdated trigger
    Worker->>GCS: Download ciphertext
    Worker->>KMS: Decrypt envelope key
    Worker->>Worker: Decrypt + hash verify
    Worker->>FS: status = verified OR rejected
```

Why this matters for pitch:
- Sensitive IDs are not uploaded in plaintext.
- Verification is cryptographically bound to device/session context.
- State transitions are auditable (`pending_review -> verified/rejected`).

## 6) Core Flow: Add Money (Stripe Sandbox Rail)
```mermaid
sequenceDiagram
    participant App as Android AddMoney UI
    participant API as Functions API
    participant Stripe as Stripe Checkout
    participant Webhook as Stripe Webhook Handler
    participant FS as Firestore Wallet

    App->>API: POST /payments/add-money/session
    API->>Stripe: Create Checkout Session
    API-->>App: checkoutUrl + sessionId

    App->>Stripe: Open checkoutUrl (external)
    Stripe->>Webhook: checkout.session.* event
    Webhook->>FS: Idempotent settlement + wallet write-through

    App->>API: GET /payments/add-money/session/:sessionId
    API-->>App: created/pending/succeeded/failed/expired
    App->>App: sync wallet (server -> Room -> UI)
```

## 7) Core Flow: Live FX Quotes
```mermaid
sequenceDiagram
    participant App as Android FX/Top-up UI
    participant API as /api/quotes
    participant Cache as In-memory provider cache
    participant Upstream as Exchange Rate API
    participant Room as Room fx_quote_cache

    App->>API: GET /api/quotes?source&target&amount&method
    API->>Cache: lookup pair
    alt cache hit
        API-->>App: quote + X-Rate-Source=cache
    else cache miss
        API->>Upstream: fetch pair rate
        API->>Cache: store TTL
        API-->>App: quote + X-Rate-Source=upstream
    end

    App->>Room: upsert quote snapshot
    App->>Room: fallback read if server unavailable
```

## 8) Trust Boundaries and Controls
- Client boundary:
  - App Check/Auth token required for sensitive API surfaces.
  - Identity payload encrypted before transport.
  - Offline hard-gate screen prevents partial/undefined client state when network is unavailable.
- API boundary:
  - Route-level validation and explicit status/error contracts.
  - Stripe webhook signature verification (or controlled unsigned mode in non-prod).
- Data boundary:
  - Firestore/Storage persistence with server-owned write paths for wallet settlement.
  - KMS-backed envelope key management for identity payloads.

## 9) Business Positioning Narrative (for pitch)
Use this architecture to tell a clear story:
1. **Trust-first payments foundation**: identity, attestation, and encrypted document pipeline.
2. **Monetizable rails active early**: add-money checkout with deterministic settlement.
3. **Cross-border readiness**: live FX quote layer and method-based fee model.
4. **Scale-ready product spine**: modular Kotlin + Functions architecture, offline-first UX, auditable backend transitions.

## 10) Observability and Cost Controls
- Firebase Analytics + Crashlytics for product/exception telemetry.
- Firebase Performance Monitoring integrated behind app-level wrapper:
  - release-only collection by default
  - local/debug collection disabled for noise/cost control
  - focused traces on critical network journeys (identity + add-money)
- Session lock diagnostics and route traces retained to verify lock lifecycle behavior.

## 11) Suggested Slide Mapping
- Slide 1: System context diagram + value proposition.
- Slide 2: Identity + compliance flow (why safer than naive upload).
- Slide 3: Add-money + FX monetization engine (unit economics + growth path).

---

If you want, I can generate a second version as a pure investor deck outline (`10-12 slides`) with speaker notes tied to this architecture.
