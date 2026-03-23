// infrastructure/di/apiContainer.ts
import { authContainer } from "./authContainer.js";
import { emailContainer } from "./emailContainer.js";
import { fxContainer } from "./fxContainer.js";
import { notificationContainer } from "./notificationContainer.js";
export function apiContainer() {
    return {
        ...authContainer(),
        ...emailContainer(),
        ...fxContainer(),
        ...notificationContainer(),
    };
}
//# sourceMappingURL=apiContainer.js.map