import { generateAuthenticationOptions, generateRegistrationOptions, verifyAuthenticationResponse, verifyRegistrationResponse, } from "@simplewebauthn/server";
export class PasskeyService {
    passkeys;
    config;
    constructor(passkeys, config) {
        this.passkeys = passkeys;
        this.config = config;
    }
    async beginRegistration(uid, input) {
        this.assertEnabled();
        const credentials = await this.passkeys.listCredentials(uid);
        const options = await generateRegistrationOptions({
            rpID: this.config.rpId,
            rpName: this.config.rpName,
            userID: Buffer.from(uid, "utf8"),
            userName: input.userName,
            userDisplayName: input.userDisplayName,
            timeout: 60_000,
            attestationType: "none",
            authenticatorSelection: {
                residentKey: "required",
                userVerification: "required",
            },
            excludeCredentials: credentials.map((credential) => ({
                id: credential.credentialId,
                transports: credential.transports,
            })),
        });
        await this.passkeys.saveChallenge(uid, {
            kind: "registration",
            challenge: options.challenge,
            expiresAtMs: Date.now() + this.config.challengeTtlMs,
        });
        return options;
    }
    async completeRegistration(uid, credential) {
        this.assertEnabled();
        const challenge = await this.consumeChallenge(uid, "registration");
        const credentialJson = parseCredentialPayload(credential);
        const verification = await verifyRegistrationResponse({
            response: credentialJson,
            expectedChallenge: challenge,
            expectedOrigin: Array.from(this.config.expectedOrigins),
            expectedRPID: this.config.rpId,
            requireUserVerification: true,
        });
        if (!verification.verified || !verification.registrationInfo) {
            throw new Error("PASSKEY_REGISTRATION_VERIFICATION_FAILED");
        }
        const now = Date.now();
        const registrationInfo = verification.registrationInfo;
        const registeredCredential = registrationInfo.credential;
        await this.passkeys.upsertCredential(uid, {
            credentialId: registeredCredential.id,
            publicKeyBase64Url: Buffer.from(registeredCredential.publicKey).toString("base64url"),
            counter: registeredCredential.counter,
            transports: (registeredCredential.transports ?? []),
            deviceType: registrationInfo.credentialDeviceType,
            backedUp: registrationInfo.credentialBackedUp,
            createdAtMs: now,
            updatedAtMs: now,
        });
        return {
            verified: true,
            credentialId: registeredCredential.id,
        };
    }
    async beginAuthentication(uid) {
        this.assertEnabled();
        const credentials = await this.passkeys.listCredentials(uid);
        if (credentials.length === 0) {
            throw new Error("PASSKEY_CREDENTIALS_NOT_FOUND");
        }
        const options = await generateAuthenticationOptions({
            rpID: this.config.rpId,
            allowCredentials: credentials.map((credential) => ({
                id: credential.credentialId,
                transports: credential.transports,
            })),
            userVerification: "required",
            timeout: 60_000,
        });
        await this.passkeys.saveChallenge(uid, {
            kind: "authentication",
            challenge: options.challenge,
            expiresAtMs: Date.now() + this.config.challengeTtlMs,
        });
        return options;
    }
    async completeAuthentication(uid, credential) {
        this.assertEnabled();
        const challenge = await this.consumeChallenge(uid, "authentication");
        const credentialJson = parseCredentialPayload(credential);
        const credentialId = (credentialJson?.id ?? "").toString();
        if (!credentialId) {
            throw new Error("PASSKEY_CREDENTIAL_ID_MISSING");
        }
        const stored = await this.passkeys.getCredential(uid, credentialId);
        if (!stored) {
            throw new Error("PASSKEY_CREDENTIAL_NOT_REGISTERED");
        }
        const verification = await verifyAuthenticationResponse({
            response: credentialJson,
            expectedChallenge: challenge,
            expectedOrigin: Array.from(this.config.expectedOrigins),
            expectedRPID: this.config.rpId,
            credential: {
                id: stored.credentialId,
                publicKey: Buffer.from(stored.publicKeyBase64Url, "base64url"),
                counter: stored.counter,
                transports: stored.transports,
            },
            requireUserVerification: true,
        });
        if (!verification.verified) {
            throw new Error("PASSKEY_AUTHENTICATION_VERIFICATION_FAILED");
        }
        await this.passkeys.updateCounter(uid, stored.credentialId, verification.authenticationInfo.newCounter);
        return {
            verified: true,
            credentialId: stored.credentialId,
        };
    }
    async consumeChallenge(uid, kind) {
        const stored = await this.passkeys.consumeChallenge(uid, kind);
        if (!stored) {
            throw new Error("PASSKEY_CHALLENGE_MISSING");
        }
        if (stored.expiresAtMs < Date.now()) {
            throw new Error("PASSKEY_CHALLENGE_EXPIRED");
        }
        return stored.challenge;
    }
    assertEnabled() {
        if (!this.config.enabled) {
            throw new Error("PASSKEY_NOT_CONFIGURED");
        }
    }
}
function parseCredentialPayload(input) {
    if (typeof input === "string") {
        const trimmed = input.trim();
        if (!trimmed)
            throw new Error("Missing credential");
        try {
            return JSON.parse(trimmed);
        }
        catch {
            throw new Error("Invalid credential JSON");
        }
    }
    if (input && typeof input === "object") {
        return input;
    }
    throw new Error("Missing credential");
}
//# sourceMappingURL=passkeyService.js.map