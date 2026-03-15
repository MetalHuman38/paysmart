import { randomUUID } from "crypto";
import { FieldValue, Firestore, Timestamp } from "firebase-admin/firestore";
import {
  FlutterwaveFundingAccount,
  FlutterwaveFundingAccountStatus,
  ProvisionFlutterwaveFundingAccountInput,
  ProvisionFlutterwaveFundingAccountResult,
} from "../../domain/model/flutterwaveFundingAccount.js";
import { FlutterwaveFundingAccountRepository } from "../../domain/repository/FlutterwaveFundingAccountRepository.js";
import {
  FlutterwavePaymentsService,
  FlutterwavePermanentFundingAccount,
  FlutterwaveProviderRequestError,
} from "../../services/flutterwavePaymentsService.js";

type FlutterwaveFundingAccountDoc = {
  accountId: string;
  uid: string;
  provider: "flutterwave";
  currency: "NGN";
  accountNumber: string;
  bankName: string;
  accountName: string;
  reference: string;
  status: FlutterwaveFundingAccountStatus;
  providerStatus: string;
  flutterwaveCustomerId: string;
  note?: string | null;
  providerCreatedAtMs?: number;
  providerUpdatedAtMs?: number;
  createdAt?: unknown;
  updatedAt?: unknown;
  lastSyncedAt?: unknown;
};

type ProvisioningProfile = {
  email: string;
  firstName: string;
  lastName: string;
  accountName: string;
  customerId?: string;
  bvn?: string;
  nin?: string;
};

