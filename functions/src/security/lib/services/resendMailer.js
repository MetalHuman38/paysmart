// src/services/resendMailer.ts
import { Resend } from "resend";
import { verificationEmailTemplate } from "../services/templates.js";
export class ResendMailer {
    resend;
    from;
    constructor(apiKey, from) {
        if (!apiKey) {
            throw new Error("ResendMailer: API key is required");
        }
        if (!from || !from.includes("<") || !from.includes(">")) {
            throw new Error("ResendMailer: 'from' must be in the format 'Name <email@domain>'");
        }
        this.resend = new Resend(apiKey);
        this.from = from;
    }
    async sendVerificationEmail({ to, link, }) {
        if (!to)
            throw new Error("ResendMailer: recipient email is required");
        if (!link)
            throw new Error("ResendMailer: verification link is required");
        const { subject, html } = verificationEmailTemplate(link);
        const { error } = await this.resend.emails.send({
            from: this.from,
            to,
            subject,
            html,
            tags: [
                { name: "type", value: "email_verification" },
            ],
        });
        if (error) {
            throw new Error(`ResendMailer failed: ${error.message}`);
        }
    }
}
//# sourceMappingURL=resendMailer.js.map