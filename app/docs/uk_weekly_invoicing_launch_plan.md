# UK Weekly Invoicing Launch Plan

## Product Wedge
PaySmart will launch as a `UK invoice-led product` for:

- shift workers
- freelancers
- hospitality staff
- care and temp workers
- solo contractors
- small agencies that invoice weekly

The first launch is not a broad wallet or remittance product. The core promise is:

> Create weekly invoices fast, share them cleanly, and get paid with less admin friction.

`Top up` remains in the product, but it is positioned as a supporting utility rather than the primary story.

## Launch Positioning

### Primary story
- Build and finalize weekly invoices
- Reuse saved worker and venue details
- Download or share a polished PDF

### Secondary story
- Add money to your PaySmart balance
- Use wallet features after the required checks are complete

## Personas

### Core launch user
- UK workers invoicing weekly for shifts or contract work
- Often repeating the same employer, venue, or agency
- Needs fast invoice generation more than a full wallet feature set

### Expansion user
- small agencies or professionals with optional business identity fields
- UK first, then selected Europe with `EUR`

## Day 30 Scope

### Goal
Get a real UK invoice product into users' hands with minimal backend risk.

### User journey
1. Create account
2. Verify OTP
3. Choose what to do first
4. Complete minimum security
5. Land in an invoice-led experience
6. Save worker details
7. Save venue details
8. Enter weekly shifts
9. Finalize invoice
10. Download or share a branded PDF

### App route map
- `Screen.CreateAccount`
- `Screen.OtpVerification`
- `Screen.PostOtpCapabilities`
- `Screen.PostOtpSecuritySteps`
- `Screen.PostOtpClientInformation`
- `Screen.ProtectAccount`
- `Screen.CreatePassword`
- `Screen.Home` or `Screen.InvoiceFlow`
- `Screen.InvoiceWorkerProfile`
- `Screen.InvoiceVenueSetup`
- `Screen.InvoiceWeeklyEntry`
- `Screen.InvoiceDetail`

### Day 30 implementation rules
- Reuse `Screen.PostOtpCapabilities` as the first-run intent selector
- Store `launchInterest` on the user profile as `invoice` or `top_up`
- Default UK users to `invoice`
- Keep `CREATE_INVOICE` behind security strength rules
- Keep `ADD_MONEY` behind verified email and address rules
- Make `Home` reorder its primary actions based on `launchInterest`
- Upgrade the invoice PDF to `pay-smart-invoice-v2`

### Files mapped to the launch wedge

#### Onboarding and routing
- `app/src/main/java/net/metalbrain/paysmart/ui/NavGraph.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/viewmodel/PostOtpCapabilitiesViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/screen/PostOtpCapabilitiesScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/account/creation/components/PostOtpCapabilitiesContent.kt`

#### User model and persistence
- `app/src/main/java/net/metalbrain/paysmart/domain/model/LaunchInterest.kt`
- `app/src/main/java/net/metalbrain/paysmart/domain/model/AuthUserModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/data/repository/FirestoreUserProfileRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/data/repository/UserProfileCacheRepository.kt`
- `app/src/main/java/net/metalbrain/paysmart/room/entity/UserProfileCacheEntity.kt`
- `app/src/main/java/net/metalbrain/paysmart/room/DbMigrations.kt`

#### Home personalization
- `app/src/main/java/net/metalbrain/paysmart/ui/home/viewmodel/HomeViewModel.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/home/state/HomeUiState.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/home/components/HomeContent.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/home/components/HomeRecentTransactionsSection.kt`
- `app/src/main/java/net/metalbrain/paysmart/ui/home/components/EmptyTransactionsBlock.kt`

#### Invoice flow polish
- `app/src/main/java/net/metalbrain/paysmart/core/features/invoicing/screen/InvoiceWorkerProfileScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/invoicing/screen/InvoiceVenueSetupScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/invoicing/screen/InvoiceWeeklyEntryScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/invoicing/screen/InvoiceDetailScreen.kt`
- `app/src/main/java/net/metalbrain/paysmart/core/features/invoicing/screen/InvoiceSurfaceCards.kt`
- `app/src/main/res/values/strings_invoicing.xml`

#### PDF generation
- `functions/src/api/src/services/invoicePdfRenderer.ts`
- `functions/src/api/src/infrastructure/firestore/FirestoreInvoiceRepository.ts`
- `functions/src/api/src/workers/processInvoicePdfGeneration.ts`

## Day 60 Scope

### Goal
Move from invoice creation to invoice collection and payment visibility.

### Planned additions
- Stripe-hosted invoice collection
- invoice send/share action from detail screen
- PaySmart-side payment lifecycle:
  - `drafted`
  - `finalized`
  - `sent`
  - `viewed`
  - `paid`
  - `failed`
- invoice notifications in the notification center
- invoice email delivery with clean operational copy

### Likely route impacts
- `Screen.InvoiceDetail` gets send and payment status surfaces
- invoice history shows payment progress
- notification center surfaces invoice events

## Day 90 Scope

### Goal
Expand from UK-only invoicing into UK + selected Europe without turning PaySmart into a generic fintech app.

### Planned additions
- `EUR` support alongside `GBP`
- Stripe-supported UK/EU collection methods
- optional `tradingName` and `businessName`
- segmentation-aware home and lifecycle messaging
- light analytics for:
  - sign-up to first invoice
  - invoice creation to send
  - invoice send to paid

## UX Principles

### Onboarding
- Ask one simple intent question after OTP
- Keep the rest of onboarding adaptive
- Do not over-expose features on day one

### Home
- Invoice-first users should see invoice creation as the main action
- Top-up-first users should still see funding as primary
- Secondary services remain available without dominating the first screen

### Invoice UI
- Use PaySmart spacing and card patterns
- Keep typography clear and operational
- Make the flow feel like one guided weekly workflow

### PDF
- Use a cleaner header/footer hierarchy
- Keep copy client-facing, not marketing-heavy
- Embed PaySmart and VoltService Ltd branding consistently

## Success Criteria

### Day 30
- A UK user can sign up and choose `Create invoice`
- The user can complete minimum security
- The user can create a weekly invoice end to end
- The PDF is polished enough to share with a client, venue, or agency
- Returning home shows an invoice-led experience

### Day 60
- A UK user can create, send, and track invoices
- Payment collection is visible and understandable inside PaySmart

### Day 90
- PaySmart supports a coherent UK/EU invoice-led experience with room for agency growth

## Non-goals for launch
- Nigeria-first release
- remittance-first positioning
- consumer wallet as the main story
- heavy identity gating for invoice-first users unless compliance later requires it

## Current decision
The product should be repurposed around weekly invoicing first. Existing wallet and top-up infrastructure remains valuable, but it supports the wedge rather than defining it.
