import { describe, expect, it } from "vitest";
import { normalizeInvoiceFinalizeInput } from "../../../infrastructure/firestore/invoiceFinalizeParsing.js";

describe("normalizeInvoiceFinalizeInput", () => {
  it("infers missing shift dates from week ending and weekday label", () => {
    const normalized = normalizeInvoiceFinalizeInput("uid-1", {
      profile: {
        fullName: "Test User",
        address: "1 Test Street",
        badgeNumber: "B-1",
        badgeExpiryDate: "2027-01-01",
        utrNumber: "1234567890",
        email: "test@example.com",
        paymentMethod: "bank_transfer",
        declaration: "I confirm that I am self-employed.",
      },
      venue: {
        venueId: "venue_1",
        venueName: "Alpha Venue",
        venueAddress: "Venue Street",
      },
      weekly: {
        invoiceDate: "2026-03-03",
        weekEndingDate: "2026-03-08",
        hourlyRateInput: "15",
        shifts: [
          { dayLabel: "Friday", workDate: "", hoursInput: "10" },
          { dayLabel: "Saturday", workDate: "", hoursInput: "8" },
          { dayLabel: "Monday", workDate: "", hoursInput: "" },
        ],
      },
    });

    expect(normalized.weekly.shifts).toEqual([
      { dayLabel: "Friday", workDate: "2026-03-06", hoursInput: "10" },
      { dayLabel: "Saturday", workDate: "2026-03-07", hoursInput: "8" },
    ]);
    expect(normalized.totalHours).toBe(18);
  });
});
