import { Firestore, FieldValue } from 'firebase-admin/firestore';
import { SECURITY_SETTINGS_COLLECTION } from '../../constants/index.js';
import {
  PersistProvidersOptions,
  SecuritySettingsRepository,
} from '../../domain/Interface/SecuritySettingsInterface.js';
import { SecuritySettingsModel } from '../../domain/model/SecuritySettingsModel.js';

export class FirestoreSecuritySettingsRepository implements SecuritySettingsRepository {
  constructor(private readonly firestore: Firestore) {}

  private ref(uid: string) {
    return this.firestore
      .collection("users")
      .doc(uid).collection("security")
      .doc("settings");
  }

  private userRef(uid: string) {
    return this.firestore.collection("users").doc(uid);
  }

  async createIfMissing(uid: string): Promise<SecuritySettingsModel> {
    const docRef = this.ref(uid);
    await this.firestore.runTransaction(async (tx) => {
      const doc = await tx.get(docRef);
      if (!doc.exists) {
        const initialData: SecuritySettingsModel = {
          ...SECURITY_SETTINGS_COLLECTION,
          updatedAt: FieldValue.serverTimestamp(),
        };
        tx.set(docRef, initialData);
        return initialData;
      }
      return doc.data() as SecuritySettingsModel;
    });
    return (await docRef.get()).data() as SecuritySettingsModel;
  }

  async get(uid: string): Promise<SecuritySettingsModel | null> {
    const doc = await this.ref(uid).get();
    return doc.exists ? (doc.data() as SecuritySettingsModel) : null;
  }

  async update(uid: string, data: Partial<SecuritySettingsModel>): Promise<void> {
    await this.ref(uid).set(data, { merge: true });
  }

  async persistProviders(
    uid: string,
    providerIds: string[],
    options: PersistProvidersOptions = {}
  ): Promise<void> {
    const payload: Record<string, unknown> = {
      providerIds: [...new Set(providerIds)],
      updatedAt: FieldValue.serverTimestamp(),
    };

    // One-time link window should be consumed only after a real new federated link.
    if (options.consumeLinkingGrant) {
      payload.allowFederatedLinking = false;
    }

    await this.ref(uid).set(payload, { merge: true });
  }

  async markEmailAsVerified(uid: string): Promise<void> {
    await this.ref(uid).set(
      {
        hasVerifiedEmail: true,
        emailToVerify: FieldValue.delete(),
        updatedAt: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }

  async upsertUserSignInProfile(
    uid: string,
    data: {
      email?: string;
      authProvider?: string;
      providerIds: string[];
    }
  ): Promise<void> {
    await this.userRef(uid).set(
      {
        ...data,
        lastSignedIn: FieldValue.serverTimestamp(),
      },
      { merge: true }
    );
  }
}
    
