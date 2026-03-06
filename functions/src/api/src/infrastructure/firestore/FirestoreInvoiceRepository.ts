import { FieldValue, Firestore } from "firebase-admin/firestore";
import { getStorage } from "firebase-admin/storage";
import {
  FinalizeInvoiceInput,
  FinalizedInvoice,
  InvoiceDetail,
  InvoicePdfDocument,
  InvoicePdfDownload,
  InvoiceListPage,
  InvoiceShiftInput,
  InvoiceSummary,
  QueueInvoicePdfResult,
} from "../../domain/model/invoice.js";
import { InvoiceRepository } from "../../domain/repository/InvoiceRepository.js";
import {
  NormalizedInvoiceFinalize,
  normalizeInvoiceFinalizeInput,
} from "./invoiceFinalizeParsing.js";

type InvoiceCounterDoc = {
  year?: number;
  lastSequence?: number;
};

type IdempotencyDoc = {
  invoiceId?: string;
  result?: FinalizedInvoice;
};

const INVOICE_PDF_TEMPLATE_VERSION = "pay-smart-invoice-v1";

export class FirestoreInvoiceRepository implements InvoiceRepository {
  constructor(
    private readonly firestore: Firestore,
    private readonly storageBucketName: string
  ) {}

  async finalize(uid: string, input: FinalizeInvoiceInput): Promise<FinalizedInvoice> {
    const normalized = normalizeInvoiceFinalizeInput(uid, input);
    const idempotencyRef = this.idempotencyRef(uid, normalized.idempotencyKey);
    const counterRef = this.counterRef(uid);

    return this.firestore.runTransaction(async (tx) => {
      const existingRequest = (await tx.get(idempotencyRef)).data() as IdempotencyDoc | undefined;
      const existingResult = existingRequest?.result;
      if (isFinalizedInvoice(existingResult)) {
        return existingResult;
      }

      const counter = (await tx.get(counterRef)).data() as InvoiceCounterDoc | undefined;
      const nextSequence = this.nextSequence(counter, normalized.sequenceYear);
      const invoiceId = this.invoiceId(normalized.sequenceYear, nextSequence);
      const invoiceNumber = this.invoiceNumber(normalized.sequenceYear, nextSequence);
      const createdAtMs = Date.now();

      const result: FinalizedInvoice = {
        invoiceId,
        invoiceNumber,
        status: "finalized",
        sequenceNumber: nextSequence,
        totalHours: normalized.totalHours,
        hourlyRate: normalized.hourlyRate,
        subtotalMinor: normalized.subtotalMinor,
        currency: normalized.currency,
        venueName: normalized.venue.venueName,
        weekEndingDate: normalized.weekly.weekEndingDate,
        createdAtMs,
      };

      tx.set(
        this.invoiceRef(uid, invoiceId),
        this.invoiceDocument(uid, normalized, result),
        { merge: true }
      );
      tx.set(
        counterRef,
        {
          year: normalized.sequenceYear,
          lastSequence: nextSequence,
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );
      tx.set(
        idempotencyRef,
        {
          invoiceId,
          result,
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      return result;
    });
  }

  async getById(uid: string, invoiceId: string): Promise<InvoiceDetail> {
    const cleanInvoiceId = invoiceId.trim();
    if (!cleanInvoiceId) {
      throw new Error("Missing invoiceId");
    }

    const snap = await this.invoiceRef(uid, cleanInvoiceId).get();
    if (!snap.exists) {
      throw new Error("Invoice not found");
    }

    return toInvoiceDetail(cleanInvoiceId, snap.data());
  }

  async list(uid: string, limit: number, cursor?: string): Promise<InvoiceListPage> {
    const safeLimit = Number.isFinite(limit) ? Math.max(1, Math.min(Math.floor(limit), 100)) : 20;
    let query = this.firestore
      .collection("users")
      .doc(uid)
      .collection("invoices")
      .orderBy("createdAtMs", "desc")
      .limit(safeLimit + 1);

    const cleanCursor = cursor?.trim();
    if (cleanCursor) {
      const cursorSnap = await this.invoiceRef(uid, cleanCursor).get();
      if (!cursorSnap.exists) {
        throw new Error("Invalid cursor");
      }
      query = query.startAfter(cursorSnap);
    }

    const snap = await query.get();
    const hasMore = snap.docs.length > safeLimit;
    const pageDocs = hasMore ? snap.docs.slice(0, safeLimit) : snap.docs;
    const items = pageDocs.map((doc) => toInvoiceSummary(doc.id, doc.data()));
    const nextCursor = hasMore ? pageDocs[pageDocs.length - 1]?.id ?? null : null;

    return {
      items,
      nextCursor,
    };
  }

  async queuePdf(uid: string, invoiceId: string): Promise<QueueInvoicePdfResult> {
    const cleanInvoiceId = invoiceId.trim();
    if (!cleanInvoiceId) {
      throw new Error("Missing invoiceId");
    }

    const ref = this.invoiceRef(uid, cleanInvoiceId);
    return this.firestore.runTransaction(async (tx) => {
      const snap = await tx.get(ref);
      if (!snap.exists) {
        throw new Error("Invoice not found");
      }

      const data = asRecord(snap.data());
      const invoiceNumber = asString(data.invoiceNumber);
      const currentPdf = toInvoicePdf(cleanInvoiceId, invoiceNumber, data.pdf);
      const nextPdf: InvoicePdfDocument =
        currentPdf.status === "ready" || currentPdf.status === "processing" ?
          currentPdf :
          {
            status: "queued",
            fileName: currentPdf.fileName,
            contentType: "application/pdf",
            templateVersion: currentPdf.templateVersion,
            objectPath: currentPdf.objectPath,
          };

      tx.set(
        ref,
        {
          pdf: {
            ...nextPdf,
            requestedAt: FieldValue.serverTimestamp(),
            updatedAt: FieldValue.serverTimestamp(),
            error: null,
          },
          updatedAt: FieldValue.serverTimestamp(),
        },
        { merge: true }
      );

      return {
        invoiceId: cleanInvoiceId,
        ...nextPdf,
      };
    });
  }

  async downloadPdf(uid: string, invoiceId: string): Promise<InvoicePdfDownload> {
    const cleanInvoiceId = invoiceId.trim();
    if (!cleanInvoiceId) {
      throw new Error("Missing invoiceId");
    }

    const snap = await this.invoiceRef(uid, cleanInvoiceId).get();
    if (!snap.exists) {
      throw new Error("Invoice not found");
    }

    const data = asRecord(snap.data());
    const pdf = toInvoicePdf(cleanInvoiceId, asString(data.invoiceNumber), data.pdf);
    if (pdf.status !== "ready") {
      throw new Error("Invoice PDF is not ready");
    }
    if (!pdf.objectPath) {
      throw new Error("Invoice PDF file is missing");
    }

    const bucket = getStorage().bucket(this.storageBucketName);
    const file = bucket.file(pdf.objectPath);
    const [exists] = await file.exists();
    if (!exists) {
      throw new Error("Invoice PDF file is missing");
    }

    const [bytes] = await file.download();
    return {
      fileName: pdf.fileName,
      contentType: "application/pdf",
      bytes,
    };
  }

  private invoiceDocument(
    uid: string,
    input: NormalizedInvoiceFinalize,
    result: FinalizedInvoice
  ): Record<string, unknown> {
    return {
      ...result,
      uid,
      profile: input.profile,
      venue: input.venue,
      weekly: input.weekly,
      pdf: {
        status: "not_requested",
        fileName: `${result.invoiceNumber}.pdf`,
        contentType: "application/pdf",
        templateVersion: INVOICE_PDF_TEMPLATE_VERSION,
      },
      createdAt: FieldValue.serverTimestamp(),
      updatedAt: FieldValue.serverTimestamp(),
      finalizedAt: FieldValue.serverTimestamp(),
      idempotencyKey: input.idempotencyKey,
    };
  }

  private nextSequence(counter: InvoiceCounterDoc | undefined, sequenceYear: number): number {
    const year = Number(counter?.year || 0);
    const last = Number(counter?.lastSequence || 0);
    const base = year === sequenceYear && Number.isFinite(last) ? last : 0;
    return Math.max(0, Math.floor(base)) + 1;
  }

  private invoiceId(year: number, sequence: number): string {
    return `${year}-${String(sequence).padStart(6, "0")}`;
  }

  private invoiceNumber(year: number, sequence: number): string {
    return `PS-${year}-${String(sequence).padStart(6, "0")}`;
  }

  private invoiceRef(uid: string, invoiceId: string) {
    return this.firestore.collection("users").doc(uid).collection("invoices").doc(invoiceId);
  }

  private counterRef(uid: string) {
    return this.firestore.collection("users").doc(uid).collection("invoiceMeta").doc("counter");
  }

  private idempotencyRef(uid: string, key: string) {
    return this.firestore
      .collection("users")
      .doc(uid)
      .collection("invoiceFinalizeRequests")
      .doc(key);
  }
}

function isFinalizedInvoice(raw: unknown): raw is FinalizedInvoice {
  const value = raw as FinalizedInvoice | undefined;
  return Boolean(value?.invoiceId && value?.invoiceNumber && value?.status === "finalized");
}

function toInvoiceSummary(invoiceId: string, raw: unknown): InvoiceSummary {
  const data = asRecord(raw);
  return {
    invoiceId,
    invoiceNumber: asString(data.invoiceNumber),
    status: asString(data.status) || "finalized",
    subtotalMinor: asInt(data.subtotalMinor),
    currency: asString(data.currency) || "GBP",
    venueName: asString(data.venueName) || asString(asRecord(data.venue).venueName),
    weekEndingDate: asString(data.weekEndingDate) || asString(asRecord(data.weekly).weekEndingDate),
    createdAtMs: asInt(data.createdAtMs),
  };
}

function toInvoiceDetail(invoiceId: string, raw: unknown): InvoiceDetail {
  const data = asRecord(raw);
  const summary = toInvoiceSummary(invoiceId, data);
  const profile = asRecord(data.profile);
  const venue = asRecord(data.venue);
  const weekly = asRecord(data.weekly);
  const pdf = toInvoicePdf(invoiceId, summary.invoiceNumber, data.pdf);
  return {
    ...summary,
    sequenceNumber: asInt(data.sequenceNumber),
    totalHours: asNumber(data.totalHours),
    hourlyRate: asNumber(data.hourlyRate),
    pdf,
    profile: {
      fullName: asString(profile.fullName),
      address: asString(profile.address),
      badgeNumber: asString(profile.badgeNumber),
      badgeExpiryDate: asString(profile.badgeExpiryDate),
      utrNumber: asString(profile.utrNumber),
      email: asString(profile.email),
      contactPhone: asString(profile.contactPhone),
      paymentMethod: asString(profile.paymentMethod),
      accountNumber: asString(profile.accountNumber),
      sortCode: asString(profile.sortCode),
      paymentInstructions: asString(profile.paymentInstructions),
      declaration: asString(profile.declaration),
    },
    venue: {
      venueId: asString(venue.venueId),
      venueName: asString(venue.venueName),
      venueAddress: asString(venue.venueAddress),
    },
    weekly: {
      invoiceDate: asString(weekly.invoiceDate),
      weekEndingDate: asString(weekly.weekEndingDate),
      hourlyRateInput: asString(weekly.hourlyRateInput),
      shifts: asShiftArray(weekly.shifts),
    },
  };
}

function toInvoicePdf(
  invoiceId: string,
  invoiceNumber: string,
  raw: unknown
): InvoicePdfDocument {
  const data = asRecord(raw);
  const status = normalizePdfStatus(asString(data.status));
  return {
    status,
    fileName: asString(data.fileName) || `${invoiceNumber || invoiceId}.pdf`,
    contentType: "application/pdf",
    templateVersion: asString(data.templateVersion) || INVOICE_PDF_TEMPLATE_VERSION,
    objectPath: asString(data.objectPath) || undefined,
    sizeBytes: toOptionalInt(data.sizeBytes),
    generatedAtMs: toOptionalInt(data.generatedAtMs),
    error: asString(data.error) || undefined,
  };
}

function asShiftArray(raw: unknown): InvoiceShiftInput[] {
  if (!Array.isArray(raw)) {
    return [];
  }
  return raw.map((entry) => {
    const row = asRecord(entry);
    return {
      dayLabel: asString(row.dayLabel),
      workDate: asString(row.workDate),
      hoursInput: asString(row.hoursInput),
    };
  });
}

function asRecord(raw: unknown): Record<string, unknown> {
  return raw && typeof raw === "object" ? (raw as Record<string, unknown>) : {};
}

function asString(raw: unknown): string {
  return typeof raw === "string" ? raw.trim() : "";
}

function asNumber(raw: unknown): number {
  if (typeof raw === "number" && Number.isFinite(raw)) {
    return raw;
  }
  if (typeof raw === "string") {
    const parsed = Number(raw);
    if (Number.isFinite(parsed)) {
      return parsed;
    }
  }
  return 0;
}

function asInt(raw: unknown): number {
  return Math.floor(asNumber(raw));
}

function toOptionalInt(raw: unknown): number | undefined {
  const value = asInt(raw);
  return value > 0 ? value : undefined;
}

function normalizePdfStatus(raw: string): InvoicePdfDocument["status"] {
  switch (raw) {
    case "queued":
    case "processing":
    case "ready":
    case "failed":
    case "not_requested":
      return raw;
    default:
      return "not_requested";
  }
}
