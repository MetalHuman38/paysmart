export type VerificationEmailInput = {
  kind: "email_verification";
  to: string;
  verificationLink: string;
  locale?: string;
};

export type ProductUpdateEmailInput = {
  kind: "product_update";
  to: string;
  subject?: string;
  title: string;
  summary: string;
  body?: string;
  highlights?: string[];
  ctaLabel?: string;
  ctaUrl?: string;
  unsubscribeUrl: string;
  locale?: string;
  campaignId: string;
  recipientName?: string;
};

export type SecurityAlertEmailInput = {
  kind: "security_alert";
  to: string;
  subject?: string;
  title: string;
  summary: string;
  body: string;
  ctaLabel?: string;
  ctaUrl?: string;
  locale?: string;
};

export type TransactionalStatusEmailInput = {
  kind: "transactional_status";
  to: string;
  subject?: string;
  title: string;
  summary: string;
  body: string;
  ctaLabel?: string;
  ctaUrl?: string;
  locale?: string;
};

export type MailMessage =
  | VerificationEmailInput
  | ProductUpdateEmailInput
  | SecurityAlertEmailInput
  | TransactionalStatusEmailInput;

export interface Mailer {
  send(message: MailMessage): Promise<void>;
  sendVerificationEmail(input: Omit<VerificationEmailInput, "kind">): Promise<void>;
}
