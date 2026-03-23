import { initDeps } from "../dependencies.js";
import { FirestoreProductUpdateCampaignRepository } from "../infrastructure/firestore/FirestoreProductUpdateCampaignRepository.js";
export async function adminDispatchProductUpdateCampaignHandler(req, res) {
    try {
        const campaignId = String(req.params.campaignId || "").trim();
        if (!campaignId) {
            return res.status(400).json({ error: "Missing campaignId" });
        }
        const { firestore } = initDeps();
        const repo = new FirestoreProductUpdateCampaignRepository(firestore);
        await repo.dispatchCampaign(campaignId);
        return res.status(200).json({
            ok: true,
            campaignId,
            status: "ready",
        });
    }
    catch (error) {
        console.error("adminDispatchProductUpdateCampaignHandler failed", error);
        return res.status(500).json({ error: "Internal error" });
    }
}
//# sourceMappingURL=adminDispatchProductUpdateCampaign.js.map