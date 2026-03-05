import { randomUUID } from "crypto";
import { FieldValue } from "firebase-admin/firestore";
const DEFAULT_SESSION_TTL_MS = 30 * 60 * 1000;
const SUPPORTED_WEBHOOK_EVENTS = new Set(["charge.completed", "charge.successful"]);
export class FirestoreAddMoneyFlutterwaveRepository {
    firestore;
    flutterwave;
    flutterwavePublicKey;
    allowedCurrencies;
    minimumAmountMinor;
    constructor(firestore, flutterwave, flutterwavePublicKey, allowedCurrencies, minimumAmountMinor) {
        this.firestore = firestore;
        this.flutterwave = flutterwave;
        this.flutterwavePublicKey = flutterwavePublicKey;
        this.allowedCurrencies = allowedCurrencies;
        this.minimumAmountMinor = minimumAmountMinor;
    }
    sessionRef(uid, sessionId) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("payments")
            .doc("add_money_flutterwave")
            .collection("sessions")
            .doc(sessionId);
    }
    lookupRef(reference) {
        return this.firestore
            .collection("paymentProviderLookups")
            .doc("flutterwave")
            .collection("references")
            .doc(reference);
    }
    walletCurrentRef(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("wallet")
            .doc("current");
    }
    walletTransactionRef(uid, transactionId) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("walletTransactions")
            .doc(transactionId);
    }
    userRef(uid) {
        return this.firestore.collection("users").doc(uid);
    }
    async createSession(uid, input) {
        const publicKey = this.flutterwavePublicKey.trim();
        if (!publicKey) {
            throw new Error("FLUTTERWAVE_PUBLIC_KEY is not configured");
        }
        const currency = this.normalizeCurrency(input.currency);
        const amountMinor = this.normalizeAmount(input.amountMinor);
        const customer = await this.resolveCustomerProfile(uid);
        const reference = buildReference(uid);
        const providerSession = await this.flutterwave.createTopupSession({
            uid,
            amountMinor,
            currency,
            idempotencyKey: input.idempotencyKey,
            reference,
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
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
        const lookupDoc = {
            provider: "flutterwave",
            uid,
            sessionId: providerSession.sessionId,
            reference: providerSession.txRef,
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        };
        await this.lookupRef(providerSession.txRef).set(lookupDoc, { merge: true });
        if (status === "succeeded") {
            await this.applySettlementIfNeeded(uid, providerSession.sessionId, amountMinor, currency, providerSession.sessionId);
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
    async getSessionStatus(uid, sessionId) {
        const cleanSessionId = sessionId.trim();
        if (!cleanSessionId) {
            throw new Error("Missing sessionId");
        }
        const ref = this.sessionRef(uid, cleanSessionId);
        const snap = await ref.get();
        if (!snap.exists) {
            throw new Error("Flutterwave add money session not found");
        }
        const current = snap.data();
        if (current.uid !== uid) {
            throw new Error("Flutterwave add money session not found");
        }
        let charge = null;
        if (current.status !== "succeeded" &&
            current.status !== "failed" &&
            current.status !== "expired") {
            charge = await this.flutterwave.retrieveLatestVirtualAccountCharge(current.flutterwaveVirtualAccountId || current.sessionId, current.flutterwaveReference);
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
        }
        else if (Date.now() >= current.expiresAtMs && status !== "succeeded") {
            status = "expired";
        }
        await ref.set({
            status,
            flutterwaveStatus: charge?.status ?? current.flutterwaveStatus ?? null,
            flutterwaveChargeId: chargeId ?? null,
            failureCode: failureCode ?? null,
            failureMessage: failureMessage ?? null,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        if (status === "succeeded") {
            await this.applySettlementIfNeeded(uid, cleanSessionId, current.amountMinor, current.currency, chargeId);
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
    async applyWebhook(rawPayload, signatureHeader, signatureName) {
        const event = this.flutterwave.parseWebhookEvent(rawPayload, signatureHeader, signatureName);
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
        const lookup = lookupSnap.data();
        const sessionRef = this.sessionRef(lookup.uid, lookup.sessionId);
        const sessionSnap = await sessionRef.get();
        if (!sessionSnap.exists) {
            return {
                handled: false,
                eventType: event.type,
            };
        }
        const current = sessionSnap.data();
        const chargeId = asString(data.id) || asString(data.flw_ref) || current.flutterwaveChargeId || "";
        const charge = await this.resolveCharge(current, chargeId);
        const validated = charge && this.validateChargeAgainstSession(current, charge);
        const status = validated?.status ?? this.deriveProviderStatus(asString(data.status));
        const failureCode = validated?.failureCode ?? current.failureCode ?? null;
        const failureMessage = validated?.failureMessage ?? current.failureMessage ?? null;
        await sessionRef.set({
            status,
            flutterwaveStatus: charge?.status ?? (asString(data.status) || null),
            flutterwaveChargeId: charge?.transactionId ?? (chargeId || null),
            failureCode,
            failureMessage,
            lastWebhookEventId: event.id,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        if (status === "succeeded") {
            await this.applySettlementIfNeeded(lookup.uid, lookup.sessionId, current.amountMinor, current.currency, charge?.transactionId || chargeId || undefined);
        }
        return {
            handled: true,
            sessionId: lookup.sessionId,
            uid: lookup.uid,
            status,
            eventType: event.type,
        };
    }
    async resolveCharge(session, chargeId) {
        if (chargeId.trim()) {
            return this.flutterwave.retrieveTransactionStatus(chargeId);
        }
        return this.flutterwave.retrieveLatestVirtualAccountCharge(session.flutterwaveVirtualAccountId || session.sessionId, session.flutterwaveReference);
    }
    validateChargeAgainstSession(session, charge) {
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
    async applySettlementIfNeeded(uid, sessionId, amountMinor, currency, flutterwaveTransactionId) {
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
            const sessionData = sessionSnap.data();
            if (sessionData.uid !== uid) {
                throw new Error("Flutterwave add money session ownership mismatch");
            }
            if (sessionData.walletAppliedAt) {
                return false;
            }
            const walletSnap = await tx.get(walletRef);
            const walletData = walletSnap.data();
            const existingBalances = parseBalances(walletData?.balancesByCurrency);
            const current = existingBalances[currency] ?? 0;
            const nextBalance = roundMoney(current + amountMajor);
            const balancesByCurrency = {
                ...existingBalances,
                [currency]: nextBalance,
            };
            tx.set(walletRef, {
                uid,
                balancesByCurrency,
                updatedAt: FieldValue.serverTimestamp(),
                lastAddMoneySessionId: sessionId,
            }, { merge: true });
            tx.set(userRef, {
                balancesByCurrency,
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
            tx.set(walletTxRef, {
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
            }, { merge: true });
            tx.set(sessionRef, {
                status: "succeeded",
                flutterwaveChargeId: flutterwaveTransactionId ?? null,
                walletAppliedAt: FieldValue.serverTimestamp(),
                settledAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
            return true;
        });
    }
    async resolveCustomerProfile(uid) {
        const userSnap = await this.userRef(uid).get();
        const user = userSnap.data();
        const displayName = asString(user?.displayName);
        const nameParts = displayName.split(/\s+/).filter(Boolean);
        const firstName = nameParts[0] || "PaySmart";
        const lastName = nameParts.slice(1).join(" ") || "User";
        const emailFromUser = asString(user?.email);
        const email = emailFromUser || `${uid}@users.pay-smart.net`;
        return {
            email,
            firstName,
            lastName,
        };
    }
    deriveProviderStatus(rawStatus) {
        const status = rawStatus.trim().toLowerCase();
        if (status === "successful" ||
            status === "success" ||
            status === "completed" ||
            status === "paid") {
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
    normalizeCurrency(raw) {
        const currency = raw.trim().toUpperCase();
        if (!currency || currency.length !== 3) {
            throw new Error("Invalid currency");
        }
        if (!this.allowedCurrencies.has(currency)) {
            throw new Error("Unsupported currency");
        }
        return currency;
    }
    normalizeAmount(raw) {
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
function buildReference(uid) {
    const shortUid = uid.slice(0, 8);
    const nonce = randomUUID().replace(/-/g, "").slice(0, 16);
    return `ps_add_${shortUid}_${Date.now()}_${nonce}`;
}
function roundMoney(value) {
    return Math.round((value + Number.EPSILON) * 100) / 100;
}
function parseBalances(raw) {
    if (!raw || typeof raw !== "object") {
        return {};
    }
    const source = raw;
    const out = {};
    for (const [key, value] of Object.entries(source)) {
        if (!key)
            continue;
        const parsed = parseNumeric(value);
        if (parsed === null)
            continue;
        out[key.toUpperCase()] = roundMoney(parsed);
    }
    return out;
}
function parseNumeric(raw) {
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
function asString(raw) {
    return typeof raw === "string" ? raw.trim() : "";
}
//# sourceMappingURL=FirestoreAddMoneyFlutterwaveRepository.js.map