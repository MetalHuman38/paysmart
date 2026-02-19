// infrastructure/di/apiContainer.ts
import { authContainer } from "./authContainer.js";
import { emailContainer } from "./emailContainer.js";
export function apiContainer() {
    return {
        ...authContainer(),
        ...emailContainer(),
    };
}
//# sourceMappingURL=apiContainer.js.map