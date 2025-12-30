import { beforeUserSignedIn } from "firebase-functions/v2/identity";
import { APP } from "../config/globals.js";
import { beforeSignInPolicy } from "../idp/beforeSignIn.policy.js";

export const beforeSignIn = beforeUserSignedIn(
  { region: APP.region },
  beforeSignInPolicy
);
