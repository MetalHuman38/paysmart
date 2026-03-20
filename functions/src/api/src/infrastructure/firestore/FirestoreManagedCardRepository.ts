import { FieldValue, Firestore } from "firebase-admin/firestore";
import {
  ManagedCard,
  ManagedCardsListResult,
  PaymentSheetCustomerConfig,
} from "../../domain/model/managedCard.js";
import { ManagedCardRepository } from "../../domain/repository/ManagedCardRepository.js";
import {
  StripeCustomerCardPaymentMethod,
  StripePaymentsService,
} from "../../services/stripePaymentsService.js";

type StripeCustomerProfile = {
  email: string;
  name: string;
  customerId?: string;
  defaultPaymentMethodId?: string;
};

type ManagedCardDoc = ManagedCard & {
  uid: string;
  stripeCustomerId?: string | null;
  stripePaymentMethodId: string;
  detachedAt?: unknown;
  createdAt?: unknown;
  updatedAt?: unknown;
};

export class FirestoreManagedCardRepository implements ManagedCardRepository {
  constructor(
    private readonly firestore: Firestore,
    private readonly stripe: StripePaymentsService
  ) {}

  private userRef(uid: string) {
    return this.firestore.collection("users").doc(uid);
  }

  private managedCardsMetaRef(uid: string) {
    return this.userRef(uid).collection("payments").doc("managed_cards");
  }

  private managedCardsCollection(uid: string) {
    return this.managedCardsMetaRef(uid).collection("cards");
  }

  async preparePaymentSheetCustomer(uid: string): Promise<PaymentSheetCustomerConfig> {
    const profile = await this.resolveStripeCustomerProfile(uid);
    const customer = await this.stripe.ensureCustomer({
      uid,
      existingCustomerId: profile.customerId,
      email: profile.email,
      name: profile.name,
    });
    const ephemeralKey = await this.stripe.createEphemeralKey(customer.id);

    await this.persistStripeCustomerProfile(
      uid,
      customer.id,
      profile.email,
      customer.defaultPaymentMethodId
    );

    return {
      customerId: customer.id,
      ephemeralKeySecret: ephemeralKey.secret,
      defaultPaymentMethodId: customer.defaultPaymentMethodId,
    };
  }

  async list(uid: string): Promise<ManagedCardsListResult> {
    return this.syncFromProvider(uid);
  }

  async detach(uid: string, paymentMethodId: string): Promise<ManagedCardsListResult> {
    const profile = await this.resolveStripeCustomerProfile(uid);
    const customerId = profile.customerId?.trim();
    const cleanPaymentMethodId = paymentMethodId.trim();
    if (!customerId) {
      throw new Error("Stripe customer is not available for this account");
    }
    if (!cleanPaymentMethodId) {
      throw new Error("Missing paymentMethodId");
    }

    const current = await this.stripe.listCustomerCardPaymentMethods(customerId);
    if (!current.some((card) => card.id === cleanPaymentMethodId)) {
      throw new Error("Saved card not found");
    }

    await this.stripe.detachPaymentMethod(cleanPaymentMethodId);

    if (profile.defaultPaymentMethodId === cleanPaymentMethodId) {
      await this.stripe.setDefaultPaymentMethod(customerId, undefined);
      await this.persistStripeCustomerProfile(uid, customerId, profile.email, undefined);
    }

    await this.managedCardsCollection(uid).doc(cleanPaymentMethodId).delete().catch(() => undefined);
    return this.syncFromProvider(uid);
  }

  async setDefault(uid: string, paymentMethodId: string): Promise<ManagedCardsListResult> {
    const profile = await this.resolveStripeCustomerProfile(uid);
    const customerId = profile.customerId?.trim();
    const cleanPaymentMethodId = paymentMethodId.trim();
    if (!customerId) {
      throw new Error("Stripe customer is not available for this account");
    }
    if (!cleanPaymentMethodId) {
      throw new Error("Missing paymentMethodId");
    }

    const current = await this.stripe.listCustomerCardPaymentMethods(customerId);
    if (!current.some((card) => card.id === cleanPaymentMethodId)) {
      throw new Error("Saved card not found");
    }

    await this.stripe.setDefaultPaymentMethod(customerId, cleanPaymentMethodId);
    await this.persistStripeCustomerProfile(uid, customerId, profile.email, cleanPaymentMethodId);
    return this.syncFromProvider(uid);
  }

