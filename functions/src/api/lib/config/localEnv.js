import { config as dotenvConfig } from "dotenv";
let loaded = false;
export function ensureLocalEnvLoaded() {
    if (loaded) {
        return;
    }
    loaded = true;
    dotenvConfig();
}
//# sourceMappingURL=localEnv.js.map