import { initDeps } from "../../dependencies.js";
import { FirestoreSecuritySettingsRepository } from "../firestore/FirestoreSecuritySettingsRepository.js";
import { EnsureSecuritySettings } from "../../domain/usecases/EnsureSecuritySettings.js";
import { SeedSecuritySettingsOnUserCreate } from "../../domain/usecases/SeedSecuritySettings.js";


export function buildSecurityContainer() {
  const { firestore } = initDeps();

  const repo = new FirestoreSecuritySettingsRepository(firestore);
  
  const seedSecuritySettingsOnUserCreate = new SeedSecuritySettingsOnUserCreate(repo);
  const ensureSecuritySettings = new EnsureSecuritySettings(repo);

  return {
    seedSecuritySettingsOnUserCreate,
    ensureSecuritySettings,
  };
}


