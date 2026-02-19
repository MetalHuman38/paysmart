import { initDeps } from "../dependencies.js";
import { EnableBiometrics } from "../application/usecase/EnableBiometrics.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
export async function biometricsEnabledHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { securitySettings } = authContainer();
        const useCase = new EnableBiometrics(securitySettings);
        await useCase.execute(decoded.uid);
        return res.status(200).json({ ok: true });
    }
    catch (e) {
        console.error(e);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=biometricEnabled.js.map