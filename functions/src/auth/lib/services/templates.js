// src/services/templates/verificationEmail.ts
export function verificationEmailTemplate(link) {
    return {
        subject: "Verify your email",
        html: `
      <p>Welcome 👋</p>
      <p>Please verify your email to continue:</p>
      <p>
        <a href="${link}" style="
          display:inline-block;
          padding:10px 16px;
          background:#000;
          color:#fff;
          text-decoration:none;
          border-radius:6px;
        ">
          Verify email
        </a>
      </p>
      <p>If you didn’t request this, you can safely ignore this email.</p>
    `,
    };
}
//# sourceMappingURL=templates.js.map