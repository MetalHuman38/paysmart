// FIRESTORE TRIGGER: On User Created, seed security settings document
import { onDocumentCreated } from "firebase-functions/v2/firestore";
import { setGlobalOptions } from "firebase-functions/v2/options";
import { initDeps } from "../dependencies.js";
import { getDefaultSecuritySettings } from "../constants/index.js";

setGlobalOptions({
  region: "europe-west2",
  memory: "256MiB",
  concurrency: 20,
  cpu: 1,
});

export const seedSecurityOnUserCreate = onDocumentCreated(
  {
    document: "users/{uid}",
  },
  async (event) => {
    const { firestore } = initDeps();
    const uid = event.params.uid as string;

    const secRef = firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");

    await firestore.runTransaction(async (tx) => {
      const secSnap = await tx.get(secRef);
      if (!secSnap.exists) {
        tx.set(secRef, getDefaultSecuritySettings());
      }
    });
  }
);
