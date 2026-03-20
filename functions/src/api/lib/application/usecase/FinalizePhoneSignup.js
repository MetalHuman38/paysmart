import { UpsertVerifiedPhoneUser } from "./UpsertVerifiedPhoneUser.js";
export class FinalizePhoneSignup {
    securityRepo;
    userRepo;
    constructor(securityRepo, userRepo) {
        this.securityRepo = securityRepo;
        this.userRepo = userRepo;
    }
    async execute(user) {
        const upserter = new UpsertVerifiedPhoneUser(this.securityRepo, this.userRepo);
        await upserter.execute({
            user,
            auditEvent: "phone_signup_finalized",
        });
    }
}
//# sourceMappingURL=FinalizePhoneSignup.js.map