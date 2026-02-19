export class FirebaseAuthService {
    auth;
    constructor(auth) {
        this.auth = auth;
    }
    async verifyIdToken(idToken) {
        const decoded = await this.auth.verifyIdToken(idToken);
        return {
            uid: decoded.uid,
            email: decoded.email,
        };
    }
    async getUser(uid) {
        const user = await this.auth.getUser(uid);
        return {
            uid: user.uid,
            email: user.email ?? undefined,
        };
    }
    async updateUserEmail(uid, email) {
        await this.auth.updateUser(uid, {
            email,
            emailVerified: false,
        });
    }
    async getUserByPhone(phone) {
        try {
            const user = await this.auth.getUserByPhoneNumber(phone);
            return { uid: user.uid };
        }
        catch (err) {
            if (err?.code === "auth/user-not-found") {
                return null;
            }
            throw err;
        }
    }
    async generateEmailVerificationLink(email, continueUrl) {
        return this.auth.generateEmailVerificationLink(email, {
            url: continueUrl,
            handleCodeInApp: false,
        });
    }
}
//# sourceMappingURL=FirebaseAuthService.js.map