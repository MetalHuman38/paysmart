export class ProductUpdateCampaignService {
    campaignRepository;
    preferencesRepository;
    notificationDelivery;
    mailer;
    subscriptionService;
    userRepository;
    auth;
    productUpdatesUrl;
    constructor(campaignRepository, preferencesRepository, notificationDelivery, mailer, subscriptionService, userRepository, auth, productUpdatesUrl) {
        this.campaignRepository = campaignRepository;
        this.preferencesRepository = preferencesRepository;
        this.notificationDelivery = notificationDelivery;
        this.mailer = mailer;
        this.subscriptionService = subscriptionService;
        this.userRepository = userRepository;
        this.auth = auth;
        this.productUpdatesUrl = productUpdatesUrl;
    }
    async createCampaign(input) {
        validateCampaignInput(input);
        return this.campaignRepository.createCampaign(input);
    }
    async dispatchCampaign(campaignId) {
        if (!campaignId.trim()) {
            throw new Error("Missing campaignId");
        }
        await this.campaignRepository.dispatchCampaign(campaignId);
    }
    async processReadyCampaign(campaignId) {
        const campaign = await this.campaignRepository.claimReadyCampaign(campaignId);
        if (!campaign) {
            return;
        }
        const stats = {
            inboxRecipients: 0,
            pushEligibleInstallations: 0,
            pushDelivered: 0,
            pushFailed: 0,
            emailRecipients: 0,
            emailDelivered: 0,
            emailFailed: 0,
        };
        try {
            const pushRecipients = campaign.sendPush
                ? dedupeRecipients(await this.preferencesRepository.listPushProductUpdateRecipients())
                : [];
            for (const recipient of pushRecipients) {
                const delivery = await this.campaignRepository.getDeliverySnapshot(campaign.campaignId, recipient.uid);
                if (delivery.notificationSentAtMs) {
                    continue;
                }
                try {
                    const result = await this.notificationDelivery.deliverToUser(recipient.uid, buildCampaignNotification(campaign), { sendPush: campaign.sendPush });
                    stats.inboxRecipients += 1;
                    stats.pushEligibleInstallations += result.pushEligibleInstallations;
                    stats.pushDelivered += result.pushDelivered;
                    stats.pushFailed += result.pushFailed;
                    await this.campaignRepository.markNotificationDelivered(campaign.campaignId, recipient.uid, result.notificationId);
                }
                catch (error) {
                    stats.pushFailed += 1;
                    await this.campaignRepository.markNotificationFailed(campaign.campaignId, recipient.uid, error instanceof Error ? error.message : "Notification delivery failed");
                }
            }
            const emailRecipients = campaign.sendEmail
                ? dedupeRecipients(await this.preferencesRepository.listEmailProductUpdateRecipients())
                : [];
            for (const recipient of emailRecipients) {
                const delivery = await this.campaignRepository.getDeliverySnapshot(campaign.campaignId, recipient.uid);
                if (delivery.emailSentAtMs) {
                    continue;
                }
                const audience = await this.resolveAudience(recipient.uid);
                if (!audience.email) {
                    await this.campaignRepository.markEmailFailed(campaign.campaignId, recipient.uid, "Missing recipient email");
                    stats.emailFailed += 1;
                    continue;
                }
                const unsubscribeUrl = this.subscriptionService.buildUnsubscribeUrl({
                    uid: recipient.uid,
                    topic: "product_updates",
                    email: audience.email,
                    campaignId: campaign.campaignId,
                });
                stats.emailRecipients += 1;
                try {
                    await this.mailer.send({
                        kind: "product_update",
                        to: audience.email,
                        subject: campaign.subject,
                        title: campaign.title,
                        summary: campaign.summary,
                        body: buildCampaignEmailBody(campaign),
                        highlights: campaign.highlights,
                        ctaLabel: campaign.ctaLabel || "Read the update",
                        ctaUrl: campaign.ctaUrl || this.productUpdatesUrl,
                        unsubscribeUrl,
                        locale: recipient.preferredLocale || undefined,
                        campaignId: campaign.campaignId,
                        recipientName: audience.displayName,
                    });
                    await this.campaignRepository.markEmailDelivered(campaign.campaignId, recipient.uid, audience.email);
                    stats.emailDelivered += 1;
                }
                catch (error) {
                    stats.emailFailed += 1;
                    await this.campaignRepository.markEmailFailed(campaign.campaignId, recipient.uid, error instanceof Error ? error.message : "Email delivery failed");
                }
            }
            await this.campaignRepository.markSent(campaign.campaignId, stats);
        }
        catch (error) {
            const message = error instanceof Error ? error.message : "Product update campaign failed";
            await this.campaignRepository.markFailed(campaign.campaignId, message, stats);
            throw error;
        }
    }
    async resolveAudience(uid) {
        const profile = await this.userRepository.getById(uid);
        const email = profile?.email?.trim().toLowerCase();
        const displayName = profile?.displayName?.trim();
        if (email) {
            return {
                email,
                displayName: displayName || undefined,
            };
        }
        try {
            const authUser = await this.auth.getUser(uid);
            return {
                email: authUser.email?.trim().toLowerCase(),
                displayName: authUser.displayName?.trim() || undefined,
            };
        }
        catch {
            return {
                email: undefined,
                displayName: displayName || undefined,
            };
        }
    }
}
function validateCampaignInput(input) {
    if (!input.title?.trim()) {
        throw new Error("Campaign title is required");
    }
    if (!input.summary?.trim()) {
        throw new Error("Campaign summary is required");
    }
}
function dedupeRecipients(recipients) {
    const seen = new Set();
    const unique = [];
    for (const recipient of recipients) {
        const uid = recipient.uid.trim();
        if (!uid || seen.has(uid)) {
            continue;
        }
        seen.add(uid);
        unique.push({
            uid,
            preferredLocale: recipient.preferredLocale,
        });
    }
    return unique;
}
function buildCampaignNotification(campaign) {
    return {
        notificationId: `product_update_${campaign.campaignId}`,
        type: "product_update_campaign",
        channel: "product_updates",
        title: campaign.title,
        body: campaign.summary,
        deepLink: campaign.ctaUrl || null,
        metadata: {
            campaignId: campaign.campaignId,
            area: campaign.area || null,
            releaseStatus: campaign.releaseStatus || null,
        },
    };
}
function buildCampaignEmailBody(campaign) {
    const introParts = [
        campaign.area ? `Area: ${campaign.area}.` : "",
        campaign.releaseStatus ? `Status: ${campaign.releaseStatus}.` : "",
    ].filter(Boolean);
    const sections = [];
    if (introParts.length > 0) {
        sections.push(introParts.join(" "));
    }
    if (campaign.body?.trim()) {
        sections.push(campaign.body.trim());
    }
    if (sections.length === 0) {
        sections.push("We have shipped a new PaySmart release and collected the important highlights for you.");
    }
    return sections.join("\n\n");
}
//# sourceMappingURL=productUpdateCampaignService.js.map