// src/services/consoleMailer.ts
import { Mailer, VerificationEmailInput } from "./mailer.js";

export class ConsoleMailer implements Mailer {
  async sendVerificationEmail(input: VerificationEmailInput): Promise<void> {
    console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    console.log("📧 VERIFICATION EMAIL (DEV)");
    console.log("To:", input.to);
    console.log("Link:", input.link);
    console.log("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
  }
}
