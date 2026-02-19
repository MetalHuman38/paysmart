// checkEmailOrPhone.ts
import type { Request, Response } from "express";
import * as admin from 'firebase-admin';

export const checkEmailOrPhone = async (req: Request, res: Response) => {
  try {
    const { email, phoneNumber } = req.body ?? {};

    if (!email && !phoneNumber) {
      res.status(400).json({ error: 'Provide email or phoneNumber' });
      return;
    }

    const providers = new Set<string>();
    let emailUser = null;
    let phoneUser = null;

    if (email) {
      try {
        emailUser = await admin.auth().getUserByEmail(email);
        emailUser.providerData.forEach(p => providers.add(p.providerId));
      } catch (error: any) {
        if (error?.code !== 'auth/user-not-found') {
          console.error('Failed to lookup by email', error);
          throw error;
        }
      }
    }

    if (phoneNumber) {
      try {
        phoneUser = await admin.auth().getUserByPhoneNumber(phoneNumber);
        phoneUser.providerData.forEach(p => providers.add(p.providerId));
      } catch (error: any) {
        if (error?.code !== 'auth/user-not-found') {
          console.error('Failed to lookup by phone', error);
          throw error;
        }
      }
    }

    const conflict = Boolean(emailUser && phoneUser && emailUser.uid !== phoneUser.uid);
    let linkTargetUid: string | null = null;
    if (!conflict) {
      linkTargetUid = (phoneUser ?? emailUser)?.uid ?? null;
    }

    const matchedBy: Array<'email' | 'phone'> = [];
    if (emailUser) matchedBy.push('email');
    if (phoneUser) matchedBy.push('phone');

    res.json({
      accountExists: Boolean(phoneUser ?? emailUser),
      matchedBy,
      existingProviders: Array.from(providers),
      linkTargetUid,
      canLinkFederatedProvider: Boolean(linkTargetUid),
      conflict,
    });
  } catch (e: any) {
    console.error('checkEmailOrPhone failed', e);
    res.status(500).json({ error: 'Internal server error' });
  }
};
