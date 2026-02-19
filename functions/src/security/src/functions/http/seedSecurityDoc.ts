import { onRequest } from "firebase-functions/v2/https";
import { buildSecurityContainer } from "../../infrastructure/di/securityContainer.js";

export const seedSecurityOnUserCreate = onRequest(async (req, res) => {
  const uid = req.query.uid as string;
  if (!uid) {
    res.status(400).json({ error: "missing uid" });
    return;
  }

  const { seedSecuritySettingsOnUserCreate } = buildSecurityContainer();
  await seedSecuritySettingsOnUserCreate.execute(uid);

  res.json({ ok: true });
});
