# Identity Upload Pipeline (Client Encrypt -> Upload -> Attested Decrypt)

_Last updated: 2026-03-02_

## Goal
Upload identity documents without exposing plaintext to transport/storage layers, while ensuring the server only decrypts payloads tied to a valid device attestation.

## End-to-End Flow
1. Client computes `sha256(plaintext)` and requests an upload session:
   - `POST /auth/identity/upload/session`
2. Server responds with:
   - `sessionId`
   - signed `uploadUrl`
   - `objectPath`
   - `associatedData` (AAD bound to session/user/doc type)
   - `attestationNonce` (nonce to bind device attestation)
3. Client encrypts document bytes locally using AEAD with AAD.
4. Client uploads encrypted bytes to `uploadUrl`.
5. Client obtains device attestation JWT bound to `attestationNonce`.
6. Client commits:
   - `POST /auth/identity/upload/commit` with `sessionId`, `payloadSha256`, `attestationJwt`.
7. Server verifies:
   - attestation validity
   - nonce/session binding
   - payload hash/object metadata checks
8. Server performs decrypt in trusted environment and marks verification state.

## Security Properties
- Payload is encrypted before upload.
- AAD binds ciphertext to session/user context, preventing ciphertext replay across sessions.
- Attestation nonce binds upload commit to a trusted device action.
- Hash checks detect payload substitution/tampering.

## Implemented Endpoints
- `POST /auth/identity/upload/session`
- `POST /auth/identity/upload/commit`

Current behavior:
- Session endpoint creates a short-lived upload session and returns a signed upload URL.
- Commit endpoint validates session/hash/object upload presence, stores attestation digest, and marks identity status as `pending_review`.
- Full attestation cryptographic verification and trusted decrypt worker are still pending.

## Kotlin Scaffold Added
- `ui/profile/identity/IdentityUploadContract.kt`
- `ui/profile/identity/IdentityUploadCipher.kt`
- `ui/profile/identity/IdentityUploadOrchestrator.kt`
- `ui/profile/identity/RemoteIdentityUploadRepository.kt`
- `ui/profile/viewmodel/IdentitySetupResolverViewModel.kt`

## Observability
- Firebase Performance traces are attached to identity upload critical calls:
  - `identity_create_upload_session`
  - `identity_upload_encrypted_payload`
  - `identity_commit_upload`
- Collection is disabled in local/debug and enabled in release builds for controlled cost.
