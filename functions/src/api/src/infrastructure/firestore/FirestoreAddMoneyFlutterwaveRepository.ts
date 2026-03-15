import { randomUUID } from "crypto";
import { FieldValue, Firestore } from "firebase-admin/firestore";
import {
  CreateFlutterwaveAddMoneySessionInput,
  FlutterwaveAddMoneySession,
  FlutterwaveAddMoneySessionStatus,
  FlutterwaveAddMoneySessionStatusResult,
  FlutterwaveWebhookApplyResult,
} from "../../domain/model/flutterwaveAddMoney.js";
import { FlutterwaveAddMoneyRepository } from "../../domain/repository/FlutterwaveAddMoneyRepository.js";
import {
  FlutterwavePaymentsService,
  FlutterwaveTransactionStatus,
} from "../../services/flutterwavePaymentsService.js";

type FlutterwaveAddMoneySessionDoc = {
  sessionId: string;
  uid: string;
  provider: "flutterwave";
  amountMinor: number;
  currency: string;
  status: FlutterwaveAddMoneySessionStatus;
  expiresAtMs: number;
  checkoutUrl?: string;
  flutterwaveStatus?: string;
  flutterwaveReference: string;
  flutterwaveVirtualAccountId?: string;
  flutterwaveChargeId?: string;
  flutterwaveCustomerId?: string;
  failureCode?: string;
  failureMessage?: string;
  lastWebhookEventId?: string;
  createdAt?: unknown;
  updatedAt?: unknown;
  walletAppliedAt?: unknown;
  settledAt?: unknown;
};

type FlutterwaveReferenceLookupDoc = {
  provider: "flutterwave";
  uid: string;
  sessionId: string;
  reference: string;
  createdAt?: unknown;
  updatedAt?: unknown;
};

const DEFAULT_SESSION_TTL_MS = 30 * 60 * 1000;
const SUPPORTED_WEBHOOK_EVENTS = new Set(["charge.completed", "charge.successful"]);

