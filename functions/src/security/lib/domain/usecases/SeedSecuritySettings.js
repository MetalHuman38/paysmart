export class SeedSecuritySettingsOnUserCreate {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        if (!uid) {
            throw new Error("UID is required");
        }
        await this.repo.createIfMissing(uid);
    }
}
//# sourceMappingURL=SeedSecuritySettings.js.map