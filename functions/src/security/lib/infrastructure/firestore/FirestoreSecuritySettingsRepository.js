import { getDefaultSecuritySettings } from "../../constants/index.js";
export class FirestoreSecuritySettingsRepository {
    firestore;
    constructor(firestore) {
        this.firestore = firestore;
    }
    ref(uid) {
        return this.firestore
            .collection("users")
            .doc(uid)
            .collection("security")
            .doc("settings");
    }
    async get(uid) {
        const snap = await this.ref(uid).get();
        return snap.exists ? snap.data() : null;
    }
    async createIfMissing(uid) {
        const ref = this.ref(uid);
        await this.firestore.runTransaction(async (tx) => {
            const snap = await tx.get(ref);
            if (!snap.exists) {
                tx.set(ref, getDefaultSecuritySettings());
            }
        });
    }
    async update(uid, data) {
        await this.ref(uid).set(data, { merge: true });
    }
}
//# sourceMappingURL=FirestoreSecuritySettingsRepository.js.map