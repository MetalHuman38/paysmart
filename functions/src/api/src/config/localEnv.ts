import { existsSync } from "fs";
import { dirname, resolve } from "path";
import { fileURLToPath } from "url";
import { config as dotenvConfig } from "dotenv";

let loaded = false;

const apiPackageRoot = resolve(dirname(fileURLToPath(import.meta.url)), "../..");

export function resolveLocalEnvProjectId(env: NodeJS.ProcessEnv): string {
  return (
    env.GOOGLE_CLOUD_PROJECT ||
    env.GCLOUD_PROJECT ||
    env.GCP_PROJECT ||
    env.FIREBASE_PROJECT ||
    ""
  ).trim();
}

export function buildLocalEnvPaths(
  packageRoot: string,
  env: NodeJS.ProcessEnv
): string[] {
  const envPaths = [resolve(packageRoot, ".env")];
  const projectId = resolveLocalEnvProjectId(env);
  if (projectId) {
    envPaths.push(resolve(packageRoot, `.env.${projectId}`));
  }
  envPaths.push(resolve(packageRoot, ".env.local"));
  return envPaths;
}

export function shouldLoadLocalEnv(env: NodeJS.ProcessEnv): boolean {
  if (isEmulatorRuntime(env)) {
    return true;
  }

  return !isHostedRuntime(env);
}

export function ensureLocalEnvLoaded() {
  if (loaded) {
    return;
  }

  loaded = true;
  if (!shouldLoadLocalEnv(process.env)) {
    return;
  }

  for (const envPath of buildLocalEnvPaths(apiPackageRoot, process.env)) {
    if (existsSync(envPath)) {
      dotenvConfig({ path: envPath });
    }
  }
}

export function resetLocalEnvLoadedForTest() {
  loaded = false;
}

function isHostedRuntime(env: NodeJS.ProcessEnv): boolean {
  return [env.K_SERVICE, env.K_REVISION, env.FUNCTION_TARGET, env.FUNCTION_SIGNATURE_TYPE]
    .some((value) => typeof value === "string" && value.trim().length > 0);
}

function isEmulatorRuntime(env: NodeJS.ProcessEnv): boolean {
  return [
    env.FUNCTIONS_EMULATOR,
    env.FIREBASE_EMULATOR_HUB,
    env.FIRESTORE_EMULATOR_HOST,
    env.FIREBASE_AUTH_EMULATOR_HOST,
  ].some((value) => typeof value === "string" && value.trim().length > 0);
}
