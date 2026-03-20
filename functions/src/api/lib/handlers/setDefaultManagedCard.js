import { initDeps } from "../dependencies.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { SetDefaultManagedCard } from "../application/usecase/SetDefaultManagedCard.js";
import { handleManagedCardError } from "./listManagedCards.js";
export async function setDefaultManagedCardHandler(req, res) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const paymentMethodId = String(req.params?.paymentMethodId || "").trim();
        if (!paymentMethodId) {
            return res.status(400).json({ error: "Missing paymentMethodId" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { managedCards } = authContainer();
        const useCase = new SetDefaultManagedCard(managedCards);
        const result = await useCase.execute(decoded.uid, paymentMethodId);
        return res.status(200).json(result);
    }
    catch (error) {
        return handleManagedCardError(error, res, "setDefaultManagedCardHandler failed");
    }
}
//# sourceMappingURL=setDefaultManagedCard.js.map