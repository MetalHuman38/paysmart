import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { FinalizePhoneSignup } from "../application/usecase/FinalizePhoneSignup.js";
export async function finalizePhoneSignupHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { authService, securitySettings, userRepo } = authContainer();
        const authUser = await authService.getUser(decoded.uid);
        const useCase = new FinalizePhoneSignup(securitySettings, userRepo);
        await useCase.execute(authUser);
        return res.status(200).json({ ok: true });
    }
    catch (error) {
        if (error instanceof Error &&
            error.message === "Verified phone number is unavailable") {
            return res.status(400).json({ error: error.message });
        }
        console.error("finalizePhoneSignup failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=finalizePhoneSignup.js.map