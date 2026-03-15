import { randomUUID } from "crypto";
import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { FlutterwaveProviderRequestError, } from "../../services/flutterwavePaymentsService.js";
export class FirestoreFlutterwaveFundingAccountRepository {
    firestore;
    flutterwave;
    constructor(firestore, flutterwave) {
        this.firestore = firestore;
        this.flutterwave = flutterwave;
    }
    userRef(uid) {
        return this.firestore.collection("users").doc(uid);
    }
    fundingAccountRef(uid) {
        return this.userRef(uid)
            .collection("payments")
            .doc("flutterwave")
            .collection("fundingAccount")
            .doc("current");
    }
    async getCurrent(uid) {
        const snap = await this.fundingAccountRef(uid).get();
        if (!snap.exists) {
            return null;
        }
        return this.mapDocToDomain(snap.data());
    }
    async provision(uid, input) {
        const profile = await this.resolveProvisioningProfile(uid, input);
        const current = await this.getCurrent(uid);
        if (current && (current.status === "active" || current.status === "pending")) {
            await this.persistFlutterwaveCustomerCache(uid, current.customerId || profile.customerId || "", profile.email);
            return {
                ...current,
                provisioningResult: "existing",
            };
        }
        if (current?.accountId) {
            try {
                const refreshed = await this.flutterwave.retrievePermanentFundingAccount(current.accountId);
                const synced = await this.persistFundingAccount(uid, profile, refreshed, current);
                await this.persistFlutterwaveCustomerCache(uid, refreshed.customerId, profile.email);
                return {
                    ...synced,
                    provisioningResult: "existing",
                };
            }
            catch (error) {
                if (!(error instanceof FlutterwaveProviderRequestError) ||
                    error.status !== 404) {
                    throw error;
                }
            }
        }
        const reference = randomUUID();
        const created = await this.flutterwave.createPermanentFundingAccount({
            uid,
            reference,
            idempotencyKey: input.idempotencyKey?.trim() || `paysmart:flutterwave:funding-account:${uid}`,
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
    async resolveProvisioningProfile(uid, input) {
        const userSnap = await this.userRef(uid).get();
        const user = userSnap.data();
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
        const bvn = normalizeRegulatoryIdentifier(injectedKyc.bvn, 11) ||
            normalizeRegulatoryIdentifier(ng.bvn, 11) ||
            undefined;
        const nin = normalizeRegulatoryIdentifier(injectedKyc.nin, 11) ||
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
    async persistFundingAccount(uid, profile, provider, existing) {
        const ref = this.fundingAccountRef(uid);
        const doc = {
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
        await ref.set({
            ...doc,
            ...(existing ? {} : { createdAt: FieldValue.serverTimestamp() }),
            updatedAt: FieldValue.serverTimestamp(),
            lastSyncedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
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
    async persistFlutterwaveCustomerCache(uid, customerId, email) {
        const cleanCustomerId = customerId.trim();
        if (!cleanCustomerId) {
            return;
        }
        await this.userRef(uid).set({
            paymentProviders: {
                flutterwave: {
                    customerId: cleanCustomerId,
                    email: email.trim().toLowerCase(),
                    updatedAt: FieldValue.serverTimestamp(),
                },
            },
        }, { merge: true });
    }
    mapDocToDomain(doc) {
        if (!doc.accountId ||
            !doc.accountNumber ||
            !doc.bankName ||
            !doc.accountName ||
            !doc.reference ||
            !doc.flutterwaveCustomerId) {
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
            createdAtMs: doc.providerCreatedAtMs ??
                timestampMillis(doc.createdAt) ??
                Date.now(),
            updatedAtMs: doc.providerUpdatedAtMs ??
                timestampMillis(doc.updatedAt) ??
                timestampMillis(doc.lastSyncedAt) ??
                Date.now(),
        };
    }
    deriveStatus(providerStatus) {
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
function asString(raw) {
    return typeof raw === "string" ? raw.trim() : "";
}
function asRecord(raw) {
    return raw && typeof raw === "object" ? raw : {};
}
function normalizeRegulatoryIdentifier(raw, expectedLength) {
    const digits = asString(raw).replace(/\D+/g, "");
    return digits.length === expectedLength ? digits : "";
}
function timestampMillis(value) {
    return value instanceof Timestamp ? value.toMillis() : undefined;
}
//# sourceMappingURL=FirestoreFlutterwaveFundingAccountRepository.js.map