export class FirestoreAddMoneyFlutterwaveRepository
  implements FlutterwaveAddMoneyRepository
{
  constructor(
    private readonly firestore: Firestore,
    private readonly flutterwave: FlutterwavePaymentsService,
    private readonly flutterwavePublicKey: string,
    private readonly allowedCurrencies: Set<string>,
    private readonly minimumAmountMinor: number
  ) {}

  private sessionRef(uid: string, sessionId: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("payments")
      .doc("add_money_flutterwave")
      .collection("sessions")
      .doc(sessionId);
  }

  private lookupRef(reference: string) {
    return this.firestore
      .collection("paymentProviderLookups")
      .doc("flutterwave")
      .collection("references")
      .doc(reference);
  }

  private walletCurrentRef(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("wallet")
      .doc("current");
  }

  private walletTransactionRef(uid: string, transactionId: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("walletTransactions")
      .doc(transactionId);
  }

  private userRef(uid: string) {
    return this.firestore.collection("users").doc(uid);
  }

  async createSession(
    uid: string,
    input: CreateFlutterwaveAddMoneySessionInput
  ): Promise<FlutterwaveAddMoneySession> {
    const publicKey = this.flutterwavePublicKey.trim();
    if (!publicKey) {
      throw new Error("FLUTTERWAVE_PUBLIC_KEY is not configured");
    }

    const currency = this.normalizeCurrency(input.currency);
    const amountMinor = this.normalizeAmount(input.amountMinor);
    const customer = await this.resolveCustomerProfile(uid);
    const reference = buildReference();
    const providerSession = await this.flutterwave.createTopupSession({
      uid,
      amountMinor,
      currency,
      idempotencyKey: input.idempotencyKey,
      reference,
      customerId: customer.customerId,
      customer,
    });
    const status = this.deriveProviderStatus(providerSession.status);
    const expiresAtMs = providerSession.createdAtMs + DEFAULT_SESSION_TTL_MS;

    await this.sessionRef(uid, providerSession.sessionId).set({
      sessionId: providerSession.sessionId,
      uid,
      provider: "flutterwave",
      amountMinor,
      currency,
      status,
      expiresAtMs,
      checkoutUrl: providerSession.checkoutUrl ?? null,
      flutterwaveStatus: providerSession.status,
      flutterwaveReference: providerSession.txRef,
      flutterwaveVirtualAccountId: providerSession.sessionId,
      flutterwaveCustomerId: providerSession.customerId,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    });

    const lookupDoc: FlutterwaveReferenceLookupDoc = {
      provider: "flutterwave",
      uid,
      sessionId: providerSession.sessionId,
      reference: providerSession.txRef,
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
    };
    await this.lookupRef(providerSession.txRef).set(lookupDoc, { merge: true });
    await this.persistFlutterwaveCustomerProfile(
      uid,
      providerSession.customerId,
      customer.email
    );

    if (status === "succeeded") {
      await this.applySettlementIfNeeded(
        uid,
        providerSession.sessionId,
        amountMinor,
        currency,
        providerSession.sessionId
      );
    }

    return {
      sessionId: providerSession.sessionId,
      provider: "flutterwave",
      amountMinor,
      currency,
      status,
      expiresAtMs,
      checkoutUrl: providerSession.checkoutUrl,
      flutterwaveTransactionId: providerSession.sessionId,
      publicKey,
    };
  }

  async getSessionStatus(
    uid: string,
    sessionId: string
  ): Promise<FlutterwaveAddMoneySessionStatusResult> {
    const cleanSessionId = sessionId.trim();
    if (!cleanSessionId) {
      throw new Error("Missing sessionId");
    }

    const ref = this.sessionRef(uid, cleanSessionId);
    const snap = await ref.get();
    if (!snap.exists) {
      throw new Error("Flutterwave add money session not found");
    }

    const current = snap.data() as FlutterwaveAddMoneySessionDoc;
    if (current.uid !== uid) {
      throw new Error("Flutterwave add money session not found");
    }

    let charge: FlutterwaveTransactionStatus | null = null;
    if (
      current.status !== "succeeded" &&
      current.status !== "failed" &&
      current.status !== "expired"
    ) {
      charge = await this.flutterwave.retrieveLatestVirtualAccountCharge(
        current.flutterwaveVirtualAccountId || current.sessionId,
        current.flutterwaveReference
      );
    }

    let status = current.status;
    let failureCode = current.failureCode;
    let failureMessage = current.failureMessage;
    let chargeId = current.flutterwaveChargeId;

    if (charge) {
      chargeId = charge.transactionId;
      const validated = this.validateChargeAgainstSession(current, charge);
      status = validated.status;
      failureCode = validated.failureCode;
      failureMessage = validated.failureMessage;
    } else if (Date.now() >= current.expiresAtMs && status !== "succeeded") {
      status = "expired";
    }

    await ref.set(
      {
        status,
        flutterwaveStatus: charge?.status ?? current.flutterwaveStatus ?? null,
        flutterwaveChargeId: chargeId ?? null,
        failureCode: failureCode ?? null,
        failureMessage: failureMessage ?? null,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    if (status === "succeeded") {
      await this.applySettlementIfNeeded(
        uid,
        cleanSessionId,
        current.amountMinor,
        current.currency,
        chargeId
      );
    }

    return {
      sessionId: cleanSessionId,
      provider: "flutterwave",
      amountMinor: current.amountMinor,
      currency: current.currency,
      status,
      expiresAtMs: current.expiresAtMs,
      checkoutUrl: current.checkoutUrl,
      flutterwaveTransactionId: chargeId ?? current.flutterwaveVirtualAccountId,
      failureCode,
      failureMessage,
      updatedAtMs: Date.now(),
    };
  }

  async applyWebhook(
    rawPayload: string,
    signatureHeader?: string,
    signatureName?: string
  ): Promise<FlutterwaveWebhookApplyResult> {
    const event = this.flutterwave.parseWebhookEvent(
      rawPayload,
      signatureHeader,
      signatureName
    );

    if (!SUPPORTED_WEBHOOK_EVENTS.has(event.type.trim().toLowerCase())) {
      return {
        handled: false,
        eventType: event.type,
      };
    }

    const data = event.dataObject;
    const reference = asString(data.reference) || asString(data.tx_ref);
    if (!reference) {
      throw new Error("Flutterwave webhook payload is missing reference");
    }

    const lookupSnap = await this.lookupRef(reference).get();
    if (!lookupSnap.exists) {
      return {
        handled: false,
        eventType: event.type,
      };
    }

    const lookup = lookupSnap.data() as FlutterwaveReferenceLookupDoc;
    const sessionRef = this.sessionRef(lookup.uid, lookup.sessionId);
    const sessionSnap = await sessionRef.get();
    if (!sessionSnap.exists) {
      return {
        handled: false,
        eventType: event.type,
      };
    }

    const current = sessionSnap.data() as FlutterwaveAddMoneySessionDoc;
    const chargeId =
      asString(data.id) || asString(data.flw_ref) || current.flutterwaveChargeId || "";
    const charge = await this.resolveCharge(current, chargeId);
    const validated = charge && this.validateChargeAgainstSession(current, charge);
    const status = validated?.status ?? this.deriveProviderStatus(asString(data.status));
    const failureCode = validated?.failureCode ?? current.failureCode ?? null;
    const failureMessage = validated?.failureMessage ?? current.failureMessage ?? null;

    await sessionRef.set(
      {
        status,
        flutterwaveStatus: charge?.status ?? (asString(data.status) || null),
        flutterwaveChargeId: charge?.transactionId ?? (chargeId || null),
        failureCode,
        failureMessage,
        lastWebhookEventId: event.id,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    if (status === "succeeded") {
      await this.applySettlementIfNeeded(
        lookup.uid,
        lookup.sessionId,
        current.amountMinor,
        current.currency,
        charge?.transactionId || chargeId || undefined
      );
    }

    return {
      handled: true,
      sessionId: lookup.sessionId,
      uid: lookup.uid,
      status,
      eventType: event.type,
    };
  }

  private async resolveCharge(
    session: FlutterwaveAddMoneySessionDoc,
    chargeId: string
  ): Promise<FlutterwaveTransactionStatus | null> {
    if (chargeId.trim()) {
      return this.flutterwave.retrieveTransactionStatus(chargeId);
    }
    return this.flutterwave.retrieveLatestVirtualAccountCharge(
      session.flutterwaveVirtualAccountId || session.sessionId,
      session.flutterwaveReference
    );
  }

  private validateChargeAgainstSession(
    session: FlutterwaveAddMoneySessionDoc,
    charge: FlutterwaveTransactionStatus
  ): {
    status: FlutterwaveAddMoneySessionStatus;
    failureCode?: string;
    failureMessage?: string;
  } {
    if (charge.currency !== session.currency) {
      return {
        status: "failed",
        failureCode: "currency_mismatch",
        failureMessage: "Charge currency does not match session currency",
      };
    }
    if (charge.amountMinor !== session.amountMinor) {
      return {
        status: "failed",
        failureCode: "amount_mismatch",
        failureMessage: "Charge amount does not match session amount",
      };
    }
    return {
      status: this.deriveProviderStatus(charge.status),
    };
  }

  private async applySettlementIfNeeded(
    uid: string,
    sessionId: string,
    amountMinor: number,
    currency: string,
    flutterwaveTransactionId?: string
  ): Promise<boolean> {
    const sessionRef = this.sessionRef(uid, sessionId);
    const walletRef = this.walletCurrentRef(uid);
    const userRef = this.userRef(uid);
    const walletTxRef = this.walletTransactionRef(uid, sessionId);
    const amountMajor = roundMoney(amountMinor / 100);

    return this.firestore.runTransaction(async (tx) => {
      const sessionSnap = await tx.get(sessionRef);
      if (!sessionSnap.exists) {
        throw new Error("Flutterwave add money session not found");
      }

      const sessionData = sessionSnap.data() as FlutterwaveAddMoneySessionDoc;
      if (sessionData.uid !== uid) {
        throw new Error("Flutterwave add money session ownership mismatch");
      }

      if (sessionData.walletAppliedAt) {
        return false;
      }

      const walletSnap = await tx.get(walletRef);
      const walletData = walletSnap.data() as Record<string, unknown> | undefined;
      const existingBalances = parseBalances(walletData?.balancesByCurrency);
      const current = existingBalances[currency] ?? 0;
      const nextBalance = roundMoney(current + amountMajor);
      const balancesByCurrency = {
        ...existingBalances,
        [currency]: nextBalance,
      };

      tx.set(
        walletRef,
        {
          uid,
          balancesByCurrency,
          updatedAt: FieldValue.serverTimestamp(),
          lastAddMoneySessionId: sessionId,
        },
        { merge: true }
      );

      tx.set(
        userRef,
        {
          balancesByCurrency,
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      tx.set(
        walletTxRef,
        {
          id: sessionId,
          uid,
          type: "top_up",
          provider: "flutterwave",
          amountMinor,
          amountMajor,
          currency,
          status: "succeeded",
          flutterwaveSessionId: sessionId,
          flutterwaveTransactionId: flutterwaveTransactionId ?? null,
          createdAt: FieldValue.serverTimestamp(),
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      tx.set(
        sessionRef,
        {
          status: "succeeded",
          flutterwaveChargeId: flutterwaveTransactionId ?? null,
          walletAppliedAt: FieldValue.serverTimestamp(),
          settledAt: FieldValue.serverTimestamp(),
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      return true;
    });
  }

  private async resolveCustomerProfile(uid: string): Promise<{
    email: string;
    firstName: string;
    lastName: string;
    customerId?: string;
  }> {
    const userSnap = await this.userRef(uid).get();
    const user = userSnap.data() as Record<string, unknown> | undefined;
    const displayName = asString(user?.displayName);
    const nameParts = displayName.split(/\s+/).filter(Boolean);
    const firstName = nameParts[0] || "PaySmart";
    const lastName = nameParts.slice(1).join(" ") || "User";

    const emailFromUser = asString(user?.email);
    const email = emailFromUser || `${uid}@users.pay-smart.net`;
    const paymentProviders = asRecord(user?.paymentProviders);
    const flutterwaveProvider = asRecord(paymentProviders.flutterwave);
    const customerId = asString(flutterwaveProvider.customerId);
    return {
      email,
      firstName,
      lastName,
      customerId: customerId || undefined,
    };
  }

  private async persistFlutterwaveCustomerProfile(
    uid: string,
    customerId: string,
    email: string
  ): Promise<void> {
    const cleanCustomerId = customerId.trim();
    if (!cleanCustomerId) {
      return;
    }

    await this.userRef(uid).set(
      {
        paymentProviders: {
          flutterwave: {
            customerId: cleanCustomerId,
            email: email.trim().toLowerCase(),
            updatedAt: FieldValue.serverTimestamp(),
          },
        },
      },
      { merge: true }
    );
  }

  private deriveProviderStatus(rawStatus: string): FlutterwaveAddMoneySessionStatus {
    const status = rawStatus.trim().toLowerCase();
    if (
      status === "successful" ||
      status === "success" ||
      status === "completed" ||
      status === "paid"
    ) {
      return "succeeded";
    }
    if (status === "failed" || status === "cancelled" || status === "canceled") {
      return "failed";
    }
    if (status === "expired") {
      return "expired";
    }
    if (status === "pending" || status === "queued" || status === "processing") {
      return "pending";
    }
    return "created";
  }

  private normalizeCurrency(raw: string): string {
    const currency = raw.trim().toUpperCase();
    if (!currency || currency.length !== 3) {
      throw new Error("Invalid currency");
    }
    if (!this.allowedCurrencies.has(currency)) {
      throw new Error("Unsupported currency");
    }
    return currency;
  }

  private normalizeAmount(raw: number): number {
    if (!Number.isFinite(raw) || !Number.isInteger(raw)) {
      throw new Error("Invalid amountMinor");
    }
    if (raw < this.minimumAmountMinor) {
      throw new Error(`Amount must be at least ${this.minimumAmountMinor}`);
    }
    if (raw > 10_000_000) {
      throw new Error("Amount exceeds max");
    }
    return raw;
  }
}

function buildReference(): string {
  return randomUUID();
}

function roundMoney(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

function parseBalances(raw: unknown): Record<string, number> {
  if (!raw || typeof raw !== "object") {
    return {};
  }

  const source = raw as Record<string, unknown>;
  const out: Record<string, number> = {};
  for (const [key, value] of Object.entries(source)) {
    if (!key) continue;

    const parsed = parseNumeric(value);
    if (parsed === null) continue;

    out[key.toUpperCase()] = roundMoney(parsed);
  }
  return out;
}

function parseNumeric(raw: unknown): number | null {
  if (typeof raw === "number" && Number.isFinite(raw)) {
    return raw;
  }
  if (typeof raw === "string") {
    const parsed = Number(raw);
    if (Number.isFinite(parsed)) {
      return parsed;
    }
  }
  return null;
}

function asString(raw: unknown): string {
  return typeof raw === "string" ? raw.trim() : "";
}

function asRecord(raw: unknown): Record<string, unknown> {
  return raw && typeof raw === "object" ? (raw as Record<string, unknown>) : {};
}