  async syncFromProvider(uid: string): Promise<ManagedCardsListResult> {
    const profile = await this.resolveStripeCustomerProfile(uid);
    const customerId = profile.customerId?.trim();
    const updatedAtMs = Date.now();

    if (!customerId) {
      await this.persistCardSnapshot(uid, [], undefined, undefined, updatedAtMs);
      return {
        cards: [],
        stripeCustomerId: undefined,
        defaultPaymentMethodId: undefined,
        updatedAtMs,
      };
    }

    const customer = await this.stripe.retrieveCustomer(customerId);
    const cards = await this.stripe.listCustomerCardPaymentMethods(customer.id);
    const mapped = cards.map((card) =>
      mapManagedCard(
        uid,
        card,
        customer.id,
        customer.defaultPaymentMethodId
      )
    );

    await this.persistStripeCustomerProfile(
      uid,
      customer.id,
      profile.email,
      customer.defaultPaymentMethodId
    );
    await this.persistCardSnapshot(
      uid,
      mapped,
      customer.id,
      customer.defaultPaymentMethodId,
      updatedAtMs
    );

    return {
      cards: mapped,
      stripeCustomerId: customer.id,
      defaultPaymentMethodId: customer.defaultPaymentMethodId,
      updatedAtMs,
    };
  }

  private async persistCardSnapshot(
    uid: string,
    cards: ManagedCard[],
    stripeCustomerId?: string,
    defaultPaymentMethodId?: string,
    updatedAtMs: number = Date.now()
  ): Promise<void> {
    const collection = this.managedCardsCollection(uid);
    const existing = await collection.get();
    const batch = this.firestore.batch();
    const currentIds = cards.map((card) => card.id);

    for (const doc of existing.docs) {
      if (!currentIds.includes(doc.id)) {
        batch.delete(doc.ref);
      }
    }

    for (const card of cards) {
      batch.set(
        collection.doc(card.id),
        {
          ...card,
          uid,
          stripeCustomerId: stripeCustomerId ?? null,
          stripePaymentMethodId: card.id,
          updatedAt: FieldValue.serverTimestamp(),
          createdAt: FieldValue.serverTimestamp(),
        } satisfies ManagedCardDoc,
        { merge: true }
      );
    }

    batch.set(
      this.managedCardsMetaRef(uid),
      {
        provider: "stripe",
        stripeCustomerId: stripeCustomerId ?? null,
        defaultPaymentMethodId: defaultPaymentMethodId ?? null,
        cardIds: currentIds,
        cardCount: cards.length,
        updatedAtMs,
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    await batch.commit();
  }

  private async resolveStripeCustomerProfile(uid: string): Promise<StripeCustomerProfile> {
    const userSnap = await this.userRef(uid).get();
    const user = userSnap.data() as Record<string, unknown> | undefined;
    const displayName = asString(user?.displayName);
    const name = displayName || "PaySmart User";
    const email = asString(user?.email) || `${uid}@users.pay-smart.net`;
    const paymentProviders = asRecord(user?.paymentProviders);
    const stripeProvider = asRecord(paymentProviders.stripe);

    return {
      email: email.trim().toLowerCase(),
      name,
      customerId: asString(stripeProvider.customerId) || undefined,
      defaultPaymentMethodId: asString(stripeProvider.defaultPaymentMethodId) || undefined,
    };
  }

  private async persistStripeCustomerProfile(
    uid: string,
    customerId: string,
    email: string,
    defaultPaymentMethodId?: string
  ): Promise<void> {
    const cleanCustomerId = customerId.trim();
    if (!cleanCustomerId) return;

    await this.userRef(uid).set(
      {
        paymentProviders: {
          stripe: {
            customerId: cleanCustomerId,
            email: email.trim().toLowerCase(),
            defaultPaymentMethodId: defaultPaymentMethodId ?? null,
            updatedAt: FieldValue.serverTimestamp(),
          },
        },
      },
      { merge: true }
    );
  }
}

function mapManagedCard(
  uid: string,
  paymentMethod: StripeCustomerCardPaymentMethod,
  stripeCustomerId: string,
  defaultPaymentMethodId?: string
): ManagedCardDoc {
  const now = Date.now();
  return {
    id: paymentMethod.id,
    uid,
    provider: "stripe",
    brand: paymentMethod.brand || "card",
    last4: paymentMethod.last4,
    expMonth: paymentMethod.expMonth,
    expYear: paymentMethod.expYear,
    funding: paymentMethod.funding,
    country: paymentMethod.country,
    fingerprint: paymentMethod.fingerprint,
    isDefault: paymentMethod.id == defaultPaymentMethodId,
    status: "active",
    createdAtMs: paymentMethod.createdAtMs || now,
    updatedAtMs: now,
    stripeCustomerId,
    stripePaymentMethodId: paymentMethod.id,
  };
}

function asString(raw: unknown): string {
  return typeof raw === "string" ? raw.trim() : "";
}

function asRecord(raw: unknown): Record<string, unknown> {
  return raw && typeof raw === "object" ? (raw as Record<string, unknown>) : {};
}
