import { FieldValue } from "firebase-admin/firestore";
import { createHash } from "node:crypto";
const CHALLENGE_DOC_ID = "passkey";
const PASSKEY_CREDENTIAL_INDEX_COLLECTION = "passkeyCredentialIndex";
const PASSKEY_SIGNIN_CHALLENGES_COLLECTION = "passkeySignInChallenges";
export class FirestorePasskeyRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    async listCredentials(uid) {
        const snap = await this.credentialsCollection(uid).get();
        return snap.docs.map((doc) => doc.data());
    }
    async getCredential(uid, credentialId) {
        const snap = await this.credentialsCollection(uid).doc(credentialId).get();
        if (!snap.exists)
            return null;
        return snap.data();
    }
    async upsertCredential(uid, credential) {
        const credentialRef = this.credentialsCollection(uid).doc(credential.credentialId);
        const indexRef = this.credentialIndexCollection().doc(credential.credentialId);
        await this.firestore.runTransaction(async (tx) => {
            tx.set(credentialRef, credential, { merge: true });
            tx.set(indexRef, {
                uid,
                createdAtMs: credential.createdAtMs,
                updatedAtMs: credential.updatedAtMs,
            }, { merge: true });
        });
    }
    async getCredentialOwner(credentialId) {
        const candidates = buildCredentialIdCandidates(credentialId);
        for (const candidate of candidates) {
            const owner = await this.resolveOwnerFromIndex(candidate);
            if (owner) {
                return owner;
            }
        }
        for (const candidate of candidates) {
            const owner = await this.resolveOwnerFromCollectionGroup(candidate);
            if (owner) {
                await this.backfillCredentialIndex(owner.uid, owner.credential);
                return owner;
            }
        }
        return null;
    }
    async deleteCredential(uid, credentialId) {
        const credentialRef = this.credentialsCollection(uid).doc(credentialId);
        const indexRef = this.credentialIndexCollection().doc(credentialId);
        await this.firestore.runTransaction(async (tx) => {
            tx.delete(credentialRef);
            tx.delete(indexRef);
        });
    }
    async updateCounter(uid, credentialId, counter) {
        await this.credentialsCollection(uid).doc(credentialId).set({
            counter,
            updatedAtMs: Date.now(),
        }, { merge: true });
    }
    async saveSignInChallenge(challenge, expiresAtMs) {
        const ref = this.signInChallengeDoc(challenge);
        await ref.set({
            challenge,
            expiresAtMs,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async consumeSignInChallenge(challenge) {
        const ref = this.signInChallengeDoc(challenge);
        return this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            if (!snap.exists)
                return null;
            const data = snap.data();
            const storedChallenge = (data.challenge || "").trim();
            const expiresAtMs = Number(data.expiresAtMs || 0);
            if (!storedChallenge || !expiresAtMs || storedChallenge !== challenge) {
                tx.delete(ref);
                return null;
            }
            tx.delete(ref);
            return {
                challenge: storedChallenge,
                expiresAtMs,
            };
        });
    }
    async saveChallenge(uid, challenge) {
        const ref = this.challengeDoc(uid);
        const payload = challenge.kind === "registration" ?
            {
                registrationChallenge: challenge.challenge,
                registrationExpiresAtMs: challenge.expiresAtMs,
            } :
            {
                authenticationChallenge: challenge.challenge,
                authenticationExpiresAtMs: challenge.expiresAtMs,
            };
        await ref.set({
            ...payload,
            updatedAt: FieldValue.serverTimestamp(),
        }, { merge: true });
    }
    async consumeChallenge(uid, kind) {
        const ref = this.challengeDoc(uid);
        return this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            if (!snap.exists)
                return null;
            const data = snap.data();
            const challenge = kind === "registration" ?
                data.registrationChallenge :
                data.authenticationChallenge;
            const expiresAtMs = kind === "registration" ?
                data.registrationExpiresAtMs :
                data.authenticationExpiresAtMs;
            if (!challenge || !expiresAtMs)
                return null;
            const clearPayload = kind === "registration" ?
                {
                    registrationChallenge: FieldValue.delete(),
                    registrationExpiresAtMs: FieldValue.delete(),
                } :
                {
                    authenticationChallenge: FieldValue.delete(),
                    authenticationExpiresAtMs: FieldValue.delete(),
                };
            tx.set(ref, {
                ...clearPayload,
                updatedAt: FieldValue.serverTimestamp(),
            }, { merge: true });
            return {
                kind,
                challenge,
                expiresAtMs,
            };
        });
    }
    credentialsCollection(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("passkeys");
    }
    challengeDoc(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("authSessionState")
            .doc(CHALLENGE_DOC_ID);
    }
    credentialIndexCollection() {
        return this.firestore.collection(PASSKEY_CREDENTIAL_INDEX_COLLECTION);
    }
    async resolveOwnerFromIndex(credentialId) {
        const indexSnap = await this.credentialIndexCollection().doc(credentialId).get();
        if (!indexSnap.exists)
            return null;
        const indexData = indexSnap.data();
        const uid = (indexData.uid || "").trim();
        if (!uid)
            return null;
        const credential = await this.getCredential(uid, credentialId);
        if (!credential) {
            await this.credentialIndexCollection().doc(credentialId).delete();
            return null;
        }
        return { uid, credential };
    }
    async resolveOwnerFromCollectionGroup(credentialId) {
        const snap = await this.firestore
            .collectionGroup("passkeys")
            .where("credentialId", "==", credentialId)
            .limit(1)
            .get();
        if (snap.empty)
            return null;
        const doc = snap.docs[0];
        const uid = doc.ref.parent.parent?.id?.trim() || "";
        if (!uid)
            return null;
        const credential = doc.data();
        return {
            uid,
            credential,
        };
    }
    async backfillCredentialIndex(uid, credential) {
        const id = credential.credentialId?.trim();
        if (!id)
            return;
        await this.credentialIndexCollection().doc(id).set({
            uid,
            createdAtMs: credential.createdAtMs || Date.now(),
            updatedAtMs: credential.updatedAtMs || Date.now(),
        }, { merge: true });
    }
    signInChallengeDoc(challenge) {
        return this.firestore
            .collection(PASSKEY_SIGNIN_CHALLENGES_COLLECTION)
            .doc(hashChallenge(challenge));
    }
}
function hashChallenge(challenge) {
    return createHash("sha256").update(challenge, "utf8").digest("base64url");
}
function buildCredentialIdCandidates(input) {
    const trimmed = input.trim();
    if (!trimmed)
        return [];
    const variants = new Set();
    variants.add(trimmed);
    if (/^[A-Za-z0-9+/_=-]+$/.test(trimmed)) {
        try {
            variants.add(Buffer.from(trimmed, "base64url").toString("base64url"));
        }
        catch {
            // Keep best-effort variants only.
        }
        try {
            variants.add(Buffer.from(trimmed, "base64").toString("base64url"));
        }
        catch {
            // Keep best-effort variants only.
        }
    }
    return Array.from(variants).filter((value) => value.length > 0);
}
//# sourceMappingURL=FirestorePasskeyRepository.js.map