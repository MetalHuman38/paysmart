import { initializeApp, getApps } from "firebase-admin/app";
export function initAdmin() {
    if (getApps().length === 0) {
        initializeApp();
    }
}
//# sourceMappingURL=bootstrap.js.map