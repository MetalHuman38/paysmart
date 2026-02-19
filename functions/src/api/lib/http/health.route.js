import { APP } from "../config/globals.js";
import { projectID } from "firebase-functions/params";
// Mount health check routes
export function mountHealthRoutes(app) {
    app.get("/health", (_req, res) => {
        res.status(200).json({
            ok: true,
            service: APP.name,
            region: APP.region,
            projectID: projectID.value(),
            env: APP.env,
            time: new Date().toISOString()
        });
    });
    app.get("/ping", (_req, res) => {
        res.type("text").send("pong");
    });
}
//# sourceMappingURL=health.route.js.map