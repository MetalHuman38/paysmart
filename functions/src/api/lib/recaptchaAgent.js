import { RecaptchaEnterpriseServiceClient } from "@google-cloud/recaptcha-enterprise";
let client = null;
function getClient() {
    if (!client) {
        // Lazy init avoids deployment-time import stalls during function discovery.
        client = new RecaptchaEnterpriseServiceClient();
    }
    return client;
}
function getProjectId() {
    return (process.env.GOOGLE_CLOUD_PROJECT ||
        process.env.GCLOUD_PROJECT ||
        process.env.GCP_PROJECT ||
        "").trim();
}
function getSiteKey() {
    return (process.env.RECAPTCHA_SITE_KEY || process.env.API_KEY || "").trim();
}
export async function verifyRecaptcha(token, action) {
    const normalizedToken = token?.trim();
    const normalizedAction = action?.trim();
    if (!normalizedToken) {
        throw new Error("Missing reCAPTCHA token");
    }
    if (!normalizedAction) {
        throw new Error("Missing reCAPTCHA action");
    }
    const projectId = getProjectId();
    const siteKey = getSiteKey();
    if (!projectId) {
        throw new Error("Missing GOOGLE_CLOUD_PROJECT for reCAPTCHA");
    }
    if (!siteKey) {
        throw new Error("Missing RECAPTCHA_SITE_KEY");
    }
    const recaptcha = getClient();
    const projectPath = recaptcha.projectPath(projectId);
    const [assessment] = await recaptcha.createAssessment({
        parent: projectPath,
        assessment: {
            event: {
                token: normalizedToken,
                siteKey,
                expectedAction: normalizedAction,
            },
        },
    });
    if (!assessment.tokenProperties?.valid) {
        throw new Error("Invalid reCAPTCHA token");
    }
    if (assessment.tokenProperties.action !== normalizedAction) {
        throw new Error("Action mismatch");
    }
    const score = assessment.riskAnalysis?.score ?? 0;
    const minScoreRaw = Number(process.env.RECAPTCHA_MIN_SCORE ?? "0.5");
    const minScore = Number.isFinite(minScoreRaw) ? minScoreRaw : 0.5;
    if (score < minScore) {
        throw new Error("Low confidence request");
    }
    return score;
}
//# sourceMappingURL=recaptchaAgent.js.map