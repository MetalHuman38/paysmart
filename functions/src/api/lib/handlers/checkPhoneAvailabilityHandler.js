import { corsify } from "../utils.js";
import { apiContainer } from "../infrastructure/di/apiContainer.js";
export async function checkPhoneAvailabilityHandler(req, res) {
    corsify(res);
    try {
        const rawPhone = (req.body?.phoneNumber ?? req.query?.phoneNumber ?? "").toString();
        const { checkPhoneAvailability } = apiContainer();
        const result = await checkPhoneAvailability.execute(rawPhone);
        if (!result.available) {
            return res.status(409).json({
                available: false,
                error: {
                    code: "already-exists",
                    message: "Phone number is already registered",
                },
            });
        }
        return res.status(200).json({ available: true });
    }
    catch (err) {
        if (err.message?.includes("E.164")) {
            return res.status(400).json({
                error: {
                    code: "invalid-argument",
                    message: err.message,
                },
            });
        }
        if (err.message?.includes("Missing phone")) {
            return res.status(400).json({
                error: {
                    code: "invalid-argument",
                    message: err.message,
                },
            });
        }
        console.error("checkPhoneAvailability error:", err);
        return res.status(500).json({
            error: {
                code: "internal",
                message: "Unable to check phone availability",
            },
        });
    }
}
//# sourceMappingURL=checkPhoneAvailabilityHandler.js.map