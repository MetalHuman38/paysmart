export class GetSecuritySettings {
    repo;
    constructor(repo) {
        this.repo = repo;
    }
    async execute(uid) {
        await this.repo.createIfMissing(uid);
        const settings = await this.repo.get(uid);
        if (!settings) {
            throw new Error("Failed to retrieve security settings");
        }
        return settings;
    }
}
//# sourceMappingURL=GetSecuritySettings.js.map