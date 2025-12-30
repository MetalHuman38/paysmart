// C:\Users\Metal\voltpay\functions\src\lib\http\routes\health.ts
import { Router } from "express";
import { APP } from "../config/globals.js";

// Mount health check routes
export function mountHealthRoutes(app: Router) {
  app.get("/health", (_req, res) => {
    res.status(200).json({
      ok: true,
      service: APP.name,
      env: APP.env,
      time: new Date().toISOString()
    });
  });

  app.get("/ping", (_req, res) => {
    res.type("text").send("pong");
  });
}
