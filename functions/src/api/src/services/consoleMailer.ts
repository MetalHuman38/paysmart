import { Mailer, MailMessage } from "./mailer.js";

export class ConsoleMailer implements Mailer {
  async send(message: MailMessage): Promise<void> {
    console.log(
      `[DEV] ConsoleMailer ${message.kind} -> ${message.to}`
    );
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
