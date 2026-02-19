import cors from "cors";
import { SECURITY } from "./globals.js";
// CORS middleware configuration
export const corsMiddleware = cors({
    origin(origin, cb) {
        // Allow same-origin / server-to-server / curl (no origin)
        if (!origin)
            return cb(null, true);
        if (SECURITY.allowedOrigins.includes(origin)) {
            return cb(null, true);
        }
        return cb(new Error(`CORS: Origin not allowed: ${origin}`));
    },
    credentials: SECURITY.allowCredentials,
    methods: ["GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"],
    allowedHeaders: [
        "Origin",
        "X-Requested-With",
        "Content-Type",
        "Accept",
        "Authorization",
        "X-Firebase-AppCheck"
    ],
    optionsSuccessStatus: 204,
    preflightContinue: false
});
// Optional helper if you need to test an origin ad hoc
export const isOriginAllowed = (req) => {
    const origin = req.headers.origin;
    return !origin || SECURITY.allowedOrigins.includes(origin);
};
//# sourceMappingURL=cors.js.map