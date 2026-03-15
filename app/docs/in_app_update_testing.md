# PaySmart In-App Update Testing

This implementation supports two testing paths:

1. Internal App Sharing
2. JVM unit tests with the fake update source

## Internal App Sharing

Use Google Play Internal App Sharing for end-to-end verification of Play Core behavior:

- upload a release-signed build with the same application ID
- install the shared build from the Play-provided Internal App Sharing link
- upload a newer build with a higher `versionCode`
- open the installed app from Play and verify:
  - `FLEXIBLE` starts only on safe routes
  - `IMMEDIATE` resumes after background/foreground when Play reports `DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS`
  - downloaded flexible updates surface the restart prompt and call `completeUpdate()`

Recommended manual checks:

- safe route such as `home`: update prompt may appear
- blocked route such as OTP, passcode, add-money, KYC, transactions, or invoice flow: prompt must not appear
- dismiss an immediate prompt once: app should respect the cooldown and avoid an immediate duplicate prompt

## Unit Tests

The update layer includes a fake source for JVM tests:

- `app/src/test/java/net/metalbrain/paysmart/core/service/update/FakeUpdateDataSource.kt`
- `app/src/test/java/net/metalbrain/paysmart/core/service/update/UpdatePolicyEngineTest.kt`
- `app/src/test/java/net/metalbrain/paysmart/core/service/update/UpdateCoordinatorTest.kt`

These tests cover:

- policy routing for `FLEXIBLE` and `IMMEDIATE`
- critical-flow gating
- immediate resume behavior
- duplicate prompt suppression
- listener registration and flexible downloaded restart flow
