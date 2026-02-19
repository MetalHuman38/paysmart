import { SecuritySettingsModel } from "../model/SecuritySettingsModel.js";

export type PersistProvidersOptions = {
  consumeLinkingGrant?: boolean;
};

export interface SecuritySettingsRepository {
  get(uid: string): Promise<SecuritySettingsModel | null>;
  createIfMissing(uid: string): Promise<SecuritySettingsModel>;
  update(uid: string, data: Partial<SecuritySettingsModel>): Promise<void>;
  persistProviders(
    uid: string,
    providerIds: string[],
    options?: PersistProvidersOptions
  ): Promise<void>;
  markEmailAsVerified(uid: string): Promise<void>;
  upsertUserSignInProfile(
    uid: string,
    data: {
      email?: string;
      authProvider?: string;
      providerIds: string[];
    }
  ): Promise<void>;
}
