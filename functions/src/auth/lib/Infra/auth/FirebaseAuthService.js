export class FirebaseAuthService {
    auth;
    constructor(auth) {
        this.auth = auth;
    }
    async getUserByPhone(phoneNumber) {
        try {
            const userRecord = await this.auth.getUserByPhoneNumber(phoneNumber);
            return { uid: userRecord.uid };
        }
        catch (error) {
            const code = error.code;
            if (code === "auth/user-not-found") {
                return null;
            }
            throw error;
        }
    }
    async getUserByEmail(email) {
        try {
            const userRecord = await this.auth.getUserByEmail(email);
            return { uid: userRecord.uid, phoneNumber: userRecord.phoneNumber ?? null };
        }
        catch (error) {
            const code = error.code;
            if (code === "auth/user-not-found") {
                return null;
            }
            throw error;
        }
    }
    async getUserByUid(uid) {
        try {
            const userRecord = await this.auth.getUser(uid);
            return {
                uid: userRecord.uid,
                phoneNumber: userRecord.phoneNumber ?? null,
                providerIds: (userRecord.providerData ?? [])
                    .map((provider) => provider.providerId)
                    .filter((providerId) => Boolean(providerId)),
            };
        }
        catch (error) {
            const code = error.code;
            if (code === "auth/user-not-found") {
                return null;
            }
            throw error;
        }
    }
}
//# sourceMappingURL=FirebaseAuthService.js.map