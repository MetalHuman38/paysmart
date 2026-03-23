const MAX_MULTICAST_TOKENS = 500;
const INVALID_TOKEN_ERROR_CODES = new Set([
    "messaging/invalid-registration-token",
    "messaging/registration-token-not-registered",
]);
export class NotificationDeliveryService {
    notifications;
    messaging;
    constructor(notifications, messaging) {
        this.notifications = notifications;
        this.messaging = messaging;
    }
    async deliverToUser(uid, input, options = {}) {
        const notificationId = await this.notifications.createInboxNotification(uid, input);
        const sendPush = options.sendPush !== false;
        if (!sendPush) {
            return {
                notificationId,
                pushEligibleInstallations: 0,
                pushDelivered: 0,
                pushFailed: 0,
            };
        }
        const installations = dedupeInstallations(await this.notifications.listPushInstallations(uid));
        if (installations.length === 0) {
            return {
                notificationId,
                pushEligibleInstallations: 0,
                pushDelivered: 0,
                pushFailed: 0,
            };
        }
        let pushDelivered = 0;
        let pushFailed = 0;
        const invalidInstallationIds = [];
        for (const chunk of chunkInstallations(installations, MAX_MULTICAST_TOKENS)) {
            const response = await this.messaging.sendEachForMulticast(buildMulticastMessage(chunk, notificationId, input));
            pushDelivered += response.successCount;
            pushFailed += response.failureCount;
            response.responses.forEach((result, index) => {
                if (isInvalidTokenResponse(result)) {
                    invalidInstallationIds.push(chunk[index].installationId);
                }
            });
        }
        if (invalidInstallationIds.length > 0) {
            await this.notifications.deleteInstallations(uid, invalidInstallationIds);
        }
        return {
            notificationId,
            pushEligibleInstallations: installations.length,
            pushDelivered,
            pushFailed,
        };
    }
}
function buildMulticastMessage(installations, notificationId, input) {
    const channel = normalizeChannel(input.channel);
    const metadataJson = input.metadata && Object.keys(input.metadata).length > 0 ?
        JSON.stringify(input.metadata) :
        "";
    return {
        tokens: installations.map((item) => item.fcmToken),
        notification: {
            title: input.title.trim(),
            body: input.body.trim(),
        },
        data: {
            notificationId,
            type: input.type.trim() || "general",
            channel,
            title: input.title.trim(),
            body: input.body.trim(),
            deepLink: input.deepLink?.trim() || "",
            metadataJson,
        },
        android: {
            priority: "high",
            notification: {
                channelId: resolveAndroidChannelId(channel),
            },
        },
    };
}
function dedupeInstallations(installations) {
    const seenTokens = new Set();
    const unique = [];
    for (const installation of installations) {
        if (!installation.fcmToken || seenTokens.has(installation.fcmToken)) {
            continue;
        }
        seenTokens.add(installation.fcmToken);
        unique.push(installation);
    }
    return unique;
}
function chunkInstallations(installations, size) {
    const chunks = [];
    for (let index = 0; index < installations.length; index += size) {
        chunks.push(installations.slice(index, index + size));
    }
    return chunks;
}
function isInvalidTokenResponse(response) {
    const code = response.error?.code?.trim().toLowerCase();
    return response.success === false && !!code && INVALID_TOKEN_ERROR_CODES.has(code);
}
function normalizeChannel(rawChannel) {
    const channel = rawChannel.trim().toLowerCase();
    if (channel === "product_updates" || channel === "app_updates") {
        return channel;
    }
    return "account_updates";
}
function resolveAndroidChannelId(channel) {
    switch (channel) {
        case "product_updates":
            return "paysmart.product_updates";
        case "app_updates":
            return "paysmart.app_updates";
        default:
            return "paysmart.account_updates";
    }
}
//# sourceMappingURL=notificationDeliveryService.js.map