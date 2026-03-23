import { Resend } from "resend";
import { Mailer, MailMessage } from "./mailer.js";
import { renderEmailTemplate } from "./templates.js";

export class ResendMailer implements Mailer {
  private readonly resend: Resend;
  private readonly from: string;

  constructor(apiKey: string, from: string) {
    if (!apiKey) {
      throw new Error("ResendMailer: API key is required");
    }
    const normalizedFrom = normalizeLegacySender(from);
    if (!normalizedFrom || !normalizedFrom.includes("<") || !normalizedFrom.includes(">")) {
      throw new Error(
        "ResendMailer: 'from' must be in the format 'Name <email@domain>'"
      );
    }

    this.resend = new Resend(apiKey);
    this.from = normalizedFrom;
  }

  async send(message: MailMessage): Promise<void> {
    const to = message.to?.trim();
    if (!to) throw new Error("ResendMailer: recipient email is required");

    const { subject, html, text } = renderEmailTemplate(message);
    const tags: Array<{ name: string; value: string }> = [
      { name: "type", value: message.kind },
    ];
    if (message.kind === "product_update") {
      tags.push({ name: "campaign_id", value: message.campaignId });
    }

    const { error } = await this.resend.emails.send({
      from: this.from,
      to,
      subject,
      html,
      text,
      tags,
    });

    if (error) {
      throw new Error(`ResendMailer failed: ${error.message}`);
    }
  }

  async sendVerificationEmail(input: {
    to: string;
    verificationLink: string;
    locale?: string;
  }): Promise<void> {
    await this.send({
      kind: "email_verification",
      to: input.to,
      verificationLink: input.verificationLink,
      locale: input.locale,
    });
  }
}

function normalizeLegacySender(from: string): string {
  return from.replace(
    /<([^<>@\s]+)@metalbrain\.net>/i,
    "<$1@pay-smart.net>"
  );
}
