import { RecaptchaEnterpriseServiceClient } from
  "@google-cloud/recaptcha-enterprise";

const client = new RecaptchaEnterpriseServiceClient();

export async function verifyRecaptcha(token: string, action: string) {
  const projectPath = client.projectPath("paysmart-7ee79");

  const [assessment] = await client.createAssessment({
    parent: projectPath,
    assessment: {
      event: {
        token,
        siteKey: process.env.API_KEY || "API_KEY",
        expectedAction: action,
      },
    },
  });

  if (!assessment.tokenProperties?.valid) {
    throw new Error("Invalid reCAPTCHA token");
  }

  if (assessment.tokenProperties.action !== action) {
    throw new Error("Action mismatch");
  }

  const score = assessment.riskAnalysis?.score ?? 0;

  if (score < 0.5) {
    throw new Error("Low confidence request");
  }

  return score;
}
