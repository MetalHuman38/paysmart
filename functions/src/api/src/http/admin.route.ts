import type { Express } from "express";
import { adminUserActivityFeedHandler, adminUserSessionSnapshotHandler } from "../handlers/adminMonitoring.js";
import { requireAdmin } from "../middleware/requireAdmin.js";
import { corsify } from "../utils.js";

export function mountAdminRoutes(app: Express) {
  app.get("/admin/ping", requireAdmin, (_req, res) => {
    return res.status(200).json({
      ok: true,
      scope: "admin",
      observedAtMs: Date.now(),
    });
  });
  app.options("/admin/ping", (_req, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get(
    "/admin/monitor/users/:uid/session",
    requireAdmin,
    adminUserSessionSnapshotHandler
  );
  app.options("/admin/monitor/users/:uid/session", (_req, res) => {
    corsify(res);
    res.status(204).end();
  });

  app.get(
    "/admin/monitor/users/:uid/activity",
    requireAdmin,
    adminUserActivityFeedHandler
  );
  app.options("/admin/monitor/users/:uid/activity", (_req, res) => {
    corsify(res);
    res.status(204).end();
  });
}
