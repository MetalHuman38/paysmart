import { apiContainer } from "../infrastructure/di/apiContainer.js";
export async function generateEmailVerificationHandler(req, res) {
    try {
        const { getUIDFromAuthHeader } = apiContainer();
        const uid = await getUIDFromAuthHeader.execute(req.headers.authorization);
        const email = String(req.body?.email || "").trim().toLowerCase();
        if (!email) {
            return res.status(400).json({ error: "Email is required" });
        }
        const { generateEmailVerification } = apiContainer();
        const result = await generateEmailVerification.execute({ uid, email });
        if ("retryAfter" in result && result.retryAfter) {
            res.setHeader("Retry-After", String(result.retryAfter));
            return res.status(429).json({ error: "Cooldown active" });
        }
        return res.json({ sent: true });
    }
    catch (err) {
        console.error("email verification error:", err);
        return res.status(500).json({ error: err.message });
    }
}
export async function checkEmailVerificationStatusHandler(req, res) {
    try {
        const { getUIDFromAuthHeader } = apiContainer();
        const { checkEmailVerificationStatus } = apiContainer();
        const uid = await getUIDFromAuthHeader.execute(req.headers.authorization);
        return res.json(await checkEmailVerificationStatus.execute(uid));
    }
    catch (err) {
        console.error("checkEmailVerificationStatus error:", err);
        return res.status(500).json({ error: err.message });
    }
}
//# sourceMappingURL=emailVerificationHandlers.js.map