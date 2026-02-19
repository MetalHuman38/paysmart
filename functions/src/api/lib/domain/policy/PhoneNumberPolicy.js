const E164_REGEX = /^\+[1-9]\d{6,14}$/;
export function normalizePhone(input) {
    return input.replace(/[^\d+]/g, "");
}
export function validateE164(phone) {
    if (!phone) {
        throw new Error("Missing phone number");
    }
    if (!E164_REGEX.test(phone)) {
        throw new Error("Phone number must be in E.164 format");
    }
}
//# sourceMappingURL=PhoneNumberPolicy.js.map