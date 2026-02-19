import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";

export class GetSecuritySettings {
    constructor(
        private readonly repo: SecuritySettingsRepository
    ) {}
    async execute(uid: string) {
        await this.repo.createIfMissing(uid);
        const settings = await this.repo.get(uid);

        if (!settings) {
            throw new Error("Failed to retrieve security settings");
        }
        return settings;
    }
}