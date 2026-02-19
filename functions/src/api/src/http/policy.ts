import type { Express } from "express";
import { corsify } from "../utils.js";

export function mountPhoneCheckRoutes(app: Express) {

  app.options("/auth/check-email-or-phone", (req, res) => {
    corsify(res);
    res.status(204).end();
  });
}
