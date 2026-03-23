import { initDeps } from "../dependencies.js";
import { FirestoreNotificationPreferencesRepository } from "../infrastructure/firestore/FirestoreNotificationPreferencesRepository.js";
import { EmailSubscriptionService } from "../services/emailSubscriptionService.js";
export async function emailUnsubscribeHandler(req, res) {
    try {
        const rawToken = typeof req.query.token === "string" ? req.query.token.trim() : "";
        if (!rawToken) {
            return renderUnsubscribeHtml(res.status(400), "Invalid unsubscribe link", "This unsubscribe link is missing the required token.");
        }
        const { firestore, getConfig } = initDeps();
        const config = getConfig();
        const subscriptionService = new EmailSubscriptionService(config.getEmailUnsubscribeSecret(), new URL("/email/unsubscribe", config.getPublicSiteOrigin()).toString());
        const payload = subscriptionService.verifyToken(rawToken);
        const preferencesRepo = new FirestoreNotificationPreferencesRepository(firestore);
        if (payload.topic === "product_updates") {
            await preferencesRepo.unsubscribeProductUpdates(payload.uid);
        }
        return renderUnsubscribeHtml(res.status(200), "You have been unsubscribed", "PaySmart will stop sending you product update emails to this account.");
    }
    catch (error) {
        console.error("emailUnsubscribeHandler failed", error);
        return renderUnsubscribeHtml(res.status(400), "Invalid unsubscribe link", "This unsubscribe link is invalid or has already expired.");
    }
}
function renderUnsubscribeHtml(res, title, body) {
    return res
        .type("html")
        .send(`<!doctype html>
<html lang="en">
  <head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${escapeHtml(title)}</title>
    <style>
      body { margin:0; font-family: Arial, sans-serif; background:#F4F6F5; color:#11211C; }
      .wrap { max-width: 560px; margin: 64px auto; padding: 0 20px; }
      .card { background:#fff; border:1px solid #DCE4E1; border-radius:24px; padding:32px; }
      .eyebrow { display:inline-block; padding:6px 12px; border-radius:999px; background:#E8F7F0; color:#0F6B4F; font-size:12px; font-weight:700; letter-spacing:0.04em; text-transform:uppercase; }
      h1 { font-size:32px; line-height:1.15; margin:18px 0 12px; }
      p { font-size:16px; line-height:1.7; margin:0; color:#42524C; }
    </style>
  </head>
  <body>
    <div class="wrap">
      <div class="card">
        <div class="eyebrow">PaySmart email preferences</div>
        <h1>${escapeHtml(title)}</h1>
        <p>${escapeHtml(body)}</p>
      </div>
    </div>
  </body>
</html>`);
}
function escapeHtml(value) {
    return value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
//# sourceMappingURL=emailUnsubscribe.js.map