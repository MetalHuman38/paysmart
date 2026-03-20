import { FieldValue } from "firebase-admin/firestore";
const SUPPORTED_WEBHOOK_EVENTS = new Set([
    "checkout.session.completed",
    "checkout.session.async_payment_succeeded",
    "checkout.session.expired",
    "payment_intent.succeeded",
    "payment_intent.payment_failed",
    "payment_intent.processing",
]);
const PAYMENT_INTENT_SESSION_TTL_MS = 30 * 60 * 1000;
export class FirestoreAddMoneyRepository {
    firestore;
    stripe;
    managedCards;
    stripePublishableKey;
    allowedCurrencies;
    minimumAmountMinor;
    constructor(firestore, stripe, managedCards, stripePublishableKey, allowedCurrencies, minimumAmountMinor) {
        this.firestore = firestore;
        this.stripe = stripe;
        this.managedCards = managedCards;
        this.stripePublishableKey = stripePublishableKey;
        this.allowedCurrencies = allowedCurrencies;
        this.minimumAmountMinor = minimumAmountMinor;
    }
    sessionRef(uid, sessionId) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("payments")
            .doc("add_money")
            .collection("sessions")
            .doc(sessionId);
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
        const publishableKey = this.stripePublishableKey.trim();
        if (!publishableKey) {
            throw new Error("STRIPE_PUBLISHABLE_KEY is not configured");
        }
        const currency = this.normalizeCurrency(input.currency);
        const amountMinor = this.normalizeAmount(input.amountMinor);
        const paymentSheetCustomer = await this.managedCards.preparePaymentSheetCustomer(uid);
        const stripeIntent = await this.stripe.createTopupPaymentIntent({
            uid,
            amountMinor,
            currency,
            idempotencyKey: input.idempotencyKey,
            customerId: paymentSheetCustomer.customerId,
            setupFutureUsage: "off_session",
        });
        const status = this.derivePaymentIntentStatus(stripeIntent.status);
        const expiresAtMs = stripeIntent.createdAtMs + PAYMENT_INTENT_SESSION_TTL_MS;
        const doc = {
            sessionId: stripeIntent.id,
            uid,
            provider: "stripe",
            amountMinor,
            currency,
            expiresAtMs,
            stripeStatus: stripeIntent.status,
            stripePaymentStatus: stripeIntent.status,
            stripePaymentIntentId: stripeIntent.id,
            stripeCustomerId: paymentSheetCustomer.customerId,
            status,
        };
        await this.sessionRef(uid, stripeIntent.id).set({
            ...doc,
            createdAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
        });
        if (status === "succeeded") {
            await this.applySettlementIfNeeded(uid, stripeIntent.id, amountMinor, currency, stripeIntent.id);
        }
        return {
            sessionId: stripeIntent.id,
            amountMinor,
            currency,
            status,
            expiresAtMs,
            paymentIntentId: stripeIntent.id,
            paymentIntentClientSecret: stripeIntent.clientSecret,
            publishableKey,
            customerId: paymentSheetCustomer.customerId,
            customerEphemeralKeySecret: paymentSheetCustomer.ephemeralKeySecret,
            defaultPaymentMethodId: paymentSheetCustomer.defaultPaymentMethodId,
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
            throw new Error("Add money session not found");
        }
        const current = snap.data();
        if (current.uid !== uid) {
            throw new Error("Add money session not found");
        }
        const isPaymentIntentSession = cleanSessionId.startsWith("pi_");
        const stripeSession = isPaymentIntentSession ? null : await this.stripe.retrieveCheckoutSession(cleanSessionId);
        const stripeIntent = isPaymentIntentSession ? await this.stripe.retrievePaymentIntent(cleanSessionId) : null;
        const status = stripeIntent ?
            this.derivePaymentIntentStatus(stripeIntent.status) :
            this.deriveCheckoutStatus(stripeSession);
        const paymentIntentId = stripeIntent?.id ?? stripeSession?.paymentIntentId;
        const stripeStatus = stripeIntent?.status ?? stripeSession?.status ?? current.stripeStatus;
        const stripePaymentStatus = stripeIntent?.status ?? stripeSession?.paymentStatus ?? current.stripePaymentStatus;
        await ref.set({
            status,
            stripeStatus,
            stripePaymentStatus,
            stripePaymentIntentId: paymentIntentId ?? null,
            failureCode: current.failureCode ?? null,
            failureMessage: current.failureMessage ?? null,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        if (status === "succeeded") {
            await this.applySettlementIfNeeded(uid, cleanSessionId, current.amountMinor, current.currency, paymentIntentId);
            await this.managedCards.syncFromProvider(uid);
        }
        return {
            sessionId: cleanSessionId,
            checkoutUrl: current.checkoutUrl,
            amountMinor: current.amountMinor,
            currency: current.currency,
            status,
            expiresAtMs: current.expiresAtMs,
            paymentIntentId,
            failureCode: current.failureCode,
            failureMessage: current.failureMessage,
            updatedAtMs: Date.now(),
        };
    }
    async applyWebhook(rawPayload, signatureHeader) {
        const event = this.stripe.parseWebhookEvent(rawPayload, signatureHeader);
        if (!SUPPORTED_WEBHOOK_EVENTS.has(event.type)) {
            return {
                handled: false,
            };
        }
        if (event.type.startsWith("payment_intent.")) {
            const paymentIntentId = asString(event.dataObject.id);
            if (!paymentIntentId) {
                throw new Error("Stripe payment intent event is missing id");
            }
            const stripeIntent = await this.stripe.retrievePaymentIntent(paymentIntentId);
            const uid = stripeIntent.metadata.uid || asString(asRecord(event.dataObject.metadata).uid);
            if (!uid) {
                throw new Error("Stripe payment intent is missing uid metadata");
            }
            const status = event.type === "payment_intent.payment_failed" ?
                "failed" :
                this.derivePaymentIntentStatus(stripeIntent.status);
            const paymentError = asRecord(event.dataObject.last_payment_error);
            await this.sessionRef(uid, stripeIntent.id).set({
                sessionId: stripeIntent.id,
                uid,
                provider: "stripe",
                amountMinor: stripeIntent.amountMinor,
                currency: stripeIntent.currency,
                expiresAtMs: stripeIntent.createdAtMs + PAYMENT_INTENT_SESSION_TTL_MS,
                stripeStatus: stripeIntent.status,
                stripePaymentStatus: stripeIntent.status,
                stripePaymentIntentId: stripeIntent.id,
                stripeCustomerId: stripeIntent.customerId ?? null,
                status,
                failureCode: asString(paymentError.code) || null,
                failureMessage: asString(paymentError.message) || null,
                lastWebhookEventId: event.id,
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
            if (status === "succeeded") {
                await this.applySettlementIfNeeded(uid, stripeIntent.id, stripeIntent.amountMinor, stripeIntent.currency, stripeIntent.id);
                await this.managedCards.syncFromProvider(uid);
            }
            return {
                handled: true,
                sessionId: stripeIntent.id,
                uid,
                status,
            };
        }
        const stripeSession = await this.stripe.retrieveCheckoutSession(asString(event.dataObject.id));
        const uid = stripeSession.metadata.uid || asString(event.dataObject.client_reference_id);
        if (!uid) {
            throw new Error("Stripe session is missing uid metadata");
        }
        const status = this.deriveCheckoutStatus(stripeSession);
        await this.sessionRef(uid, stripeSession.id).set({
            sessionId: stripeSession.id,
            uid,
            provider: "stripe",
            amountMinor: stripeSession.amountTotalMinor,
            currency: stripeSession.currency,
            checkoutUrl: stripeSession.url,
            expiresAtMs: stripeSession.expiresAtMs,
            stripeStatus: stripeSession.status,
            stripePaymentStatus: stripeSession.paymentStatus,
            stripePaymentIntentId: stripeSession.paymentIntentId ?? null,
            status,
            lastWebhookEventId: event.id,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
        if (status === "succeeded") {
            await this.applySettlementIfNeeded(uid, stripeSession.id, stripeSession.amountTotalMinor, stripeSession.currency, stripeSession.paymentIntentId);
            await this.managedCards.syncFromProvider(uid);
        }
        return {
            handled: true,
            sessionId: stripeSession.id,
            uid,
            status,
        };
    }
    async applySettlementIfNeeded(uid, sessionId, amountMinor, currency, paymentIntentId) {
        const sessionRef = this.sessionRef(uid, sessionId);
        const walletRef = this.walletCurrentRef(uid);
        const userRef = this.userRef(uid);
        const walletTxRef = this.walletTransactionRef(uid, sessionId);
        const amountMajor = roundMoney(amountMinor / 100);
        return this.firestore.runTransaction(async (tx) => {
            const sessionSnap = await tx.get(sessionRef);
            if (!sessionSnap.exists) {
                throw new Error("Add money session not found");
            }
            const sessionData = sessionSnap.data();
            if (sessionData.uid !== uid) {
                throw new Error("Add money session ownership mismatch");
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
                provider: "stripe",
                amountMinor,
                amountMajor,
                currency,
                status: "succeeded",
                stripeSessionId: sessionId,
                stripePaymentIntentId: paymentIntentId ?? null,
                createdAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
            tx.set(sessionRef, {
                status: "succeeded",
                stripePaymentIntentId: paymentIntentId ?? null,
                walletAppliedAt: FieldValue.serverTimestamp(),
                settledAt: FieldValue.serverTimestamp(),
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
            return true;
        });
    }
    deriveCheckoutStatus(session) {
        const stripeStatus = session.status.trim().toLowerCase();
        const paymentStatus = session.paymentStatus.trim().toLowerCase();
        if (paymentStatus === "paid" || paymentStatus === "no_payment_required") {
            return "succeeded";
        }
        if (stripeStatus === "expired") {
            return "expired";
        }
        if (paymentStatus === "failed") {
            return "failed";
        }
        if (stripeStatus === "open" || stripeStatus === "complete") {
            return "pending";
        }
        return "created";
    }
    derivePaymentIntentStatus(statusRaw) {
        const status = statusRaw.trim().toLowerCase();
        switch (status) {
            case "succeeded":
                return "succeeded";
            case "requires_payment_method":
            case "requires_action":
            case "requires_confirmation":
            case "processing":
            case "requires_capture":
                return "pending";
            case "canceled":
                return "failed";
            default:
                return "created";
        }
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
function asRecord(raw) {
    return raw && typeof raw === "object" ? raw : {};
}
//# sourceMappingURL=FirestoreAddMoneyRepository.js.map