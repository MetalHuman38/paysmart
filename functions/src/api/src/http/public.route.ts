import type { Express } from "express";
import { publicProductUpdatesFeedHandler } from "../handlers/publicProductUpdatesFeed.js";

export function mountPublicRoutes(app: Express) {
  app.get("/data/product-updates.json", publicProductUpdatesFeedHandler);
}
