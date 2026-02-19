import { AuthBlockingEvent } from "firebase-functions/v2/identity";
import { randomUUID } from "node:crypto";
import { HttpsError } from "firebase-functions/v2/https";
import {
  SecuritySettingsRepository,
  PersistProvidersOptions,
} from "../../domain/Interface/SecuritySettingsInterface.js";
import { AuditLogRepository } from "../../domain/Interface/AuditLogRepository.js";
import { AuthSessionRepository } from "../../domain/Interface/AuthSessionRepository.js";
import { SecuritySettingsModel } from "../../domain/model/SecuritySettingsModel.js";
import {
  AuthUserByUid,
  FirebaseAuthServiceInterface,
} from "../../Infra/auth/FirebaseAuthServiceInterface.js";

type SignInContext = {
  hasPhone: boolean;
  hasFederated: boolean;
  hasPassword: boolean;
  isNewFederatedProvider: boolean;
};

const FEDERATED_PROVIDERS = new Set(["google.com", "facebook.com", "apple.com"]);
const PASSWORD_PROVIDER = "password";

export class BeforeSignInPolicyUsecase {
  constructor(
    private readonly securityRepo: SecuritySettingsRepository,
    private readonly auditLogRepo: AuditLogRepository,
    private readonly authService: FirebaseAuthServiceInterface,
    private readonly authSessionRepo: AuthSessionRepository
  ) {}

  async execute(event: AuthBlockingEvent) {
    const user = event.data;
    if (!user?.uid) {
      throw new HttpsError("invalid-argument", "Missing UID");
    }

    const uid = user.uid;
    const ts = Math.floor(Date.now() / 1000);
    const sid = randomUUID();
    const incomingProviderIds = this.normalizeProviderIds(
      (user.providerData ?? []).map((p) => p.providerId)
    );
    const security = await this.securityRepo.createIfMissing(uid);
    const authoritativeUser = await this.resolveAuthUser(uid);
    const authoritativePhone = user.phoneNumber ?? authoritativeUser?.phoneNumber ?? null;
    const effectiveProviderIds = this.mergeProviderIds(
      incomingProviderIds,
      authoritativeUser?.providerIds ?? []
    );
    const existingProviderIds = security.providerIds ?? [];

    const ctx = this.deriveContext(
      effectiveProviderIds,
      existingProviderIds,
      authoritativePhone ?? undefined
    );
    this.enforceSignInPolicy(ctx, security);

    if (this.haveProviderIdsChanged(effectiveProviderIds, existingProviderIds)) {
      const persistOptions: PersistProvidersOptions = {
        consumeLinkingGrant: ctx.isNewFederatedProvider,
      };
      await this.securityRepo.persistProviders(uid, effectiveProviderIds, persistOptions);
    }

    if (ctx.isNewFederatedProvider) {
      await this.auditLogRepo.log(uid, "federated_link_confirmed", {
        providerIds: effectiveProviderIds,
      });
    }

    const primaryProvider = this.resolvePrimaryProvider(effectiveProviderIds, ctx.hasPhone);

    const emailWasVerifiedNow = Boolean(user.email && !security.hasVerifiedEmail);
    if (emailWasVerifiedNow) {

      await Promise.all([
        this.securityRepo.upsertUserSignInProfile(uid, {
          email: user.email,
          authProvider: primaryProvider.replace(".com", ""),
          providerIds: effectiveProviderIds,
        }),
        this.securityRepo.markEmailAsVerified(uid),
        this.auditLogRepo.log(uid, "email_verified", {
          email: user.email,
          provider: effectiveProviderIds,
        }),
      ]);
    }

    const session = await this.recordSessionBestEffort({
      uid,
      sid,
      provider: primaryProvider,
      providerIds: effectiveProviderIds,
      signInAtSeconds: ts,
      ipAddress: event.ipAddress,
      userAgent: event.userAgent,
    });

    return {
      sessionClaims: {
        ts,
        sid: session.sid,
        sv: session.sv,
        emailVerified: Boolean(security.hasVerifiedEmail || emailWasVerifiedNow),
      },
    };
  }

  private async resolveAuthUser(uid: string): Promise<AuthUserByUid | null> {
    try {
      return await this.authService.getUserByUid(uid);
    } catch {
      throw new HttpsError("internal", "Unable to verify authoritative auth state");
    }
  }

