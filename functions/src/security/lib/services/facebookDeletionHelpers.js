// Facebook data deletion helpers
import { FieldValue } from "firebase-admin/firestore";
import { initDeps } from "../dependencies.js";
export async function lookupUidByAppScopedId(appScopedUserId) {
    const { firestore } = initDeps();
    const docId = `facebook:${appScopedUserId}`;
    const snap = await firestore
        .collection("app_scoped_id_mappings")
        .doc(docId)
        .get();
    return snap.exists ? snap.data()?.uid ?? null : null;
}
export async function writeDeletionLog(appScopedUserId, uid, userSnapshot, actionSummary, confirmationCode) {
    const { firestore } = initDeps();
    await firestore.collection("deletion_logs").add({
        app_scoped_user_id: appScopedUserId,
        uid,
        user_snapshot: userSnapshot,
        action_summary: actionSummary,
        confirmation_code: confirmationCode,
        created_at: FieldValue.serverTimestamp(),
    });
}
export async function getUserDoc(uid) {
    const { firestore } = initDeps();
    const snap = await firestore.collection("users").doc(uid).get();
    if (!snap.exists)
        return null;
    return { id: snap.id, ...snap.data() };
}
export async function deleteUserData(uid, keepForAml = true) {
    const { firestore, auth } = initDeps();
    const summary = {
        uid,
        deleted: [],
        retained: [],
        notes: [],
    };
    const userRef = firestore.collection("users").doc(uid);
    const userSnap = await userRef.get();
    if (userSnap.exists) {
        if (keepForAml) {
            await userRef.update({
                email: null,
                displayName: "Deleted User",
                photoURL: null,
                phoneNumber: null,
                isOnboarded: false,
                identity: null,
                draft: null,
                contact: null,
                updatedAt: FieldValue.serverTimestamp(),
            });
            summary.retained.push("users");
            summary.notes.push("anonymised per AML/KYC policy");
        }
        else {
            await userRef.delete();
            summary.deleted.push("users");
        }
    }
    try {
        await auth.deleteUser(uid);
        summary.deleted.push("auth_user");
    }
    catch (err) {
        summary.notes.push("auth user not found");
    }
    return summary;
}
//# sourceMappingURL=facebookDeletionHelpers.js.map