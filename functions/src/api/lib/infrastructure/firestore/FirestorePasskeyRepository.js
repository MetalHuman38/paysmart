import { FieldValue } from "firebase-admin/firestore";
const CHALLENGE_DOC_ID = "passkey";
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
        await this.credentialsCollection(uid)
            .doc(credential.credentialId)
            .set(credential, { merge: true });
    }
    async updateCounter(uid, credentialId, counter) {
        await this.credentialsCollection(uid).doc(credentialId).set({
            counter,
            updatedAtMs: Date.now(),
        }, { merge: true });
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
}
//# sourceMappingURL=FirestorePasskeyRepository.js.map