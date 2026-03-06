import express from "express";
import helmet from "helmet";
import bodyParser from "body-parser";
import compression from "compression";
import {
  registerApiRoutes,
  registerFallbackRoutes,
  registerPostBodyParserRoutes,
  registerPreMiddlewareRoutes,
} from "./registerApiRoutes.js";

export function createApiApp() {
  const app = express();
  app.set("trust proxy", 1); // behind Cloud LB
  registerPreMiddlewareRoutes(app);

  app.use(express.json({ limit: "1mb" }));
  app.use(express.urlencoded({ extended: true }));
  app.use(express.text({ type: "text/*", limit: "1mb" }));
  app.use(helmet());
  app.use(compression());

  registerApiRoutes(app);

  app.use(bodyParser.text({ type: "*/*" }));
  app.use(bodyParser.urlencoded({ extended: false }));
  app.use(bodyParser.json());

  registerPostBodyParserRoutes(app);
  registerFallbackRoutes(app);

  return app;
}
