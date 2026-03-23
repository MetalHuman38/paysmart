import { notificationContainer } from "../infrastructure/di/notificationContainer.js";
export async function registerNotificationInstallationHandler(req, res) {
    try {
        const uid = req.authSession?.uid?.trim();
        if (!uid) {
            return res.status(401).json({ error: "Session missing" });
        }
        const installationId = asString(req.body?.installationId);
        const fcmToken = asString(req.body?.fcmToken);
        const locale = asOptionalString(req.body?.locale);
        const appVersion = asOptionalString(req.body?.appVersion);
        const notificationsPermissionGranted = asBoolean(req.body?.notificationsPermissionGranted);
        if (!installationId) {
            return res.status(400).json({ error: "Missing installationId" });
        }
        if (!fcmToken) {
            return res.status(400).json({ error: "Missing fcmToken" });
        }
        if (notificationsPermissionGranted == null) {
            return res.status(400).json({
                error: "Missing notificationsPermissionGranted",
            });
        }
        const { notifications } = notificationContainer();
        await notifications.registerInstallation(uid, {
            installationId,
            fcmToken,
            platform: "android",
            locale,
            appVersion,
            notificationsPermissionGranted,
            appCheckAppId: asOptionalString(req.appCheck?.app_id),
        });
        return res.status(200).json({
            ok: true,
            installationId,
            notificationsPermissionGranted,
        });
    }
    catch (error) {
        console.error("registerNotificationInstallationHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
function asString(value) {
    return typeof value === "string" ? value.trim() : "";
}
function asOptionalString(value) {
    const normalized = asString(value);
    return normalized || undefined;
}
function asBoolean(value) {
    if (typeof value === "boolean")
        return value;
    if (typeof value === "string") {
        const normalized = value.trim().toLowerCase();
        if (normalized === "true")
            return true;
        if (normalized === "false")
            return false;
    }
    return null;
}
//# sourceMappingURL=registerNotificationInstallation.js.map