import { verifyRecaptcha } from "../recaptchaAgent.js";
export function mountRecaptchaRoutes(app) {
    // Verify reCAPTCHA
    app.post("/auth/verify-recaptcha", async (req, res) => {
        const { token, action } = req.body;
        const result = await verifyRecaptcha(token, action);
        res.json({ score: result });
    });
    app.options("/auth/verify-recaptcha", (req, res) => {
        res.status(204).end();
    });
}
//# sourceMappingURL=recaptcha.route.js.map