import { Request, Response } from "express";
import { GetSecuritySettings } from "../application/usecase/GetSecuritySettings.js";
import { authContainer } from "../infrastructure/di/authContainer.js";
import { initDeps } from "../dependencies.js";

export async function getSecuritySettingsHandler(req: Request, res: Response) {
    try {
        const authHeader = req.headers.authorization;
        if (!authHeader?.startsWith("Bearer ")) {
            return res.status(401).json({ error: "Missing token" });
        }
        const idToken = authHeader.split("Bearer ")[1];
        const { auth } = initDeps();
        const decoded = await auth.verifyIdToken(idToken);
        const { securitySettings } = authContainer(); 
        const useCase = new GetSecuritySettings(securitySettings);
        const settings = await useCase.execute(decoded.uid);
        return res.status(200).json({ ok: true, settings });
    } catch (e) {
        console.error(e);
        return res.status(500).json({ error: "Internal error" });
    }
}
