import { publicProductUpdatesFeedHandler } from "../handlers/publicProductUpdatesFeed.js";
export function mountPublicRoutes(app) {
    app.get("/data/product-updates.json", publicProductUpdatesFeedHandler);
}
//# sourceMappingURL=public.route.js.map