import { getDefaultSecuritySettings } from "../constants/index.js";
import { onRequest } from "firebase-functions/v2/https";
import { initDeps } from "../dependencies.js";
const REGION = "europe-west2";
export const ensureSecurityDoc = onRequest({
    cors: false,
    region: REGION,
    memory: "128MiB",
    concurrency: 20,
    cpu: 1,
}, async (req, res) => {
    const { firestore } = initDeps();
    const uid = req.query.uid ?? "";
    if (!uid) {
        res.status(400).json({ error: "missing uid" });
        return;
    }
    const secRef = firestore
        .collection("users")
        .doc(uid)
        .collection("security")
        .doc("settings");
    await firestore.runTransaction(async (tx) => {
        const snap = await tx.get(secRef);
        if (!snap.exists) {
            tx.set(secRef, getDefaultSecuritySettings());
        }
    });
    res.json({ ok: true });
});
//# sourceMappingURL=ensureSecurityDoc.js.map