export class FirestoreFlutterwaveFundingAccountRepository
  implements FlutterwaveFundingAccountRepository
{
  constructor(
    private readonly firestore: Firestore,
    private readonly flutterwave: FlutterwavePaymentsService
  ) {}

  private userRef(uid: string) {
    return this.firestore.collection("users").doc(uid);
  }

  private fundingAccountRef(uid: string) {
    return this.userRef(uid)
      .collection("payments")
      .doc("flutterwave")
      .collection("fundingAccount")
      .doc("current");
  }

  async getCurrent(uid: string): Promise<FlutterwaveFundingAccount | null> {
    const snap = await this.fundingAccountRef(uid).get();
    if (!snap.exists) {
      return null;
    }

    return this.mapDocToDomain(snap.data() as FlutterwaveFundingAccountDoc);
  }

  async provision(
    uid: string,
    input: ProvisionFlutterwaveFundingAccountInput
  ): Promise<ProvisionFlutterwaveFundingAccountResult> {
    const profile = await this.resolveProvisioningProfile(uid, input);
    const current = await this.getCurrent(uid);

    if (current && (current.status === "active" || current.status === "pending")) {
      await this.persistFlutterwaveCustomerCache(
        uid,
        current.customerId || profile.customerId || "",
        profile.email
      );
      return {
        ...current,
        provisioningResult: "existing",
      };
    }

    if (current?.accountId) {
      try {
        const refreshed = await this.flutterwave.retrievePermanentFundingAccount(
          current.accountId
        );
        const synced = await this.persistFundingAccount(uid, profile, refreshed, current);
        await this.persistFlutterwaveCustomerCache(
          uid,
          refreshed.customerId,
          profile.email
        );
        return {
          ...synced,
          provisioningResult: "existing",
        };
      } catch (error) {
        if (
          !(error instanceof FlutterwaveProviderRequestError) ||
          error.status !== 404
        ) {
          throw error;
        }
      }
    }

    const reference = randomUUID();
    const created = await this.flutterwave.createPermanentFundingAccount({
      uid,
      reference,
      idempotencyKey:
        input.idempotencyKey?.trim() || `paysmart:flutterwave:funding-account:${uid}`,
      customerId: profile.customerId,
      customer: {
        email: profile.email,
        firstName: profile.firstName,
        lastName: profile.lastName,
      },
      kyc: {
        ...(profile.bvn ? { bvn: profile.bvn } : {}),
        ...(profile.nin ? { nin: profile.nin } : {}),
      },
    });

    const persisted = await this.persistFundingAccount(uid, profile, created, current);
    await this.persistFlutterwaveCustomerCache(uid, created.customerId, profile.email);

    return {
      ...persisted,
      provisioningResult: "created",
    };
  }

  private async resolveProvisioningProfile(
    uid: string,
    input: ProvisionFlutterwaveFundingAccountInput
  ): Promise<ProvisioningProfile> {
    const userSnap = await this.userRef(uid).get();
    const user = userSnap.data() as Record<string, unknown> | undefined;
    const displayName = asString(user?.displayName);
    const nameParts = displayName.split(/\s+/).filter(Boolean);
    const firstName = nameParts[0] || "PaySmart";
    const lastName = nameParts.slice(1).join(" ") || "User";
    const accountName = [firstName, lastName].join(" ").trim();
    const email = asString(user?.email) || `${uid}@users.pay-smart.net`;

    const paymentProviders = asRecord(user?.paymentProviders);
    const flutterwaveProvider = asRecord(paymentProviders.flutterwave);
    const customerId = asString(flutterwaveProvider.customerId) || undefined;

    const kyc = asRecord(user?.kyc);
    const ng = asRecord(kyc.ng);
    const injectedKyc = input.kyc ?? {};
    const bvn =
      normalizeRegulatoryIdentifier(injectedKyc.bvn, 11) ||
      normalizeRegulatoryIdentifier(ng.bvn, 11) ||
      undefined;
    const nin =
      normalizeRegulatoryIdentifier(injectedKyc.nin, 11) ||
      normalizeRegulatoryIdentifier(ng.nin, 11) ||
      undefined;

    if (!bvn && !nin) {
      throw new Error("FLUTTERWAVE_FUNDING_ACCOUNT_KYC_REQUIRED");
    }

    return {
      email: email.trim().toLowerCase(),
      firstName,
      lastName,
      accountName,
      customerId,
      bvn,
      nin,
    };
  }

  private async persistFundingAccount(
    uid: string,
    profile: ProvisioningProfile,
    provider: FlutterwavePermanentFundingAccount,
    existing: FlutterwaveFundingAccount | null
  ): Promise<FlutterwaveFundingAccount> {
    const ref = this.fundingAccountRef(uid);
    const doc: FlutterwaveFundingAccountDoc = {
      accountId: provider.accountId,
      uid,
      provider: "flutterwave",
      currency: "NGN",
      accountNumber: provider.accountNumber,
      bankName: provider.bankName,
      accountName: provider.accountName || profile.accountName,
      reference: provider.reference,
      status: this.deriveStatus(provider.status),
      providerStatus: provider.status,
      flutterwaveCustomerId: provider.customerId,
      note: provider.note ?? null,
      providerCreatedAtMs: provider.createdAtMs,
      providerUpdatedAtMs: provider.updatedAtMs,
    };

    await ref.set(
      {
        ...doc,
        ...(existing ? {} : { createdAt: FieldValue.serverTimestamp() }),
        updatedAt: FieldValue.serverTimestamp(),
        lastSyncedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );

    return {
      accountId: doc.accountId,
      provider: "flutterwave",
      currency: "NGN",
      accountNumber: doc.accountNumber,
      bankName: doc.bankName,
      accountName: doc.accountName,
      reference: doc.reference,
      status: doc.status,
      providerStatus: doc.providerStatus,
      customerId: doc.flutterwaveCustomerId,
      note: doc.note ?? undefined,
      createdAtMs: existing?.createdAtMs ?? provider.createdAtMs ?? Date.now(),
      updatedAtMs: provider.updatedAtMs ?? Date.now(),
    };
  }

  private async persistFlutterwaveCustomerCache(
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

  private mapDocToDomain(doc: FlutterwaveFundingAccountDoc): FlutterwaveFundingAccount {
    if (
      !doc.accountId ||
      !doc.accountNumber ||
      !doc.bankName ||
      !doc.accountName ||
      !doc.reference ||
      !doc.flutterwaveCustomerId
    ) {
      throw new Error("Stored Flutterwave funding account is malformed");
    }

    return {
      accountId: doc.accountId,
      provider: "flutterwave",
      currency: "NGN",
      accountNumber: doc.accountNumber,
      bankName: doc.bankName,
      accountName: doc.accountName,
      reference: doc.reference,
      status: doc.status,
      providerStatus: doc.providerStatus,
      customerId: doc.flutterwaveCustomerId,
      note: doc.note ?? undefined,
      createdAtMs:
        doc.providerCreatedAtMs ??
        timestampMillis(doc.createdAt) ??
        Date.now(),
      updatedAtMs:
        doc.providerUpdatedAtMs ??
        timestampMillis(doc.updatedAt) ??
        timestampMillis(doc.lastSyncedAt) ??
        Date.now(),
    };
  }

  private deriveStatus(providerStatus: string): FlutterwaveFundingAccountStatus {
    const status = providerStatus.trim().toLowerCase();
    if (status === "active" || status === "successful") {
      return "active";
    }
    if (status === "pending" || status === "processing" || status === "queued") {
      return "pending";
    }
    if (status === "disabled" || status === "inactive" || status === "blocked") {
      return "disabled";
    }
    return "failed";
  }
}

function asString(raw: unknown): string {
  return typeof raw === "string" ? raw.trim() : "";
}

function asRecord(raw: unknown): Record<string, unknown> {
  return raw && typeof raw === "object" ? (raw as Record<string, unknown>) : {};
}

function normalizeRegulatoryIdentifier(raw: unknown, expectedLength: number): string {
  const digits = asString(raw).replace(/\D+/g, "");
  return digits.length === expectedLength ? digits : "";
}

function timestampMillis(value: unknown): number | undefined {
  return value instanceof Timestamp ? value.toMillis() : undefined;
}
