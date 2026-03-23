import { createHmac, timingSafeEqual } from "crypto";

export type EmailSubscriptionTopic = "product_updates";

type EmailSubscriptionTokenPayload = {
  uid: string;
  topic: EmailSubscriptionTopic;
  email?: string;
  campaignId?: string;
};

export class EmailSubscriptionService {
  constructor(
    private readonly secret: string,
    private readonly unsubscribeBaseUrl: string
  ) {}

  buildUnsubscribeUrl(input: EmailSubscriptionTokenPayload): string {
    const token = this.createToken(input);
    const url = new URL(this.unsubscribeBaseUrl);
    url.searchParams.set("token", token);
    return url.toString();
  }

  createToken(input: EmailSubscriptionTokenPayload): string {
    if (!this.secret.trim()) {
      throw new Error("EMAIL_UNSUBSCRIBE_SECRET is required");
    }
    if (!input.uid.trim()) {
      throw new Error("Missing uid");
    }

    const payload = {
      uid: input.uid.trim(),
      topic: input.topic,
      email: input.email?.trim().toLowerCase() || undefined,
      campaignId: input.campaignId?.trim() || undefined,
    };
    const encodedPayload = Buffer.from(JSON.stringify(payload), "utf8").toString(
      "base64url"
    );
    const signature = signPayload(this.secret, encodedPayload);
    return `${encodedPayload}.${signature}`;
  }

  verifyToken(token: string): EmailSubscriptionTokenPayload {
    const [encodedPayload, signature] = token.split(".");
    if (!encodedPayload || !signature) {
      throw new Error("Invalid unsubscribe token");
    }

    const expectedSignature = signPayload(this.secret, encodedPayload);
    if (
      expectedSignature.length !== signature.length ||
      !timingSafeEqual(
        Buffer.from(expectedSignature, "utf8"),
        Buffer.from(signature, "utf8")
      )
    ) {
      throw new Error("Invalid unsubscribe token");
    }

    const payload = JSON.parse(
      Buffer.from(encodedPayload, "base64url").toString("utf8")
    ) as EmailSubscriptionTokenPayload;

    if (!payload.uid?.trim() || payload.topic !== "product_updates") {
      throw new Error("Invalid unsubscribe token");
    }

    return {
      uid: payload.uid.trim(),
      topic: payload.topic,
      email: payload.email?.trim().toLowerCase() || undefined,
      campaignId: payload.campaignId?.trim() || undefined,
    };
  }
}

function signPayload(secret: string, encodedPayload: string): string {
  return createHmac("sha256", secret.trim())
    .update(encodedPayload)
    .digest("base64url");
}
