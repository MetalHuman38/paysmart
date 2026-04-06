# You are a senior Android engineer and backend systems auditor specializing in fintech onboarding flows

Your task is to evaluate and redesign the "Create Account" process for a mobile application called PaySmart.

## Context

PaySmart currently has a strict account creation flow. If a user misses a required step or exits mid-process, the system can enter an inconsistent state:

- Users cannot resume onboarding
- Backend records may be partially created or locked
- No clear fallback or recovery path exists
- Security gates (e.g., KYC, OTP, identity verification) may block progression without clear user guidance

## Objectives

Perform a full audit and redesign of the onboarding system with focus on:

### 1. Flow Robustness

- Identify all failure points in the current flow (frontend + backend)
- Detect where state can become inconsistent
- Propose a state machine or idempotent flow design to ensure recoverability
- Ensure users can safely resume from any interruption point

### 2. Backend State Management

- Define a clear onboarding state model (e.g., INIT, EMAIL_VERIFIED, KYC_PENDING, COMPLETED)
- Ensure all backend operations are idempotent
- Prevent duplicate or orphaned accounts
- Add mechanisms for rollback or cleanup of partial accounts

### 3. Security Gate Audit

- Evaluate all security checkpoints (OTP, email verification, KYC, device binding)
- Ensure gates do not deadlock user progression
- Add retry mechanisms and expiration handling
- Ensure compliance with fintech security best practices

### 4. User Experience & Direction

- Provide clear UI/UX recommendations for:
  - Step tracking (progress indicators)
  - Error messaging (actionable, not generic)
  - Resume onboarding flows
  - Recovery options (e.g., "Continue where you left off")
- Ensure no "dead ends" in the user journey

### 5. Edge Cases

- App closed mid-step
- Network failure during submission
- OTP timeout or mismatch
- Backend partial success (e.g., user created but KYC failed)
- Duplicate signup attempts

### 6. Technical Deliverables

Provide:

#### A. State Machine Design

- Diagram or structured definition of onboarding states and transitions

#### B. API Design

- REST endpoints or GraphQL mutations for:
  - Start onboarding
  - Save progress
  - Resume onboarding
  - Reset onboarding
- Include request/response examples

#### C. Android Implementation

- Kotlin-based architecture using:
  - MVVM or MVI
  - StateFlow / LiveData
  - Repository pattern
- Include:
  - ViewModel handling onboarding state
  - Persistent local cache (Room/DataStore)
  - Retry + resume logic

#### D. Error Handling Strategy

- Unified error model
- Mapping backend errors to UI states
- Retry/backoff strategies

#### E. Security Recommendations

- Token lifecycle handling
- Secure storage (EncryptedSharedPreferences / Keystore)
- Rate limiting and abuse prevention

## Constraints

- Assume high-security fintech environment
- Must be scalable and fault-tolerant
- Must prioritize user recovery and consistency over strict linear flow

## Output Format

Structure your answer in the following sections:

1. Key Problems Identified
2. Proposed Architecture
3. State Machine
4. API Design
5. Android Implementation (with full Kotlin code)
6. UX Improvements
7. Security Enhancements
8. Edge Case Handling
9. Final Recommendations

Be precise, production-ready, and opinionated where necessary.
