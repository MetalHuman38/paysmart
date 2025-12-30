import { Request, Response } from "express";
import { initDeps } from "./dependencies.js";
import { getAuth } from "firebase-admin/auth";
import { FieldValue, Timestamp } from "firebase-admin/firestore";
import { logEvent } from "./utils.js";

/**
 * POST /auth/email/verification/generate
 * body: { email?: string }
 */

const COOLDOWN_SECONDS = 60;
const MAX_DAILY_ATTEMPTS = 5;

export async function generateEmailVerificationHandler(
  req: Request,
  res: Response
) {
  try {
    const { firestore, auth, getConfig } = initDeps();
    const cfg = getConfig();

    /* ---------- Auth ---------- */
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing or invalid token" });
    }

    const decoded = await getAuth().verifyIdToken(
      authHeader.substring("Bearer ".length)
    );
    const uid = decoded.uid;

    /* ---------- Email ---------- */
    const email =
      (req.body?.email || decoded.email || "").trim().toLowerCase();

    if (!email) {
      return res.status(400).json({ error: "Email is required" });
    }

    /* ---------- Fetch Auth user ---------- */
    const authUser = await auth.getUser(uid);

    // Attach email if missing or changed
    if (!authUser.email || authUser.email.toLowerCase() !== email) {
      await auth.updateUser(uid, {
        email,
        emailVerified: false,
      });
    }

    /* ---------- User profile ---------- */
    const userRef = firestore.collection("users").doc(uid);
    const userSnap = await userRef.get();

    if (!userSnap.exists) {
      return res.status(404).json({ error: "User profile missing" });
    }

    const userProfile = userSnap.data()!;
    const tenantId = String(userProfile.tenantId || "").toLowerCase();

    if (
      cfg.allowedTenants.size &&
      !cfg.allowedTenants.has(tenantId)
    ) {
      return res.status(403).json({ error: "Tenant not allowed" });
    }

    /* ---------- Security doc ---------- */
    const secRef = userRef.collection("security").doc("settings");
    const now = Timestamp.now();

    let allowed = true;
    let retryAfter = 0;

    await firestore.runTransaction(async (tx) => {
      const snap = await tx.get(secRef);
      if (!snap.exists) throw new Error("security/settings missing");

      const sec = snap.data()!;

      if (sec.hasVerifiedEmail === true) {
        allowed = false;
        return;
      }

      const sentAt = sec.emailVerificationSentAt as Timestamp | null;
      const attempts = Number(sec.emailVerificationAttemptsToday || 0);

      if (sentAt) {
        const elapsed = now.seconds - sentAt.seconds;
        if (elapsed < COOLDOWN_SECONDS) {
          allowed = false;
          retryAfter = COOLDOWN_SECONDS - elapsed;
          return;
        }
      }

      if (attempts >= MAX_DAILY_ATTEMPTS) {
        allowed = false;
        return;
      }

      tx.set(
        secRef,
        {
          emailToVerify: email,
          emailVerificationSentAt: now,
          emailVerificationAttemptsToday: FieldValue.increment(1),
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );
    });

    if (!allowed) {
      if (retryAfter > 0) {
        res.setHeader("Retry-After", String(retryAfter));
        return res.status(429).json({ error: "Cooldown active" });
      }
      return res.status(429).json({ error: "Too many attempts" });
    }

    /* ---------- Generate link ---------- */
    const link = await auth.generateEmailVerificationLink(email, {
      url: cfg.getVerifyUrl(),
      handleCodeInApp: false,
    });

    const sendRealEmail = process.env.NODE_ENV === "production" ||
      process.env.SEND_REAL_EMAILS === "true";

    if (!sendRealEmail) {
    console.log(`[DEV] Email verification link for ${email}: ${link}`);
    } else {
        await cfg.getMailer().sendVerificationEmail({
        to: email,
        link,
      });
    }

    /* ---------- Audit ---------- */
    await firestore.collection("audit_logs").add({
      type: "email_verification_sent",
      uid,
      tenantId,
      email,
      provider: decoded.firebase?.sign_in_provider || null,
      ip: req.ip,
      createdAt: FieldValue.serverTimestamp(),
    });

    logEvent("email-verification:sent", { uid, email, tenantId });

    return res.json({ sent: true });
  } catch (err: any) {
    console.error(err);
    return res.status(500).json({ error: err.message });
  }
}


/**
 * POST /auth/email/verification/status
 * body: { email: string }
 */
export async function checkEmailVerificationStatusHandler(
  req: Request,
  res: Response
) {
  try {
    const { firestore } = initDeps();

    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const decoded = await getAuth().verifyIdToken(
      authHeader.substring("Bearer ".length)
    );
    const uid = decoded.uid;

    const snap = await firestore
      .collection("users")
      .doc(uid)
      .collection("security")
      .doc("settings")
      .get();

    return res.json({
      verified: Boolean(snap.data()?.hasVerifiedEmail),
    });
  } catch (err: any) {
    console.error("checkEmailVerificationStatusHandler error:", err);
    return res.status(500).json({ error: err.message });
  }
}

