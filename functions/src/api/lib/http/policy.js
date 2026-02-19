import { corsify } from "../utils.js";
export function mountPhoneCheckRoutes(app) {
    app.options("/auth/check-email-or-phone", (req, res) => {
        corsify(res);
        res.status(204).end();
    });
}
//# sourceMappingURL=policy.js.map