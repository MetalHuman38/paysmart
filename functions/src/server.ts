import express from "express";
import helmet from "helmet";
import bodyParser from "body-parser";
import compression from "compression";
import { mountPolicyRoutes } from "./http/policy";
import { mountHealthRoutes } from "./http/health";
import { mountRecaptchaRoutes } from "./http/recaptcha";
import { facebookDataDeletionHandler } from "./facebookDataDeletion";
import { requireAppCheck } from "./config/appcheck";
import { checkEmailOrPhone } from "./auth/checkEmailOrPhone";

export function buildApp() {
    const app = express();
    app.set("trust proxy", 1); // behind Cloud LB
    app.use(express.json({ limit: "1mb" }));
    app.use(express.urlencoded({ extended: true }));
    app.use(express.text({ type: "text/*", limit: "1mb" }));
    app.use(helmet());
    app.use(compression());
    mountHealthRoutes(app);
    mountPolicyRoutes(app);
    mountRecaptchaRoutes(app);

    app.use(bodyParser.text({ type: "*/*" }));
    app.use(bodyParser.urlencoded({ extended: false }));
    app.use(bodyParser.json());

    app.post("/auth/check-email-or-phone", requireAppCheck, checkEmailOrPhone);

    // Routes
    app.post("/facebook/data-deletion", facebookDataDeletionHandler);

    app.use((req, res) => {
    console.error(`404 Not Found: ${req.method} ${req.path}`);
    res.status(404).json({ error: "Not Found" });
});
    return app;
}
