export class ConsoleMailer {
    async send(message) {
        console.log(`[DEV] ConsoleMailer ${message.kind} -> ${message.to}`);
    }
    async sendVerificationEmail(input) {
        await this.send({
            kind: "email_verification",
            to: input.to,
            verificationLink: input.verificationLink,
            locale: input.locale,
        });
    }
}
//# sourceMappingURL=consoleMailer.js.map