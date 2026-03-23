import { describe, expect, it } from "vitest";
import { renderInvoicePdf } from "../../services/invoicePdfRenderer.js";
import { InvoiceDetail } from "../../domain/model/invoice.js";

describe("renderInvoicePdf", () => {
  it("renders a valid pdf buffer", () => {
    const invoice: InvoiceDetail = {
      invoiceId: "2026-000001",
      invoiceNumber: "PS-2026-000001",
      status: "finalized",
      sequenceNumber: 1,
      totalHours: 20,
      hourlyRate: 15,
      subtotalMinor: 30000,
      currency: "GBP",
      venueName: "Alpha Venue",
      weekEndingDate: "2026-03-08",
      createdAtMs: 1772668800000,
      pdf: {
        status: "not_requested",
        fileName: "PS-2026-000001.pdf",
        contentType: "application/pdf",
        templateVersion: "pay-smart-invoice-v2",
      },
      profile: {
        fullName: "Jane Tester",
        address: "1 Test Street",
        badgeNumber: "B-1",
        badgeExpiryDate: "2027-01-01",
        utrNumber: "1234567890",
        email: "jane@example.com",
        contactPhone: "07123456789",
        paymentMethod: "bank_transfer",
        accountNumber: "12345678",
        sortCode: "00-00-00",
        paymentInstructions: "",
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
          { dayLabel: "Friday", workDate: "2026-03-06", hoursInput: "10" },
          { dayLabel: "Saturday", workDate: "2026-03-07", hoursInput: "10" },
        ],
      },
    };

    const pdf = renderInvoicePdf(invoice);
    const raw = pdf.toString("latin1");

    expect(pdf.byteLength).toBeGreaterThan(64);
    expect(pdf.subarray(0, 8).toString("utf8")).toContain("%PDF-1.4");
    expect(raw).toContain("PaySmart Weekly Invoice");
    expect(raw).toContain("Invoice summary");
    expect(raw).toContain("Worker and venue details");
    expect(raw).toContain("Worker details");
    expect(raw).toContain("Venue details");
    expect(raw).toContain("Shifts worked");
    expect(raw).toContain("Grand total");
    expect(raw).toContain("VoltService Ltd");
    expect(raw).toContain("pay-smart.net");
    expect(raw).toContain("pay-smart-invoice-v2");
  });
});
