// infrastructure/di/apiContainer.ts
import { authContainer } from "./authContainer.js";
import { emailContainer } from "./emailContainer.js";
import { fxContainer } from "./fxContainer.js";
export function apiContainer() {
    return {
        ...authContainer(),
        ...emailContainer(),
        ...fxContainer(),
    };
}
//# sourceMappingURL=apiContainer.js.map