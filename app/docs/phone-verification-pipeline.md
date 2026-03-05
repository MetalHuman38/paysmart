# Phone Verification Pipeline (PNV-Ready)

_Last updated: 2026-03-02_

## Goal
Keep the existing SMS OTP flow stable while preparing a clean switch to Firebase Phone Number Verification (PNV) once Firebase marks it production-stable.

## Current Architecture
- `phone/core/PhoneAuthHandler.kt`
  - Existing Firebase SMS OTP implementation.
- `phone/data/PnvPreviewPhoneVerifier.kt`
  - Preview bridge for PNV route selection.
  - Currently delegates to legacy OTP path.
- `phone/data/PluggablePhoneVerifier.kt`
  - Runtime router that selects:
    - `LEGACY_OTP` (default)
    - `PNV_PREVIEW` (when feature flag enabled and runtime check passes)
- `di/AppModule.kt`
  - Wires `PhoneVerifier` as `PluggablePhoneVerifier`.

## Feature Flag
- `BuildConfig.PHONE_PNV_PREVIEW_ENABLED` in `app/build.gradle.kts`
  - Default: `false`
  - Safe default keeps all current behavior unchanged.

## Integration Contract (when PNV is stable)
1. Implement true PNV request/start logic in `PnvPreviewPhoneVerifier.start(...)`.
2. Preserve existing callback contract:
   - `onCodeSent` for OTP-required path.
   - `onAutoVerified` for silent/instant verified path.
   - `onError` for all terminal failures.
3. Keep `PluggablePhoneVerifier` unchanged so UI/ViewModels remain isolated from provider-specific details.

## Why this split
- No regression risk for existing SMS OTP onboarding.
- Single seam to swap to PNV without touching screen/viewmodel flows.
- Easy rollback by feature flag if preview/stable behavior changes.

## Current onboarding routing note
- OTP success now feeds capability/security preparation and password-readiness checks.
- Biometric setup completion no longer loops back to account-protection; it routes to:
  - `CreatePassword` when local password is not ready
  - `Home` when local password is already ready
