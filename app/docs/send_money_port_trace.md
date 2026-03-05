# Send Money Port Trace (VoltPay -> PaySmart)

_Last updated: 2026-03-02_

## Goal
- Port VoltPay recipient-first transfer flow into PaySmart Kotlin without touching Stripe add-money settlement flow.
- Keep architecture aligned with `server -> Room -> local state`.

## VoltPay flow reference

### Entry points
- `C:/Users/Metal/voltpay/lib/features/home/ui/home_screen.dart`
  - Home calculator is embedded directly in Home.
  - CTA routes from quote context into recipient flow.
- `C:/Users/Metal/voltpay/lib/features/recipient/presentation/add_money_page.dart`
  - Separate transfer prep page.
  - Step order: amount/currencies -> quote/details -> `Add recipient`.

### Recipient orchestration
- `C:/Users/Metal/voltpay/lib/features/recipient/presentation/add_recipient_page.dart`
  - Method picker:
  - `voltpayLookup`
  - `bankDetails`
  - `documentUpload`
  - `emailRequest`
- `C:/Users/Metal/voltpay/lib/features/recipient/provider/recipient_provider.dart`
  - Recipient params union and API payload projection.
- `C:/Users/Metal/voltpay/lib/features/recipient/provider/recipient_controller.dart`
  - Merges method-specific forms into unified recipient payload.
- `C:/Users/Metal/voltpay/lib/features/recipient/provider/recipient_flow_controller.dart`
  - Step state: method picker -> details -> review -> done.

### FX quote state
- `C:/Users/Metal/voltpay/lib/features/rates/providers/quote_provider.dart`
  - Reactive quote params provider + quote fetch provider.
- `C:/Users/Metal/voltpay/lib/features/rates/domain/quote.dart`
  - Quote contract with fee lines and timing metadata.
- `C:/Users/Metal/voltpay/lib/features/rates/domain/pay_method_type.dart`
  - Method enum with API wire values.

## Current PaySmart state

### What exists now
- FX domain/repository + Room cache:
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/fx/FxModels.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/fx/FxQuoteRepository.kt`
  - `app/src/main/java/net/metalbrain/paysmart/room/entity/FxQuoteCacheEntity.kt`
  - `app/src/main/java/net/metalbrain/paysmart/room/doa/FxQuoteCacheDao.kt`
- Add money screen uses quote context:
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/screen/AddMoneyScreen.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/viewmodel/AddMoneyViewModel.kt`

### Gaps vs VoltPay send flow
- No dedicated recipient domain/controller in Kotlin.
- No recipient method picker screen.
- No recipient review/submit step.
- "Send money" quick action currently routes to transactions:
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/screen/HomeScreen.kt`
  - `app/src/main/java/net/metalbrain/paysmart/ui/home/components/HomeContent.kt`
- Feature gate has `SEND_MONEY`, but no downstream send flow surface yet:
  - `app/src/main/java/net/metalbrain/paysmart/ui/featuregate/FeatureAccessPolicy.kt`

## Recommended Kotlin port slices

### SEND-001 Recipient domain + Room
- Add `ui/sendmoney/recipient` module:
  - `RecipientMethod` enum
  - `RecipientParams` sealed model (lookup/bank/document/email)
  - `RecipientPayload` mapper for API
- Add Room cache:
  - `recipient_draft` table + DAO
  - write-through repository with draft restore

### SEND-002 Recipient flow ViewModel + screens
- Add `SendMoneyViewModel` with step state:
  - `METHOD_PICKER`, `DETAILS`, `REVIEW`, `DONE`
- Add screens:
  - method picker
  - method-specific detail forms
  - review/confirm
- Route "Send money" button to this flow behind existing `FeatureGate`.

### SEND-003 Quote + recipient coupling
- Reuse existing `FxQuoteRepository` from Add Money.
- Add send-specific quote params state in `SendMoneyViewModel`.
- Persist quote snapshot in Room with recipient draft for resume continuity.

### SEND-004 API contracts (Functions)
- Add recipient endpoints in Functions API:
  - `POST /transfers/recipient/validate`
  - `POST /transfers/quote` (or reuse `/quotes` with send context)
  - `POST /transfers/create`
- Keep add-money/Stripe paths unchanged.

### SEND-005 Resume route and intent gating
- On feature click:
  - evaluate `FeatureKey.SEND_MONEY`
  - route to send money entry if allowed
  - preserve `resumeRoute` for interrupted KYC/profile completion.

## Immediate implementation order
1. `SEND-001` recipient domain + Room draft.
2. `SEND-002` method picker + details screen.
3. `SEND-003` wire quote provider into send flow.
4. `SEND-004` wire Functions endpoints and submit transfer.
