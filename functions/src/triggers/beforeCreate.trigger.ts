import {
  beforeUserCreated,
} from "firebase-functions/v2/identity";

import { beforeCreatePolicy } from "../idp/beforeCreate.policy.js";
import { APP } from "../config/globals.js";

export const beforeCreate = beforeUserCreated(
    { region: APP.region },
    beforeCreatePolicy
);
