import { initDeps } from "../dependencies.js";
import { FirestoreProductUpdateCampaignRepository, } from "../infrastructure/firestore/FirestoreProductUpdateCampaignRepository.js";
export async function publicProductUpdatesFeedHandler(req, res) {
    try {
        const { firestore } = initDeps();
        const repository = new FirestoreProductUpdateCampaignRepository(firestore);
        const limit = parseLimit(req.query.limit);
        const campaigns = await repository.listPublishedCampaigns(limit);
        const payload = campaigns.map(mapCampaignToPublicProductUpdateFeedItem);
        res.setHeader("Cache-Control", "public, max-age=300");
        res.setHeader("Content-Type", "application/json; charset=utf-8");
        return res.status(200).json(payload);
    }
    catch (error) {
        console.error("publicProductUpdatesFeedHandler failed", error);
        res.setHeader("Cache-Control", "no-cache");
        return res.status(500).json({ error: "Could not load product updates" });
    }
}
export function mapCampaignToPublicProductUpdateFeedItem(campaign) {
    return {
        campaignId: campaign.campaignId,
        date: formatDate(campaign.sentAtMs),
        area: campaign.area || "General",
        status: campaign.releaseStatus || "Shipped",
        title: campaign.title,
        summary: campaign.summary,
        highlights: campaign.highlights,
        ...(campaign.body ? { body: campaign.body } : {}),
        ...(campaign.ctaLabel ? { ctaLabel: campaign.ctaLabel } : {}),
        ...(campaign.ctaUrl ? { ctaUrl: campaign.ctaUrl } : {}),
    };
}
function formatDate(value) {
    return new Date(value).toISOString().slice(0, 10);
}
function parseLimit(raw) {
    const value = typeof raw === "string" && raw.trim() ? Number.parseInt(raw.trim(), 10) : Number.NaN;
    if (!Number.isFinite(value)) {
        return 20;
    }
    return Math.max(1, Math.min(value, 50));
}
//# sourceMappingURL=publicProductUpdatesFeed.js.map