  private mergeProviderIds(incomingProviderIds: string[], authoritativeProviderIds: string[]): string[] {
    return this.normalizeProviderIds([...incomingProviderIds, ...authoritativeProviderIds]);
  }

  private normalizeProviderIds(providerIds: Array<string | undefined | null>): string[] {
    const seen = new Set<string>();
    const normalized: string[] = [];

    for (const providerId of providerIds) {
      if (!providerId || seen.has(providerId)) {
        continue;
      }
      seen.add(providerId);
      normalized.push(providerId);
    }

    return normalized;
  }

  private haveProviderIdsChanged(
    incomingProviderIds: string[],
    existingProviderIds: string[]
  ): boolean {
    const incoming = new Set(this.normalizeProviderIds(incomingProviderIds));
    const existing = new Set(this.normalizeProviderIds(existingProviderIds));

    if (incoming.size !== existing.size) {
      return true;
    }

    for (const providerId of incoming) {
      if (!existing.has(providerId)) {
        return true;
      }
    }

    return false;
  }

  private deriveContext(
    incomingProviderIds: string[],
    existingProviderIds: string[],
    phoneNumber?: string
  ): SignInContext {
    const incoming = new Set(incomingProviderIds);
    const existing = new Set(existingProviderIds);
    const hasProviderBaseline = existingProviderIds.length > 0;

    return {
      // `phoneNumber` is the strongest signal for phone-authenticated users,
      // including credential-linking flows where providerData can be incomplete.
      hasPhone: Boolean(phoneNumber) || incoming.has("phone"),
      hasPassword: incoming.has(PASSWORD_PROVIDER),
      hasFederated: incomingProviderIds.some((id) => FEDERATED_PROVIDERS.has(id)),
      // No baseline means we are syncing initial state, not evaluating a new link attempt.
      isNewFederatedProvider: hasProviderBaseline && incomingProviderIds.some(
        (id) => FEDERATED_PROVIDERS.has(id) && !existing.has(id)
      ),
    };
  }

  private enforceSignInPolicy(ctx: SignInContext, security: SecuritySettingsModel) {
    if (ctx.hasFederated && !ctx.hasPhone) {
      throw new HttpsError(
        "permission-denied",
        "Federated login requires verified phone number"
      );
    }

    if (ctx.hasPassword && !ctx.hasPhone && !security.hasVerifiedEmail) {
      throw new HttpsError(
        "permission-denied",
        "Password login requires verified email or phone number"
      );
    }

    if (ctx.hasPassword) {
      if (!security.passwordEnabled) {
        throw new HttpsError("permission-denied", "Password login disabled");
      }
      if (!security.hasVerifiedEmail) {
        throw new HttpsError("permission-denied", "Email not verified");
      }
    }

    if (ctx.isNewFederatedProvider && !security.allowFederatedLinking) {
      throw new HttpsError(
        "permission-denied",
        "Federated account linking not authorized"
      );
    }
  }

  private resolvePrimaryProvider(providerIds: string[], hasPhone: boolean): string {
    return (
      providerIds.find((p) => FEDERATED_PROVIDERS.has(p) || p === PASSWORD_PROVIDER) ??
      (hasPhone ? "phone" : providerIds[0] ?? "unknown")
    );
  }

  private async recordSessionBestEffort(input: {
    uid: string;
    sid: string;
    provider: string;
    providerIds: string[];
    signInAtSeconds: number;
    ipAddress?: string;
    userAgent?: string;
  }): Promise<{ sid: string; sv: number }> {
    try {
      const recorded = await this.authSessionRepo.recordSignInSession(input);
      await this.auditLogRepo.log(input.uid, "session_started", {
        sid: recorded.sid,
        sv: recorded.sv,
        provider: input.provider,
        providerIds: input.providerIds,
      });
      return recorded;
    } catch {
      // Observability must never block successful authentication.
      try {
        await this.auditLogRepo.log(input.uid, "session_log_failed", {
          sid: input.sid,
          provider: input.provider,
        });
      } catch {
        // noop
      }
      return { sid: input.sid, sv: 1 };
    }
  }
}
