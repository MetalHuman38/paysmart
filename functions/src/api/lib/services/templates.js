export function renderEmailTemplate(message) {
    switch (message.kind) {
        case "email_verification":
            return renderVerificationEmail(message);
        case "product_update":
            return renderProductUpdateEmail(message);
        case "security_alert":
            return renderSecurityAlertEmail(message);
        case "transactional_status":
            return renderTransactionalStatusEmail(message);
    }
}
function renderVerificationEmail(message) {
    return renderBaseEmail({
        subject: "Verify your PaySmart email",
        eyebrow: "Email verification",
        title: "Confirm your email address",
        summary: "Please confirm this email so PaySmart can use it for account updates and recovery.",
        body: `
      <p style="margin:0 0 16px;">Use the button below to verify this email address and return to PaySmart.</p>
      <p style="margin:0;">If you did not request this, you can ignore this email.</p>
    `,
        ctaLabel: "Confirm email",
        ctaUrl: message.verificationLink,
        footer: "<p style=\"margin:0;\">This link opens the PaySmart verification handoff and works best on your Android device.</p>",
    });
}
function renderProductUpdateEmail(message) {
    const greeting = message.recipientName?.trim()
        ? `<p style="margin:0 0 16px;">Hi ${escapeHtml(message.recipientName.trim())},</p>`
        : "";
    const highlights = renderHighlights(message.highlights);
    const footer = `
    <p style="margin:0 0 12px;">You are receiving this because you asked for PaySmart product updates.</p>
    <p style="margin:0;"><a href="${escapeAttribute(message.unsubscribeUrl)}" style="color:#2ED08A;">Unsubscribe from product update emails</a></p>
  `;
    return renderBaseEmail({
        subject: message.subject?.trim() || message.title.trim(),
        eyebrow: "PaySmart product update",
        title: message.title,
        summary: message.summary,
        body: `${greeting}${wrapParagraphs(message.body || "We have shipped a new PaySmart update and wanted you to see the important changes first.")}${highlights}`,
        ctaLabel: message.ctaLabel || "Read the update",
        ctaUrl: message.ctaUrl,
        footer,
    });
}
function renderSecurityAlertEmail(message) {
    return renderBaseEmail({
        subject: message.subject?.trim() || message.title.trim(),
        eyebrow: "PaySmart security alert",
        title: message.title,
        summary: message.summary,
        body: wrapParagraphs(message.body),
        ctaLabel: message.ctaLabel,
        ctaUrl: message.ctaUrl,
        footer: "<p style=\"margin:0;\">If this was not you, review your PaySmart security settings immediately.</p>",
    });
}
function renderTransactionalStatusEmail(message) {
    return renderBaseEmail({
        subject: message.subject?.trim() || message.title.trim(),
        eyebrow: "PaySmart account update",
        title: message.title,
        summary: message.summary,
        body: wrapParagraphs(message.body),
        ctaLabel: message.ctaLabel,
        ctaUrl: message.ctaUrl,
        footer: "<p style=\"margin:0;\">This message relates to your PaySmart account activity.</p>",
    });
}
function renderBaseEmail(input) {
    const button = input.ctaLabel && input.ctaUrl
        ? `
        <p style="margin:24px 0 0;">
          <a href="${escapeAttribute(input.ctaUrl)}" style="
            display:inline-block;
            padding:14px 22px;
            background:#11211C;
            color:#F5FBF8;
            text-decoration:none;
            border-radius:999px;
            font-weight:600;
          ">${escapeHtml(input.ctaLabel)}</a>
        </p>
      `
        : "";
    const footer = input.footer
        ? `<div style="margin-top:32px;color:#6D7A75;font-size:13px;line-height:1.6;">${input.footer}</div>`
        : "";
    return {
        subject: input.subject.trim(),
        html: `
      <div style="margin:0;padding:32px 0;background:#F4F6F5;font-family:'Segoe UI',Arial,sans-serif;color:#11211C;">
        <div style="max-width:620px;margin:0 auto;padding:0 20px;">
          <div style="background:#FFFFFF;border:1px solid #DCE4E1;border-radius:24px;padding:32px;">
            <div style="display:inline-block;padding:6px 12px;border-radius:999px;background:#E8F7F0;color:#0F6B4F;font-size:12px;font-weight:700;letter-spacing:0.04em;text-transform:uppercase;">
              ${escapeHtml(input.eyebrow)}
            </div>
            <h1 style="margin:20px 0 12px;font-size:32px;line-height:1.15;color:#11211C;">${escapeHtml(input.title)}</h1>
            <p style="margin:0 0 20px;font-size:18px;line-height:1.6;color:#42524C;">${escapeHtml(input.summary)}</p>
            <div style="font-size:15px;line-height:1.7;color:#1B2B25;">${input.body}</div>
            ${button}
          </div>
          ${footer}
        </div>
      </div>
    `,
        text: renderPlainTextEmail(input),
    };
}
function renderHighlights(highlights) {
    const items = (highlights || []).map((item) => item.trim()).filter(Boolean);
    if (items.length === 0) {
        return "";
    }
    const renderedItems = items
        .map((item) => `<li style="margin:0 0 8px;">${escapeHtml(item)}</li>`)
        .join("");
    return `
    <div style="margin-top:20px;padding:18px 20px;border-radius:18px;background:#F4F8F6;border:1px solid #E1EAE6;">
      <p style="margin:0 0 10px;font-size:13px;font-weight:700;letter-spacing:0.04em;text-transform:uppercase;color:#0F6B4F;">Highlights</p>
      <ul style="margin:0;padding-left:18px;">${renderedItems}</ul>
    </div>
  `;
}
function wrapParagraphs(raw) {
    return raw
        .split(/\n{2,}/)
        .map((value) => value.trim())
        .filter(Boolean)
        .map((paragraph) => `<p style="margin:0 0 16px;">${escapeHtml(paragraph)}</p>`)
        .join("");
}
function escapeHtml(value) {
    return value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#39;");
}
function escapeAttribute(value) {
    return escapeHtml(value);
}
function renderPlainTextEmail(input) {
    const parts = [
        input.eyebrow,
        "",
        input.title,
        input.summary,
        "",
        htmlToPlainText(input.body),
    ];
    if (input.ctaLabel && input.ctaUrl) {
        parts.push("", `${input.ctaLabel}: ${input.ctaUrl}`);
    }
    if (input.footer) {
        parts.push("", htmlToPlainText(input.footer));
    }
    return parts
        .map((part) => part.trim())
        .filter(Boolean)
        .join("\n\n");
}
function htmlToPlainText(value) {
    return value
        .replace(/<br\s*\/?>/gi, "\n")
        .replace(/<\/p>/gi, "\n\n")
        .replace(/<\/li>/gi, "\n")
        .replace(/<li[^>]*>/gi, "- ")
        .replace(/<[^>]+>/g, "")
        .replace(/&amp;/g, "&")
        .replace(/&lt;/g, "<")
        .replace(/&gt;/g, ">")
        .replace(/&quot;/g, "\"")
        .replace(/&#39;/g, "'")
        .replace(/\n{3,}/g, "\n\n")
        .trim();
}
//# sourceMappingURL=templates.js.map