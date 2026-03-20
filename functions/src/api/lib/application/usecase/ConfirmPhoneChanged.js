import { UpsertVerifiedPhoneUser } from "./UpsertVerifiedPhoneUser.js";
export class ConfirmPhoneChanged {
    authService;
    securityRepo;
    userRepo;
    constructor(authService, securityRepo, userRepo) {
        this.authService = authService;
        this.securityRepo = securityRepo;
        this.userRepo = userRepo;
    }
    async execute(uid, phoneNumber) {
        const authUser = await this.authService.getUser(uid);
        const upserter = new UpsertVerifiedPhoneUser(this.securityRepo, this.userRepo);
        await upserter.execute({
            user: authUser,
            expectedPhoneNumber: phoneNumber.trim(),
            auditEvent: "phone_changed_confirmed",
        });
    }
}
//# sourceMappingURL=ConfirmPhoneChanged.js.map