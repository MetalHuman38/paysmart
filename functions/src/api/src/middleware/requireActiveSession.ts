import type { NextFunction, Request, Response } from "express";
import { initDeps } from "../dependencies.js";

type DecodedToken = {
  uid: string;
  sid?: unknown;
  sv?: unknown;
};

type SessionState = {
  activeSid: string | null;
  sessionVersion: number | null;
};

type SessionDeps = {
  verifyIdToken(idToken: string): Promise<DecodedToken>;
  getSessionState(uid: string): Promise<SessionState>;
};

function normalizeSid(value: unknown): string | null {
  if (typeof value !== "string") return null;
  const sid = value.trim();
  return sid.length > 0 ? sid : null;
}

function normalizeSessionVersion(value: unknown): number | null {
  if (typeof value === "number" && Number.isFinite(value) && value > 0) {
    return Math.floor(value);
  }
  if (typeof value === "string") {
    const parsed = Number(value);
    if (Number.isFinite(parsed) && parsed > 0) {
      return Math.floor(parsed);
    }
  }
  return null;
}

export function createRequireActiveSession(deps?: Partial<SessionDeps>) {
  const resolvedDeps: SessionDeps = {
    verifyIdToken:
      deps?.verifyIdToken ??
      (async (idToken: string) => {
        const { auth } = initDeps();
        return (await auth.verifyIdToken(idToken)) as unknown as DecodedToken;
      }),
    getSessionState:
      deps?.getSessionState ??
      (async (uid: string) => {
        const { firestore } = initDeps();
        const snap = await firestore
          .collection("users")
          .doc(uid)
          .collection("authSessionState")
          .doc("current")
          .get();

        if (!snap.exists) {
          return {
            activeSid: null,
            sessionVersion: null,
          };
        }

        return {
          activeSid: normalizeSid(snap.get("activeSid")),
          sessionVersion: normalizeSessionVersion(snap.get("sessionVersion")),
        };
      }),
  };

  return async function requireActiveSession(
    req: Request,
    res: Response,
    next: NextFunction
  ) {
    const authHeader = req.headers.authorization;
    if (!authHeader?.startsWith("Bearer ")) {
      return res.status(401).json({ error: "Missing token" });
    }

    const idToken = authHeader.substring("Bearer ".length);

    let decoded: DecodedToken;
    try {
      decoded = await resolvedDeps.verifyIdToken(idToken);
    } catch {
      return res.status(401).json({ error: "Invalid token" });
    }

    const uid = decoded.uid;
    const sid = normalizeSid(decoded.sid);
    const sv = normalizeSessionVersion(decoded.sv);
    if (!uid || !sid || !sv) {
      return res.status(401).json({ error: "Session claims missing. Please sign in again." });
    }

    let currentState: SessionState;
    try {
      currentState = await resolvedDeps.getSessionState(uid);
    } catch {
      return res.status(503).json({ error: "Session validation unavailable" });
    }

    if (!currentState.activeSid || !currentState.sessionVersion) {
      return res.status(401).json({ error: "Session not active. Please sign in again." });
    }

    if (currentState.activeSid !== sid || currentState.sessionVersion !== sv) {
      return res.status(401).json({ error: "Session replaced. Please sign in again." });
    }

    return next();
  };
}

export const requireActiveSession = createRequireActiveSession();
