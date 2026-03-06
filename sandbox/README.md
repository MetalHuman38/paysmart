# PaySmart Java Sandbox

This module isolates Java-only test-mode payment experiments from the Android app runtime.

## Why this exists

- Keeps server-side Stripe secret usage out of `:app`.
- Lets you run investor/demo payment setup flows in test mode.
- Uses a small constructor-based DI container (`SandboxContainer`) and repository pattern.

## Run

Preferred approach (project-local, no system-wide env changes):

1. Copy `sandbox/local.env.properties.example` to `sandbox/local.env.properties`
2. Fill values in that file (it is git-ignored)

Then run:

```powershell
.\gradlew :sandbox:run
```

The command prints created Stripe `productId` and `priceId`.

Alternative (one-off, no file):

```powershell
.\gradlew :sandbox:run -Psandbox.stripeSecretKey=sk_test_xxx -Psandbox.priceCurrency=gbp -Psandbox.priceMinor=1200
```
