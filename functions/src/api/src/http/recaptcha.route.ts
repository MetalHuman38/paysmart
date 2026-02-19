import type { Express, Request, Response } from "express";
import { verifyRecaptcha } from "../recaptchaAgent.js";


export function mountRecaptchaRoutes(app: Express) {
  // Verify reCAPTCHA
  app.post("/auth/verify-recaptcha", async (req: Request, res: Response) => {
    const { token, action } = req.body;
    const result = await verifyRecaptcha(token, action);
    res.json({ score: result });
  });
    app.options("/auth/verify-recaptcha", (req: Request, res: Response) => {
    res.status(204).end();
  });
}
