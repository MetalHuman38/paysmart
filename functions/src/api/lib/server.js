import express from "express";
import helmet from "helmet";
import bodyParser from "body-parser";
import compression from "compression";
import { mountHealthRoutes } from "./http/health.route.js";
import { mountRecaptchaRoutes } from "./http/recaptcha.route.js";
import { mountAuthPolicyRoutes } from "./http/policy.route.js";
import { mountPhoneCheckRoutes } from "./http/policy.js";
import { facebookDataDeletionHandler } from "./facebookDataDeletion.js";
import { requireAppCheck } from "./config/appcheck.js";
import { checkEmailOrPhone } from "./checkEmailOrPhone.js";
export function buildApp() {
    const app = express();
    app.set("trust proxy", 1); // behind Cloud LB
    app.use(express.json({ limit: "1mb" }));
    app.use(express.urlencoded({ extended: true }));
    app.use(express.text({ type: "text/*", limit: "1mb" }));
    app.use(helmet());
    app.use(compression());
    mountAuthPolicyRoutes(app);
    mountHealthRoutes(app);
    mountRecaptchaRoutes(app);
    mountPhoneCheckRoutes(app);
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
//# sourceMappingURL=server.js.map