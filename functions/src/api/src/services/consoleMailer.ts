// src/services/consoleMailer.ts
import { Mailer, VerificationEmailInput } from "./mailer.js";

export class ConsoleMailer implements Mailer {
  async sendVerificationEmail(input: VerificationEmailInput): Promise<void> {
  }
}
