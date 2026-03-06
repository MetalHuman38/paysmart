import { FinalizeInvoiceInput, InvoiceShiftInput } from "../../domain/model/invoice.js";

export type NormalizedInvoiceFinalize = {
  idempotencyKey: string;
  sequenceYear: number;
  currency: string;
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
  totalHours: number;
  hourlyRate: number;
  subtotalMinor: number;
};

const DATE_PATTERN = /^\d{4}-\d{2}-\d{2}$/;
const WEEKDAY_TO_ISO_INDEX: Record<string, number> = {
  mon: 1,
  tue: 2,
  wed: 3,
  thu: 4,
  fri: 5,
  sat: 6,
  sun: 7,
};

export function normalizeInvoiceFinalizeInput(
  uid: string,
  input: FinalizeInvoiceInput
): NormalizedInvoiceFinalize {
  const profile = requireObject(input.profile, "profile");
  const venue = requireObject(input.venue, "venue");
  const weekly = requireObject(input.weekly, "weekly");
  const invoiceDate = requireDate(weekly.invoiceDate, "weekly.invoiceDate");
  const weekEndingDate = requireDate(weekly.weekEndingDate, "weekly.weekEndingDate");
  const shifts = normalizeShifts(weekly.shifts, weekEndingDate);
  const totalHours = roundMoney(shifts.reduce((sum, row) => sum + parsePositive(row.hoursInput, "shift.hoursInput"), 0));
  const hourlyRate = parsePositive(weekly.hourlyRateInput, "weekly.hourlyRateInput");
  const subtotalMinor = Math.round(totalHours * hourlyRate * 100);
  if (subtotalMinor <= 0) {
    throw new Error("Subtotal must be greater than 0");
  }

  return {
    idempotencyKey: normalizeIdempotencyKey(input.idempotencyKey, uid, weekEndingDate, venue.venueId),
    sequenceYear: Number(invoiceDate.substring(0, 4)),
    currency: normalizeCurrency(input.currency ?? "GBP"),
    profile: {
      fullName: requireText(profile.fullName, "profile.fullName"),
      address: requireText(profile.address, "profile.address"),
      badgeNumber: requireText(profile.badgeNumber, "profile.badgeNumber"),
      badgeExpiryDate: requireDate(profile.badgeExpiryDate, "profile.badgeExpiryDate"),
      utrNumber: requireText(profile.utrNumber, "profile.utrNumber"),
      email: requireText(profile.email, "profile.email"),
      contactPhone: optionalText(profile.contactPhone),
      paymentMethod: requireText(profile.paymentMethod, "profile.paymentMethod"),
      accountNumber: optionalText(profile.accountNumber),
      sortCode: optionalText(profile.sortCode),
      paymentInstructions: optionalText(profile.paymentInstructions),
      declaration: requireText(profile.declaration, "profile.declaration"),
    },
    venue: {
      venueId: requireText(venue.venueId, "venue.venueId"),
      venueName: requireText(venue.venueName, "venue.venueName"),
      venueAddress: optionalText(venue.venueAddress),
    },
    weekly: {
      invoiceDate,
      weekEndingDate,
      hourlyRateInput: weekly.hourlyRateInput.trim(),
      shifts,
    },
    totalHours,
    hourlyRate,
    subtotalMinor,
  };
}

function normalizeShifts(raw: InvoiceShiftInput[], weekEndingDate: string): InvoiceShiftInput[] {
  if (!Array.isArray(raw) || raw.length === 0) {
    throw new Error("weekly.shifts is required");
  }
  const normalized: InvoiceShiftInput[] = [];
  raw.forEach((row, index) => {
    const dayLabel = optionalText(row?.dayLabel);
    const workDate = optionalText(row?.workDate);
    const hoursInput = optionalText(row?.hoursInput);
    if (!workDate && !hoursInput) return;
    const resolvedDayLabel = dayLabel || requireText(row?.dayLabel, `weekly.shifts[${index}].dayLabel`);
    normalized.push({
      dayLabel: resolvedDayLabel,
      workDate: workDate
        ? requireDate(workDate, `weekly.shifts[${index}].workDate`)
        : inferWorkDateFromWeekEnding(resolvedDayLabel, weekEndingDate, `weekly.shifts[${index}]`),
      hoursInput: requireText(hoursInput, `weekly.shifts[${index}].hoursInput`),
    });
  });
  if (normalized.length === 0) {
    throw new Error("weekly.shifts is required");
  }
  return normalized;
}

function requireObject<T>(value: T, field: string): NonNullable<T> {
  if (!value || typeof value !== "object") throw new Error(`${field} is required`);
  return value as NonNullable<T>;
}

function requireDate(value: unknown, field: string): string {
  const text = requireText(value, field);
  if (!DATE_PATTERN.test(text)) throw new Error(`${field} must be YYYY-MM-DD`);
  return formatIsoDate(parseIsoDate(text, field));
}

function requireText(value: unknown, field: string): string {
  const text = typeof value === "string" ? value.trim() : "";
  if (!text) throw new Error(`${field} is required`);
  return text;
}

function optionalText(value: unknown): string {
  return typeof value === "string" ? value.trim() : "";
}

function parsePositive(raw: string, field: string): number {
  const value = Number(raw.trim());
  if (!Number.isFinite(value) || value <= 0) throw new Error(`${field} must be greater than 0`);
  return roundMoney(value);
}

function normalizeCurrency(raw: string): string {
  const currency = raw.trim().toUpperCase();
  if (currency.length !== 3) throw new Error("currency must be an ISO-4217 code");
  return currency;
}

function normalizeIdempotencyKey(raw: string | undefined, uid: string, weekEndingDate: string, venueId: string): string {
  const fallback = `${uid}:${weekEndingDate}:${venueId}`.slice(0, 120);
  const candidate = (raw || fallback).trim();
  return candidate.replace(/[^\w\-:.]/g, "_").slice(0, 120);
}

function roundMoney(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

function inferWorkDateFromWeekEnding(dayLabel: string, weekEndingDate: string, field: string): string {
  const targetIsoDay = WEEKDAY_TO_ISO_INDEX[weekdayKey(dayLabel)];
  if (!targetIsoDay) {
    throw new Error(`${field}.dayLabel is invalid`);
  }

  const endingDate = parseIsoDate(weekEndingDate, `${field}.weekEndingDate`);
  const endingIsoDay = getIsoDayOfWeek(endingDate);
  const offset = (endingIsoDay - targetIsoDay + 7) % 7;
  endingDate.setUTCDate(endingDate.getUTCDate() - offset);
  return formatIsoDate(endingDate);
}

function parseIsoDate(value: string, field: string): Date {
  const parsed = new Date(`${value}T00:00:00Z`);
  if (Number.isNaN(parsed.getTime()) || formatIsoDate(parsed) !== value) {
    throw new Error(`${field} must be YYYY-MM-DD`);
  }
  return parsed;
}

function formatIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10);
}

function getIsoDayOfWeek(date: Date): number {
  const utcDay = date.getUTCDay();
  return utcDay === 0 ? 7 : utcDay;
}

function weekdayKey(raw: string): string {
  return raw.trim().toLowerCase().slice(0, 3);
}
