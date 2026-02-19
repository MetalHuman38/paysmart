// src/services/mailer.ts
export interface Mailer {
  sendVerificationEmail(input: VerificationEmailInput): Promise<void>;
}

export type VerificationEmailInput = {
  to: string;
  link: string;
  locale?: string;
};
