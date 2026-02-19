import { Request, Response } from "express";
import { apiContainer } from "../infrastructure/di/apiContainer.js";
import { GetUIDFromAuthHeader } from "../application/usecase/GetUIDFromAuthHeader.js";

export async function getUidFromAuthHeaders(req: Request, res: Response) {
  const { authService } = apiContainer();
  const getUid = new GetUIDFromAuthHeader(authService);
  const uid = await getUid.execute(req.headers.authorization);
  if (!uid) {
    return res.status(401).json({ error: "Missing or invalid token" });
  }
  return res.status(200).json({ uid });
}