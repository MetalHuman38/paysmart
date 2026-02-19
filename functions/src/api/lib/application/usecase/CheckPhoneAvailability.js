import { normalizePhone, validateE164, } from "../../domain/policy/PhoneNumberPolicy.js";
export class CheckPhoneAvailability {
    authRepo;
    constructor(authRepo) {
        this.authRepo = authRepo;
    }
    async execute(rawPhone) {
        const phone = normalizePhone(rawPhone);
        validateE164(phone);
        const existingUser = await this.authRepo.getUserByPhone(phone);
        return { available: !existingUser };
    }
}
//# sourceMappingURL=CheckPhoneAvailability.js.map