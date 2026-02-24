import { corsify } from "../utils.js";
import { getFxQuoteHandler } from "../handlers/getFxQuote.js";
export function mountFxRoutes(app) {
    // Support both paths during client rollout. Some clients call function root + /quotes,
    // while others include /api prefix.
    const paths = ["/quotes", "/api/quotes"];
    for (const path of paths) {
        app.get(path, getFxQuoteHandler);
        app.options(path, (_, res) => {
            corsify(res);
            res.status(204).end();
        });
    }
}
//# sourceMappingURL=fx.route.js.map