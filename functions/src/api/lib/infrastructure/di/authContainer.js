// infrastructure/di/authContainer.ts
import { initDeps } from "../../dependencies.js";
import { FirebaseAuthService } from "../auth/FirebaseAuthService.js";
import { GetUIDFromAuthHeader } from "../../application/usecase/GetUIDFromAuthHeader.js";
import { CheckPhoneAvailability } from "../../application/usecase/CheckPhoneAvailability.js";
import { FirestoreSecuritySettingsRepository } from "../firestore/FirestoreSecuritySettingsRepository.js";
import { FirestoreUserRepository } from "../firestore/FirestoreUserRepository.js";
import { FirestoreIdentityUploadRepository } from "../firestore/FirestoreIdentityUploadRepository.js";
import { FirestoreIdentityProviderRepository } from "../firestore/FirestoreIdentityProviderRepository.js";
import { FirestoreAddMoneyRepository } from "../firestore/FirestoreAddMoneyRepository.js";
import { FirestorePasskeyRepository } from "../firestore/FirestorePasskeyRepository.js";
import { GoogleCloudAccessTokenProvider } from "../../services/googleCloudAccessTokenProvider.js";
import { PlayIntegrityVerifier } from "../../services/playIntegrityVerifier.js";
import { KmsEnvelopeService } from "../../services/kmsEnvelopeService.js";
import { GoogleVisionTextExtractionService } from "../../services/googleVisionTextExtractionService.js";
import { PasskeyService } from "../../services/passkeyService.js";
import { StripePaymentsService } from "../../services/stripePaymentsService.js";
export function authContainer() {
    const { auth, firestore, getConfig } = initDeps();
    const config = getConfig();
    const storageBucket = config.storageBucket || `${config.projectId}.appspot.com`;
    const tokenProvider = new GoogleCloudAccessTokenProvider();
    const attestationVerifier = new PlayIntegrityVerifier(tokenProvider, {
        packageName: config.playIntegrityPackageName,
        maxAgeMs: config.playIntegrityMaxAgeMs,
        allowFallback: config.playIntegrityAllowFallback,
        allowedAppVerdicts: config.playIntegrityAllowedAppVerdicts,
        allowedDeviceVerdicts: config.playIntegrityAllowedDeviceVerdicts,
        requireLicensed: config.playIntegrityRequireLicensed,
    });
    const kmsEnvelopeService = new KmsEnvelopeService(tokenProvider, config.identityKmsKeyName);
    const stripePaymentsService = new StripePaymentsService({
        secretKey: config.stripeSecretKey,
        webhookSigningSecret: config.stripeWebhookSecret,
        allowUnsignedWebhooks: config.stripeAllowUnsignedWebhooks,
    });
    const authService = new FirebaseAuthService(auth);
    const securityRepo = new FirestoreSecuritySettingsRepository(firestore);
    const userRepo = new FirestoreUserRepository(firestore);
    const identityUploadRepo = new FirestoreIdentityUploadRepository(firestore, storageBucket, attestationVerifier, kmsEnvelopeService, config.identityMaxPayloadBytes);
    const identityProviderRepo = new FirestoreIdentityProviderRepository(firestore);
    const passkeyRepo = new FirestorePasskeyRepository(firestore);
    const addMoneyRepo = new FirestoreAddMoneyRepository(firestore, stripePaymentsService, config.stripePublishableKey, config.stripeAllowedTopupCurrencies, config.stripeMinimumTopupAmountMinor);
    const identityTextExtractionService = new GoogleVisionTextExtractionService(undefined, config.identityOcrEnabled);
    const passkeyService = new PasskeyService(passkeyRepo, {
        enabled: config.passkeyEnabled,
        rpId: config.passkeyRpId,
        rpName: config.passkeyRpName,
        expectedOrigins: config.passkeyExpectedOrigins,
        challengeTtlMs: config.passkeyChallengeTtlMs,
    });
    return {
        getUIDFromAuthHeader: new GetUIDFromAuthHeader(authService),
        checkPhoneAvailability: new CheckPhoneAvailability(authService),
        authService,
        securitySettings: securityRepo,
        userRepo,
        identityUploads: identityUploadRepo,
        identityProvider: identityProviderRepo,
        identityTextExtraction: identityTextExtractionService,
        passkeys: passkeyService,
        addMoney: addMoneyRepo,
    };
}
//# sourceMappingURL=authContainer.js.map