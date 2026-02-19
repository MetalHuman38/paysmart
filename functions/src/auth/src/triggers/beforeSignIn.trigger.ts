import { beforeUserSignedIn } from "firebase-functions/v2/identity";
import { APP } from "../config/globals.js";
import { beforeSignInPolicy } from "../idp/beforeSignIn.policy.js";

export const beforeSignIn: ReturnType<typeof beforeUserSignedIn> = beforeUserSignedIn(
  { region: APP.region },
  beforeSignInPolicy
);
