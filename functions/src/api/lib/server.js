import { createApiApp } from "./bootstrap/createApiApp.js";
let cachedApp = null;
export function buildApp() {
    if (!cachedApp) {
        cachedApp = createApiApp();
    }
    return cachedApp;
}
//# sourceMappingURL=server.js.map