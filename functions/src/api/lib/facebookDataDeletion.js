// src/functions/facebookDataDeletion.ts
import crypto from "crypto";
import { FACEBOOK_APP_SECRET } from "./config/params.js";
import { deleteUserData, getUserDoc, lookupUidByAppScopedId, writeDeletionLog, } from "./services/facebookDeletionHelpers.js";
import { parseSignedRequest } from "./facebookSignedRequest.js";
function corsHeaders() {
    return {
        "Access-Control-Allow-Origin": "*",
        "Access-Control-Allow-Headers": "Content-Type",
    };
}
function readSignedRequest(req) {
    const contentType = (req.headers["content-type"] || "").toLowerCase();
    if (typeof req.body === "string" && req.body.includes(".")) {
        return req.body.trim();
    }
    if (contentType.includes("application/x-www-form-urlencoded")) {
        const body = req.body?.signed_request;
        return typeof body === "string" ? body : null;
    }
    if (typeof req.body?.signed_request === "string") {
        return req.body.signed_request;
    }
    return null;
}
export async function facebookDataDeletionHandler(req, res) {
    if (req.method === "OPTIONS") {
        return res.status(204).set(corsHeaders()).send("");
    }
    if (req.method !== "POST") {
        return res
            .status(405)
            .set(corsHeaders())
            .json({ error: "Method not allowed" });
    }
    const appSecret = FACEBOOK_APP_SECRET.value();
    if (!appSecret) {
        return res
            .status(500)
            .set(corsHeaders())
            .json({ error: "Server misconfigured: FACEBOOK_APP_SECRET missing" });
    }
    const signedRequest = readSignedRequest(req);
    if (!signedRequest) {
        return res
            .status(400)
            .set(corsHeaders())
            .json({ error: "missing signed_request" });
    }
    let payload;
    let appScopedUserId;
    try {
        payload = parseSignedRequest(signedRequest, appSecret);
        if (!payload.user_id) {
            throw new Error("user_id missing in payload");
        }
        appScopedUserId = payload.user_id;
    }
    catch (err) {
        return res
            .status(400)
            .set(corsHeaders())
            .json({ error: `invalid signed_request: ${err.message}` });
    }
    const uid = await lookupUidByAppScopedId(appScopedUserId);
    const userSnapshot = uid ? await getUserDoc(uid) : null;
    const actionSummary = uid
        ? await deleteUserData(uid, true)
        : { notes: ["no uid mapping found; no action taken"] };
    const confirmationCode = crypto.randomBytes(8).toString("hex");
    const statusUrl = `https://pay-smart.net/data-deletion?code=${confirmationCode}`;
    await writeDeletionLog(appScopedUserId, uid, userSnapshot, actionSummary, confirmationCode);
    // Meta expects text/plain
    return res
        .status(200)
        .set({
        ...corsHeaders(),
        "Content-Type": "text/plain; charset=utf-8",
    })
        .send(JSON.stringify({
        url: statusUrl,
        confirmation_code: confirmationCode,
    }));
}
//# sourceMappingURL=facebookDataDeletion.js.map