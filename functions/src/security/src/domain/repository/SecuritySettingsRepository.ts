import { SecuritySettingsModel } from "../model/SecuritySettingsModel.js";

export interface SecuritySettingsRepository {
  get(uid: string): Promise<SecuritySettingsModel | null>;
  createIfMissing(uid: string): Promise<void>;
  update(uid: string, data: Partial<SecuritySettingsModel>): Promise<void>;
}
