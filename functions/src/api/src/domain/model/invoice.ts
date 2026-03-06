export interface InvoiceShiftInput {
  dayLabel: string;
  workDate: string;
  hoursInput: string;
}

export interface InvoiceProfileInput {
  fullName: string;
  address: string;
  badgeNumber: string;
  badgeExpiryDate: string;
  utrNumber: string;
  email: string;
  contactPhone?: string;
  paymentMethod: string;
  accountNumber?: string;
  sortCode?: string;
  paymentInstructions?: string;
  declaration: string;
}

export interface InvoiceVenueInput {
  venueId: string;
  venueName: string;
  venueAddress: string;
}

export interface InvoiceWeeklyInput {
  invoiceDate: string;
  weekEndingDate: string;
  hourlyRateInput: string;
  shifts: InvoiceShiftInput[];
}

export interface FinalizeInvoiceInput {
  profile: InvoiceProfileInput;
  venue: InvoiceVenueInput;
  weekly: InvoiceWeeklyInput;
  currency?: string;
  idempotencyKey?: string;
}

export interface FinalizedInvoice {
  invoiceId: string;
  invoiceNumber: string;
  status: "finalized";
  sequenceNumber: number;
  totalHours: number;
  hourlyRate: number;
  subtotalMinor: number;
  currency: string;
  venueName: string;
  weekEndingDate: string;
  createdAtMs: number;
}

export type InvoicePdfStatus =
  | "not_requested"
  | "queued"
  | "processing"
  | "ready"
  | "failed";

export interface InvoicePdfDocument {
  status: InvoicePdfStatus;
  fileName: string;
  contentType: "application/pdf";
  templateVersion: string;
  objectPath?: string;
  sizeBytes?: number;
  generatedAtMs?: number;
  error?: string;
}

export interface InvoiceSummary {
  invoiceId: string;
  invoiceNumber: string;
  status: string;
  subtotalMinor: number;
  currency: string;
  venueName: string;
  weekEndingDate: string;
  createdAtMs: number;
}

export interface InvoiceListPage {
  items: InvoiceSummary[];
  nextCursor: string | null;
}

export interface InvoiceDetail extends InvoiceSummary {
  sequenceNumber: number;
  totalHours: number;
  hourlyRate: number;
  pdf: InvoicePdfDocument;
  profile: {
    fullName: string;
    address: string;
    badgeNumber: string;
    badgeExpiryDate: string;
    utrNumber: string;
    email: string;
    contactPhone: string;
    paymentMethod: string;
    accountNumber: string;
    sortCode: string;
    paymentInstructions: string;
    declaration: string;
  };
  venue: {
    venueId: string;
    venueName: string;
    venueAddress: string;
  };
  weekly: {
    invoiceDate: string;
    weekEndingDate: string;
    hourlyRateInput: string;
    shifts: InvoiceShiftInput[];
  };
}

export interface QueueInvoicePdfResult extends InvoicePdfDocument {
  invoiceId: string;
}

export interface InvoicePdfDownload {
  fileName: string;
  contentType: "application/pdf";
  bytes: Buffer;
}
