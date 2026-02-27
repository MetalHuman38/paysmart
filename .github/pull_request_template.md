## Summary

- What problem does this PR solve?
- Why is this approach chosen?

## Scope

- [ ] Android app
- [ ] Functions API
- [ ] Public site/docs
- [ ] CI/CD

Affected paths:

-

## Related Ticket / Issue

- Ticket ID(s): <!-- e.g. SEND-003, SEC-001 -->
- Issue link(s):

## Behavior Change

- Expected behavior after this PR:
- Any user-visible changes:

## Risk and Rollback

- Key risks:
- Rollback plan:

## Test Evidence

Commands run and results:

```bash
# Example
./gradlew :app:compileDebugKotlin --no-configuration-cache
cd functions && npm run build && npm run test
```

- [ ] Tests added or updated for changed behavior
- [ ] No known regressions in related flows

## Screenshots / Video (if UI change)

<!-- Attach before/after captures -->

## Docs and Contracts

- [ ] README/CONTRIBUTING/docs updated where needed
- [ ] API contract changes documented (if applicable)

## Security Checklist

- [ ] No secrets, keys, or sensitive data committed
- [ ] Security-sensitive changes reviewed (auth/session/identity/payment)

## Reviewer Notes

Any areas where reviewer focus is requested:

-
