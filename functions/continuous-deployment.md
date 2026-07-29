# Continuous Deployment From GitHub To GCP

Last verified: 2026-07-29

This repository currently deploys backend code as Firebase Functions Gen 2, configured in the root `firebase.json`:

- `functions/src/api` deploys as the `api` codebase.
- `functions/src/auth` deploys as the `auth` codebase.
- Production project: `paysmart-7ee79`.
- Primary deployed function region used by the app: `europe-west2`.

## Recommended Path For This Repo

Use a Google Cloud Build trigger connected to GitHub. On a push to `main`, the trigger should run `cloudbuild.firebase.yaml`, which installs the two function packages, builds TypeScript, and deploys Firebase Functions.

Flow:

```text
GitHub main push
  -> Cloud Build trigger
  -> npm ci for functions/src/api and functions/src/auth
  -> npm --prefix functions run build
  -> firebase deploy --project paysmart-7ee79 --only functions --non-interactive
```

This keeps deployment owned by GCP while using GitHub as the source repository.

## GCP Setup

Enable these APIs in project `paysmart-7ee79`:

- Cloud Build API
- Firebase API
- Resource Manager API
- Secret Manager API
- Cloud Functions API
- Cloud Run Admin API
- Artifact Registry API

Connect GitHub to Cloud Build:

1. Open Google Cloud Console -> Cloud Build -> Repositories.
2. Connect the GitHub repository using Cloud Build repositories 2nd gen or Developer Connect.
3. Install/authorize the Google Cloud Build GitHub app for the repository if prompted.
4. Confirm Cloud Build can read the repository.

Create the trigger:

1. Open Cloud Build -> Triggers -> Create trigger.
2. Event: push to branch.
3. Source: this GitHub repository.
4. Branch regex: `^main$`.
5. Configuration type: Cloud Build configuration file.
6. Configuration file path: `cloudbuild.firebase.yaml`.
7. Substitution `_FIREBASE_PROJECT_ID`: `paysmart-7ee79`.
8. Save the trigger.

## IAM

Use a dedicated Cloud Build service account if possible. The build service account needs permission to deploy Firebase resources and to let the Firebase deployment process create/update Gen 2 backing resources.

Baseline roles from the Google Cloud Firebase deployment guidance:

- Cloud Build Service Account
- Firebase Admin
- API Keys Viewer

For Functions Gen 2 deployments, verify the service account can also create/update the underlying Cloud Functions, Cloud Run, Artifact Registry, Eventarc, Cloud Build, and runtime-service-account bindings required by Firebase. If deployment fails with an IAM error, add only the missing role called out by the build log.

## GitHub Branch Controls

Protect `main` before enabling automatic production deploys:

- Require pull requests before merge.
- Require the existing GitHub Actions CI checks to pass.
- Restrict direct pushes to release maintainers.
- Keep repository secrets out of Git; this repo uses ignored local `.env*` files and Firebase/GCP managed secrets for production values.

## Manual Validation

Before enabling the trigger, run locally:

```bash
cd functions
npm run install:all
npm run build
npm run test
```

Then test the Cloud Build config manually from the repository root:

```bash
gcloud builds submit --config cloudbuild.firebase.yaml --substitutions _FIREBASE_PROJECT_ID=paysmart-7ee79 .
```

After deployment:

```bash
firebase functions:list --project paysmart-7ee79
firebase functions:log --project paysmart-7ee79
```

Smoke test the production API base URL used by Android:

```bash
curl https://europe-west2-paysmart-7ee79.cloudfunctions.net/api/
```

Expected response:

```json
{"ok":true,"service":"api"}
```

## Cloud Run Source Or Function Alternative

Use the Cloud Run console "Continuously deploy from a repository" path only when deploying a standalone Cloud Run service or Cloud Run function from this repository. That flow creates or attaches a Cloud Build trigger for a Cloud Run service.

For a standalone service:

1. Cloud Run -> Create service.
2. Select "Continuously deploy new revisions from a source repository".
3. Choose Cloud Build or Developer Connect.
4. Select the GitHub repository and branch.
5. For buildpacks, set the build context directory to the service directory.
6. For Dockerfile builds, set the Dockerfile path and build context directory.
7. Leave Function target blank.

For a Cloud Run function:

1. Use the same source repository connection flow.
2. Select the function/source build option.
3. Set the build context directory to the function source directory.
4. Set Function target to the exported function entry point.

Do not use this direct Cloud Run source/function path for the current `functions/src/api` and `functions/src/auth` Firebase codebases unless they are intentionally split out of Firebase deployment ownership.

## References

- Google Cloud Run: Continuously deploy from a repository: https://docs.cloud.google.com/run/docs/continuous-deployment
- Google Cloud Build: Deploy to Firebase: https://docs.cloud.google.com/build/docs/deploying-builds/deploy-firebase
- Google Cloud Build: Connect to a GitHub repository: https://docs.cloud.google.com/build/docs/automating-builds/github/connect-repo-github
- Firebase CLI deploy reference: https://firebase.google.com/docs/cli
- Firebase Functions deployment management: https://firebase.google.com/docs/functions/manage-functions
