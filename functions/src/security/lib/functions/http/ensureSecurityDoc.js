import { onRequest } from "firebase-functions/v2/https";
import { buildSecurityContainer } from "../../infrastructure/di/securityContainer.js";
export const ensureSecurityDoc = onRequest(async (req, res) => {
    const uid = req.query.uid;
    if (!uid) {
        res.status(400).json({ error: "missing uid" });
        return;
    }
    const { ensureSecuritySettings } = buildSecurityContainer();
    await ensureSecuritySettings.execute(uid);
    res.json({ ok: true });
});
//# sourceMappingURL=ensureSecurityDoc.js.map