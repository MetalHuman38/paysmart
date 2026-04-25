# Feature Account Lint Fix, Build Scan, And Release Gate

## Context

`./gradlew build --scan` failed in `:feature:account:lintDebug`.

The build stopped on one lint error:

- `feature/account/src/main/res/values/strings.xml`
- `StringEscaping`
- `change_passcode_biometric_title` used `&#39;` for an apostrophe

The same lint run also reported one Compose warning:

- `feature/account/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeyGlowSwitch.kt`
- `UseOfNonLambdaOffsetOverload`
- animated state was passed to `Modifier.offset(x = thumbOffset)`

## Fix

1. Escape the apostrophe in `change_passcode_biometric_title` with Android string resource syntax:

```xml
<string name="change_passcode_biometric_title">Confirm it\'s you</string>
```

1. Use the lambda overload for the animated switch thumb offset:

```kotlin
.offset { IntOffset(x = thumbOffset.roundToPx(), y = 0) }
```

## Validation Order

Run the smallest failing gate first:

```powershell
./gradlew :feature:account:lintDebug
```

Then run the affected module compile gate:

```powershell
./gradlew :feature:account:compileDebugKotlin
```

Then run the full build with scan:

```powershell
./gradlew build --scan
```

If the scan publish times out after the build completes, keep the local build result as the release gate and retry only scan publishing when network is stable.

## Commit Scope

Commit only the files that belong to this fix:

- `feature/account/src/main/res/values/strings.xml`
- `feature/account/src/main/java/net/metalbrain/paysmart/core/features/account/passkey/components/PasskeyGlowSwitch.kt`
- `app/docs/feature_account_lint_build_scan_release_fix.md`

Do not include unrelated migration work, IDE metadata, function lockfiles, or sandbox deletions in this commit.

## Release Gate

After the lint and full build pass, build the release artifact:

```powershell
./gradlew :app:assembleRelease
```

The release gate is complete when:

- `:feature:account:lintDebug` passes
- `./gradlew build --scan` completes the build
- build scan publish either succeeds or fails only after the build has already completed
- `:app:assembleRelease` succeeds
- the git commit contains only the fix scope listed above
