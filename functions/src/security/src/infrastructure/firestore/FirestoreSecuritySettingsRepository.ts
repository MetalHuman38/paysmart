import { Firestore } from "firebase-admin/firestore";
import { SecuritySettingsRepository } from "../../domain/repository/SecuritySettingsRepository.js";
import { SecuritySettingsModel } from "../../domain/model/SecuritySettingsModel.js";
import { getDefaultSecuritySettings } from "../../constants/index.js";

export class FirestoreSecuritySettingsRepository
  implements SecuritySettingsRepository {

  constructor(private readonly firestore: Firestore) {}

  private ref(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings");
  }

  async get(uid: string): Promise<SecuritySettingsModel | null> {
    const snap = await this.ref(uid).get();
    return snap.exists ? (snap.data() as SecuritySettingsModel) : null;
  }

  async createIfMissing(uid: string): Promise<void> {
    const ref = this.ref(uid);

    await this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(ref);
      if (!snap.exists) {
        tx.set(ref, getDefaultSecuritySettings());
      }
    });
  }

  async update(uid: string, data: Partial<SecuritySettingsModel>): Promise<void> {
    await this.ref(uid).set(data, { merge: true });
  }
}
