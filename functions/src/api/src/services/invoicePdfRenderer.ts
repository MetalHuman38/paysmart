import { InvoiceDetail } from "../domain/model/invoice.js";

const PAGE_WIDTH = 595;
const PAGE_HEIGHT = 842;
const PAGE_MARGIN = 40;
const CONTENT_WIDTH = PAGE_WIDTH - (PAGE_MARGIN * 2);
const HEADER_HEIGHT = 74;
const FOOTER_HEIGHT = 26;
const CONTENT_BOTTOM_Y = PAGE_MARGIN + FOOTER_HEIGHT + 22;
const CARD_PADDING = 14;
const CARD_GAP = 14;
const BODY_FONT_SIZE = 10.5;
const SMALL_FONT_SIZE = 9;
const TITLE_FONT_SIZE = 12;
const HEADER_TITLE_SIZE = 18;
const SECTION_LINE_HEIGHT = 14;
const TABLE_ROW_HEIGHT = 24;
const TABLE_HEADER_HEIGHT = 26;

const BRAND = rgb(0x06, 0x5a, 0x60);
const ACCENT = rgb(0x52, 0xb7, 0x88);
const SURFACE = rgb(0xff, 0xff, 0xff);
const SURFACE_ALT = rgb(0xf4, 0xf7, 0xf8);
const BORDER = rgb(0xd7, 0xe0, 0xe4);
const TEXT = rgb(0x1f, 0x36, 0x3d);
const WHITE = rgb(0xff, 0xff, 0xff);
const LOGO_GREEN = rgb(0x2c, 0x9c, 0x6a);

type PdfColor = [number, number, number];
type PdfFont = "F1" | "F2";

type TextOptions = {
  font?: PdfFont;
  size?: number;
  color?: PdfColor;
};

type SectionCard = {
  title: string;
  lines: string[];
};

export function renderInvoicePdf(invoice: InvoiceDetail): Buffer {
  const paginator = new PdfPaginator(invoice);

  paginator.drawSectionCard({
    title: "Invoice summary",
    lines: [
      `Invoice number: ${invoice.invoiceNumber}`,
      `Invoice date: ${invoice.weekly.invoiceDate}`,
      `Week ending: ${invoice.weekEndingDate}`,
      `Venue: ${invoice.venue.venueName}`,
      `Currency: ${invoice.currency}`,
      `Status: ${invoice.status}`,
    ],
  });

  paginator.drawPartyColumns();

  paginator.drawShiftGroups();

  paginator.drawTotalsCard();

  paginator.drawSectionCard({
    title: "Declaration",
    lines: [invoice.profile.declaration],
  });

  paginator.drawSectionCard({
    title: "Payment information",
    lines: buildPaymentLines(invoice),
  });

  return paginator.build();
}

function drawHeader(canvas: PdfCanvas, invoice: InvoiceDetail, topY: number): void {
  const bottomY = topY - HEADER_HEIGHT;
  canvas.drawRect(PAGE_MARGIN, bottomY, CONTENT_WIDTH, HEADER_HEIGHT, {
    fillColor: BRAND,
    strokeColor: BRAND,
  });

  const logoSize = 34;
  const logoX = PAGE_MARGIN + 16;
  const logoY = bottomY + 20;
  drawPaySmartLogo(canvas, logoX, logoY, logoSize);

  canvas.drawText(PAGE_MARGIN + 64, topY - 26, "PaySmart Weekly Invoice", {
    font: "F2",
    size: HEADER_TITLE_SIZE,
    color: WHITE,
  });
  canvas.drawText(PAGE_MARGIN + 64, topY - 46, "Clean weekly invoicing for UK self-employed work", {
    size: BODY_FONT_SIZE,
    color: WHITE,
  });

  const amountText = formatMinor(invoice.subtotalMinor, invoice.currency);
  canvas.drawTextRight(PAGE_MARGIN + CONTENT_WIDTH - 16, topY - 24, amountText, {
    font: "F2",
    size: 16,
    color: WHITE,
  });
  canvas.drawTextRight(PAGE_MARGIN + CONTENT_WIDTH - 16, topY - 44, `Total due for ${invoice.invoiceNumber}`, {
    size: SMALL_FONT_SIZE,
    color: WHITE,
  });
}

function drawSectionCard(
  canvas: PdfCanvas,
  topY: number,
  section: SectionCard
): number {
  const wrappedLines = section.lines
    .map((line) => wrapText(line, CONTENT_WIDTH - (CARD_PADDING * 2), BODY_FONT_SIZE))
    .flat();
  const cardHeight =
    CARD_PADDING +
    TITLE_FONT_SIZE +
    10 +
    (wrappedLines.length * SECTION_LINE_HEIGHT) +
    CARD_PADDING;
  const bottomY = topY - cardHeight;

  canvas.drawRect(PAGE_MARGIN, bottomY, CONTENT_WIDTH, cardHeight, {
    fillColor: SURFACE,
    strokeColor: BORDER,
  });
  canvas.drawText(PAGE_MARGIN + CARD_PADDING, topY - 24, section.title, {
    font: "F2",
    size: TITLE_FONT_SIZE,
    color: TEXT,
  });

  let lineY = topY - 42;
  for (const line of wrappedLines) {
    canvas.drawText(PAGE_MARGIN + CARD_PADDING, lineY, line, {
      size: BODY_FONT_SIZE,
      color: TEXT,
    });
    lineY -= SECTION_LINE_HEIGHT;
  }

  return bottomY;
}

function drawPartyColumns(
  canvas: PdfCanvas,
  topY: number,
  invoice: InvoiceDetail
): number {
  const title = "Worker and venue details";
  const columnGap = 20;
  const innerWidth = CONTENT_WIDTH - (CARD_PADDING * 2);
  const columnWidth = (innerWidth - columnGap) / 2;
  const leftLines = [
    invoice.profile.fullName,
    invoice.profile.address,
    `Badge number: ${invoice.profile.badgeNumber}`,
    `Badge expiry: ${invoice.profile.badgeExpiryDate}`,
    `UTR number: ${invoice.profile.utrNumber}`,
    `Email: ${invoice.profile.email}`,
    invoice.profile.contactPhone ? `Phone: ${invoice.profile.contactPhone}` : "",
  ].filter(Boolean);
  const rightLines = [
    invoice.venue.venueName,
    invoice.venue.venueAddress || "Venue address not provided",
    `Service period ending: ${invoice.weekEndingDate}`,
    `Invoice currency: ${invoice.currency}`,
    `Hourly rate: ${invoice.hourlyRate.toFixed(2)} ${invoice.currency}`,
  ];

  const wrappedLeft = leftLines.map((line) => wrapText(line, columnWidth, BODY_FONT_SIZE)).flat();
  const wrappedRight = rightLines.map((line) => wrapText(line, columnWidth, BODY_FONT_SIZE)).flat();
  const lineCount = Math.max(wrappedLeft.length, wrappedRight.length);
  const cardHeight =
    CARD_PADDING +
    TITLE_FONT_SIZE +
    10 +
    BODY_FONT_SIZE +
    12 +
    (lineCount * SECTION_LINE_HEIGHT) +
    CARD_PADDING;
  const bottomY = topY - cardHeight;
  const leftX = PAGE_MARGIN + CARD_PADDING;
  const rightX = leftX + columnWidth + columnGap;
  const dividerX = leftX + columnWidth + (columnGap / 2);

  canvas.drawRect(PAGE_MARGIN, bottomY, CONTENT_WIDTH, cardHeight, {
    fillColor: SURFACE,
    strokeColor: BORDER,
  });
  canvas.drawText(leftX, topY - 24, title, {
    font: "F2",
    size: TITLE_FONT_SIZE,
    color: TEXT,
  });
  canvas.drawText(leftX, topY - 44, "Worker details", {
    font: "F2",
    size: BODY_FONT_SIZE,
    color: BRAND,
  });
  canvas.drawText(rightX, topY - 44, "Venue details", {
    font: "F2",
    size: BODY_FONT_SIZE,
    color: BRAND,
  });
  canvas.drawLine(dividerX, bottomY + CARD_PADDING, dividerX, topY - 52, BORDER, 0.8);

  let leftY = topY - 60;
  wrappedLeft.forEach((line) => {
    canvas.drawText(leftX, leftY, line, {
      size: BODY_FONT_SIZE,
      color: TEXT,
    });
    leftY -= SECTION_LINE_HEIGHT;
  });

  let rightY = topY - 60;
  wrappedRight.forEach((line) => {
    canvas.drawText(rightX, rightY, line, {
      size: BODY_FONT_SIZE,
      color: TEXT,
    });
    rightY -= SECTION_LINE_HEIGHT;
  });

  return bottomY;
}

function drawTotalsCard(canvas: PdfCanvas, topY: number, invoice: InvoiceDetail): number {
  const lines = [
    [`Total hours`, `${invoice.totalHours.toFixed(2)}`],
    [`Hourly rate`, `${invoice.hourlyRate.toFixed(2)} ${invoice.currency}`],
    [`Subtotal`, formatMinor(invoice.subtotalMinor, invoice.currency)],
    [`Grand total`, formatMinor(invoice.subtotalMinor, invoice.currency)],
  ] as const;

  const cardHeight = 120;
  const bottomY = topY - cardHeight;
  canvas.drawRect(PAGE_MARGIN, bottomY, CONTENT_WIDTH, cardHeight, {
    fillColor: SURFACE,
    strokeColor: BORDER,
  });
  canvas.drawText(PAGE_MARGIN + CARD_PADDING, topY - 24, "Totals", {
    font: "F2",
    size: TITLE_FONT_SIZE,
    color: TEXT,
  });

  let rowY = topY - 48;
  lines.forEach(([label, value], index) => {
    const font: PdfFont = index === lines.length - 1 ? "F2" : "F1";
    const color = index === lines.length - 1 ? BRAND : TEXT;
    canvas.drawText(PAGE_MARGIN + CARD_PADDING, rowY, label, {
      font,
      size: BODY_FONT_SIZE,
      color,
    });
    canvas.drawTextRight(PAGE_MARGIN + CONTENT_WIDTH - CARD_PADDING, rowY, value, {
      font,
      size: BODY_FONT_SIZE,
      color,
    });
    rowY -= 18;
  });

  return bottomY;
}

function drawFooter(
  canvas: PdfCanvas,
  invoice: InvoiceDetail,
  pageNumber: number,
  totalPages: number
): void {
  const footerTop = PAGE_MARGIN + FOOTER_HEIGHT;
  const brandLineY = PAGE_MARGIN + 12;
  const marketingLineY = PAGE_MARGIN + 2;
  const metadata = [
    `Invoice ID ${invoice.invoiceId}`,
    `Template ${invoice.pdf.templateVersion}`,
    `Page ${pageNumber} of ${totalPages}`,
  ].join("  •  ");

  canvas.drawLine(PAGE_MARGIN, footerTop, PAGE_MARGIN + CONTENT_WIDTH, footerTop, BORDER, 0.8);
  canvas.drawText(PAGE_MARGIN, brandLineY, "PaySmart by VoltService Ltd  •  pay-smart.net", {
    font: "F2",
    size: SMALL_FONT_SIZE,
    color: BRAND,
  });
  canvas.drawText(
    PAGE_MARGIN,
    marketingLineY,
    "Invoice template prepared for weekly contractor and shift-work billing.",
    {
      size: SMALL_FONT_SIZE,
      color: TEXT,
    }
  );
  canvas.drawTextRight(PAGE_MARGIN + CONTENT_WIDTH, brandLineY, metadata, {
    size: SMALL_FONT_SIZE,
    color: TEXT,
  });
}

class PdfPaginator {
  private readonly pages: PdfCanvas[] = [];
  private currentCanvas!: PdfCanvas;
  private cursorY = 0;

  constructor(private readonly invoice: InvoiceDetail) {
    this.startNewPage();
  }

  drawSectionCard(section: SectionCard): void {
    this.ensureSpace(measureSectionCardHeight(section));
    this.cursorY = drawSectionCard(this.currentCanvas, this.cursorY, section);
    this.cursorY -= CARD_GAP;
  }

  drawPartyColumns(): void {
    this.ensureSpace(measurePartyColumnsHeight(this.invoice));
    this.cursorY = drawPartyColumns(this.currentCanvas, this.cursorY, this.invoice);
    this.cursorY -= CARD_GAP;
  }

  drawShiftGroups(): void {
    const weekGroups = buildShiftWeekGroups(this.invoice);
    let needsSectionTitle = true;

    weekGroups.forEach((group, index) => {
      const requiredHeight =
        (needsSectionTitle ? measureShiftSectionTitleHeight() : 0) +
        measureShiftWeekMatrixHeight();
      if ((this.cursorY - requiredHeight) < CONTENT_BOTTOM_Y) {
        this.startNewPage();
        needsSectionTitle = true;
      }

      if (needsSectionTitle) {
        this.currentCanvas.drawText(PAGE_MARGIN, this.cursorY - 12, "Shifts worked", {
          font: "F2",
          size: TITLE_FONT_SIZE,
          color: TEXT,
        });
        this.cursorY -= 28;
        needsSectionTitle = false;
      }

      this.cursorY = drawShiftWeekMatrix(this.currentCanvas, this.cursorY, group, this.invoice);
      if (index < weekGroups.length - 1) {
        this.cursorY -= 10;
      }
    });
    this.cursorY -= CARD_GAP;
  }

  drawTotalsCard(): void {
    this.ensureSpace(measureTotalsCardHeight());
    this.cursorY = drawTotalsCard(this.currentCanvas, this.cursorY, this.invoice);
    this.cursorY -= CARD_GAP;
  }

  build(): Buffer {
    const totalPages = this.pages.length;
    this.pages.forEach((page, index) => {
      drawFooter(page, this.invoice, index + 1, totalPages);
    });
    return buildPdf(this.pages.map((page) => page.serialize()));
  }

  private startNewPage(): void {
    this.currentCanvas = new PdfCanvas();
    this.pages.push(this.currentCanvas);
    this.cursorY = PAGE_HEIGHT - PAGE_MARGIN;
    drawHeader(this.currentCanvas, this.invoice, this.cursorY);
    this.cursorY -= HEADER_HEIGHT + CARD_GAP;
  }

  private ensureSpace(requiredHeight: number): void {
    if ((this.cursorY - requiredHeight) < CONTENT_BOTTOM_Y) {
      this.startNewPage();
    }
  }
}

function buildPaymentLines(invoice: InvoiceDetail): string[] {
  return [
    formatPaymentMethod(invoice.profile.paymentMethod),
    invoice.profile.accountNumber
      ? `Account number: ${invoice.profile.accountNumber}`
      : "",
    invoice.profile.sortCode ? `Sort code: ${invoice.profile.sortCode}` : "",
    invoice.profile.paymentInstructions || "",
  ].filter(Boolean);
}

function measureSectionCardHeight(section: SectionCard): number {
  const wrappedLines = section.lines
    .map((line) => wrapText(line, CONTENT_WIDTH - (CARD_PADDING * 2), BODY_FONT_SIZE))
    .flat();
  return (
    CARD_PADDING +
    TITLE_FONT_SIZE +
    10 +
    (wrappedLines.length * SECTION_LINE_HEIGHT) +
    CARD_PADDING
  );
}

function measurePartyColumnsHeight(invoice: InvoiceDetail): number {
  const columnGap = 20;
  const innerWidth = CONTENT_WIDTH - (CARD_PADDING * 2);
  const columnWidth = (innerWidth - columnGap) / 2;
  const leftLines = [
    invoice.profile.fullName,
    invoice.profile.address,
    `Badge number: ${invoice.profile.badgeNumber}`,
    `Badge expiry: ${invoice.profile.badgeExpiryDate}`,
    `UTR number: ${invoice.profile.utrNumber}`,
    `Email: ${invoice.profile.email}`,
    invoice.profile.contactPhone ? `Phone: ${invoice.profile.contactPhone}` : "",
  ].filter(Boolean);
  const rightLines = [
    invoice.venue.venueName,
    invoice.venue.venueAddress || "Venue address not provided",
    `Service period ending: ${invoice.weekEndingDate}`,
    `Invoice currency: ${invoice.currency}`,
    `Hourly rate: ${invoice.hourlyRate.toFixed(2)} ${invoice.currency}`,
  ];

  const wrappedLeft = leftLines.map((line) => wrapText(line, columnWidth, BODY_FONT_SIZE)).flat();
  const wrappedRight = rightLines.map((line) => wrapText(line, columnWidth, BODY_FONT_SIZE)).flat();
  const lineCount = Math.max(wrappedLeft.length, wrappedRight.length);
  return (
    CARD_PADDING +
    TITLE_FONT_SIZE +
    10 +
    BODY_FONT_SIZE +
    12 +
    (lineCount * SECTION_LINE_HEIGHT) +
    CARD_PADDING
  );
}

function measureShiftSectionTitleHeight(): number {
  return 28;
}

function measureShiftWeekMatrixHeight(): number {
  return 24 + TABLE_HEADER_HEIGHT + (TABLE_ROW_HEIGHT * 3);
}

function measureTotalsCardHeight(): number {
  return 120;
}

type ShiftWeekGroup = {
  weekEndingDate: string;
  shifts: Array<{
    dayLabel: string;
    workDate: string;
    hours: number;
    amount: number;
  }>;
};

function buildShiftWeekGroups(invoice: InvoiceDetail): ShiftWeekGroup[] {
  const workedShifts = invoice.weekly.shifts.filter(
    (shift) => (Number(shift.hoursInput) || 0) > 0
  );
  const sourceShifts = workedShifts.length > 0 ? workedShifts : invoice.weekly.shifts;
  const groups = new Map<string, ShiftWeekGroup>();

  sourceShifts.forEach((shift) => {
    const workDate = shift.workDate || invoice.weekly.weekEndingDate || invoice.weekEndingDate;
    const weekEndingDate = resolveWeekEndingDate(workDate, invoice.weekly.weekEndingDate || invoice.weekEndingDate);
    const group = groups.get(weekEndingDate) ?? {
      weekEndingDate,
      shifts: [],
    };

    group.shifts.push({
      dayLabel: shift.dayLabel,
      workDate,
      hours: Number(shift.hoursInput) || 0,
      amount: (Number(shift.hoursInput) || 0) * invoice.hourlyRate,
    });
    groups.set(weekEndingDate, group);
  });

  return Array.from(groups.values())
    .map((group) => ({
      ...group,
      shifts: group.shifts.sort((left, right) => daySortValue(left.dayLabel) - daySortValue(right.dayLabel)),
    }))
    .sort((left, right) => left.weekEndingDate.localeCompare(right.weekEndingDate));
}

function drawShiftWeekMatrix(
  canvas: PdfCanvas,
  topY: number,
  group: ShiftWeekGroup,
  invoice: InvoiceDetail
): number {
  canvas.drawText(PAGE_MARGIN, topY - 12, `Week ending ${group.weekEndingDate}`, {
    font: "F2",
    size: BODY_FONT_SIZE,
    color: TEXT,
  });

  const tableTop = topY - 24;
  const labelColumnWidth = 96;
  const dayCount = Math.max(1, group.shifts.length);
  const dayColumnWidth = (CONTENT_WIDTH - labelColumnWidth) / dayCount;
  const tableHeight = TABLE_HEADER_HEIGHT + (TABLE_ROW_HEIGHT * 3);
  const tableBottom = tableTop - tableHeight;
  const headerFontSize = dayCount >= 6 ? SMALL_FONT_SIZE : BODY_FONT_SIZE;

  canvas.drawRect(PAGE_MARGIN, tableBottom, CONTENT_WIDTH, tableHeight, {
    fillColor: SURFACE,
    strokeColor: BORDER,
  });
  canvas.drawRect(PAGE_MARGIN, tableTop - TABLE_HEADER_HEIGHT, CONTENT_WIDTH, TABLE_HEADER_HEIGHT, {
    fillColor: ACCENT,
    strokeColor: ACCENT,
  });

  canvas.drawText(PAGE_MARGIN + 10, tableTop - 17, "Metric", {
    font: "F2",
    size: BODY_FONT_SIZE,
    color: WHITE,
  });

  let columnX = PAGE_MARGIN + labelColumnWidth;
  group.shifts.forEach((shift) => {
    canvas.drawLine(columnX, tableBottom, columnX, tableTop, BORDER, 0.8);
    canvas.drawText(columnX + 8, tableTop - 17, shift.dayLabel, {
      font: "F2",
      size: headerFontSize,
      color: WHITE,
    });
    columnX += dayColumnWidth;
  });
  canvas.drawLine(PAGE_MARGIN + CONTENT_WIDTH, tableBottom, PAGE_MARGIN + CONTENT_WIDTH, tableTop, BORDER, 0.8);

  const rows = [
    {
      label: "Date",
      values: group.shifts.map((shift) => shift.workDate),
    },
    {
      label: "Hours worked",
      values: group.shifts.map((shift) => formatHours(`${shift.hours}`)),
    },
    {
      label: "Amount",
      values: group.shifts.map((shift) => `${shift.amount.toFixed(2)} ${invoice.currency}`),
    },
  ];

  rows.forEach((row, rowIndex) => {
    const rowTop = tableTop - TABLE_HEADER_HEIGHT - (rowIndex * TABLE_ROW_HEIGHT);
    const rowBottom = rowTop - TABLE_ROW_HEIGHT;
    if (rowIndex % 2 === 0) {
      canvas.drawRect(PAGE_MARGIN, rowBottom, CONTENT_WIDTH, TABLE_ROW_HEIGHT, {
        fillColor: SURFACE_ALT,
      });
    }
    canvas.drawLine(PAGE_MARGIN, rowBottom, PAGE_MARGIN + CONTENT_WIDTH, rowBottom, BORDER, 0.8);
    canvas.drawText(PAGE_MARGIN + 10, rowTop - 16, row.label, {
      font: "F2",
      size: BODY_FONT_SIZE,
      color: TEXT,
    });

    let valueX = PAGE_MARGIN + labelColumnWidth;
    row.values.forEach((value) => {
      const wrapped = wrapText(value, dayColumnWidth - 12, SMALL_FONT_SIZE);
      const firstLine = wrapped[0] ?? "";
      canvas.drawText(valueX + 6, rowTop - 16, firstLine, {
        size: SMALL_FONT_SIZE,
        color: TEXT,
      });
      valueX += dayColumnWidth;
    });
  });

  return tableBottom;
}

function formatPaymentMethod(method: string): string {
  return method === "bank_transfer" ? "Payment method: Bank transfer" : `Payment method: ${method}`;
}

function wrapText(text: string, maxWidth: number, fontSize: number): string[] {
  const normalized = text.trim();
  if (!normalized) return [""];

  const words = normalized.split(/\s+/);
  const lines: string[] = [];
  let currentLine = "";

  words.forEach((word) => {
    const nextLine = currentLine ? `${currentLine} ${word}` : word;
    if (measureTextWidth(nextLine, fontSize) <= maxWidth) {
      currentLine = nextLine;
      return;
    }

    if (currentLine) {
      lines.push(currentLine);
      currentLine = word;
      return;
    }

    lines.push(word);
  });

  if (currentLine) {
    lines.push(currentLine);
  }

  return lines;
}

class PdfCanvas {
  private readonly commands: string[] = [];

  drawText(x: number, y: number, text: string, options: TextOptions = {}): void {
    if (!text) return;
    const font = options.font ?? "F1";
    const size = options.size ?? BODY_FONT_SIZE;
    const color = options.color ?? TEXT;
    this.commands.push("BT");
    this.commands.push(`/${font} ${size.toFixed(2)} Tf`);
    this.commands.push(`${color[0]} ${color[1]} ${color[2]} rg`);
    this.commands.push(`1 0 0 1 ${formatNumber(x)} ${formatNumber(y)} Tm`);
    this.commands.push(`(${escapePdfText(text)}) Tj`);
    this.commands.push("ET");
  }

  drawTextRight(rightX: number, y: number, text: string, options: TextOptions = {}): void {
    const size = options.size ?? BODY_FONT_SIZE;
    const x = rightX - measureTextWidth(text, size, options.font ?? "F1");
    this.drawText(x, y, text, options);
  }

  drawRect(
    x: number,
    y: number,
    width: number,
    height: number,
    options: {
      fillColor?: PdfColor;
      strokeColor?: PdfColor;
      lineWidth?: number;
    }
  ): void {
    this.commands.push("q");
    if (options.fillColor) {
      this.commands.push(`${options.fillColor[0]} ${options.fillColor[1]} ${options.fillColor[2]} rg`);
    }
    if (options.strokeColor) {
      this.commands.push(`${options.strokeColor[0]} ${options.strokeColor[1]} ${options.strokeColor[2]} RG`);
    }
    this.commands.push(`${formatNumber(options.lineWidth ?? 1)} w`);
    this.commands.push(`${formatNumber(x)} ${formatNumber(y)} ${formatNumber(width)} ${formatNumber(height)} re`);
    if (options.fillColor && options.strokeColor) {
      this.commands.push("B");
    } else if (options.fillColor) {
      this.commands.push("f");
    } else if (options.strokeColor) {
      this.commands.push("S");
    }
    this.commands.push("Q");
  }

  drawLine(
    x1: number,
    y1: number,
    x2: number,
    y2: number,
    color: PdfColor,
    lineWidth = 1
  ): void {
    this.commands.push("q");
    this.commands.push(`${color[0]} ${color[1]} ${color[2]} RG`);
    this.commands.push(`${formatNumber(lineWidth)} w`);
    this.commands.push(`${formatNumber(x1)} ${formatNumber(y1)} m`);
    this.commands.push(`${formatNumber(x2)} ${formatNumber(y2)} l`);
    this.commands.push("S");
    this.commands.push("Q");
  }

  drawCircle(
    centerX: number,
    centerY: number,
    radius: number,
    options: {
      fillColor?: PdfColor;
      strokeColor?: PdfColor;
      lineWidth?: number;
    }
  ): void {
    this.commands.push("q");
    if (options.fillColor) {
      this.commands.push(`${options.fillColor[0]} ${options.fillColor[1]} ${options.fillColor[2]} rg`);
    }
    if (options.strokeColor) {
      this.commands.push(`${options.strokeColor[0]} ${options.strokeColor[1]} ${options.strokeColor[2]} RG`);
    }
    this.commands.push(`${formatNumber(options.lineWidth ?? 1)} w`);
    this.commands.push(circlePath(centerX, centerY, radius));
    if (options.fillColor && options.strokeColor) {
      this.commands.push("B");
    } else if (options.fillColor) {
      this.commands.push("f");
    } else if (options.strokeColor) {
      this.commands.push("S");
    }
    this.commands.push("Q");
  }

  drawRing(
    outerCenterX: number,
    outerCenterY: number,
    outerRadius: number,
    innerCenterX: number,
    innerCenterY: number,
    innerRadius: number,
    fillColor: PdfColor
  ): void {
    this.commands.push("q");
    this.commands.push(`${fillColor[0]} ${fillColor[1]} ${fillColor[2]} rg`);
    this.commands.push(circlePath(outerCenterX, outerCenterY, outerRadius));
    this.commands.push(circlePath(innerCenterX, innerCenterY, innerRadius));
    this.commands.push("f*");
    this.commands.push("Q");
  }

  serialize(): string {
    return this.commands.join("\n");
  }
}

function drawPaySmartLogo(canvas: PdfCanvas, x: number, y: number, size: number): void {
  const centerX = x + (size / 2);
  const centerY = y + (size / 2);
  const radius = size / 2.15;

  canvas.drawCircle(centerX, centerY, radius, {
    strokeColor: WHITE,
    lineWidth: 1.6,
  });
  canvas.drawCircle(centerX + (size * 0.23), centerY + (size * 0.23), size * 0.08, {
    fillColor: LOGO_GREEN,
  });
  canvas.drawText(centerX - (size * 0.26), centerY - (size * 0.16), "PS", {
    font: "F2",
    size: size * 0.42,
    color: WHITE,
  });
}

function measureTextWidth(text: string, fontSize: number, font: PdfFont = "F1"): number {
  const factor = font === "F2" ? 0.56 : 0.52;
  return text.length * fontSize * factor;
}

function rgb(red: number, green: number, blue: number): PdfColor {
  return [
    Number((red / 255).toFixed(3)),
    Number((green / 255).toFixed(3)),
    Number((blue / 255).toFixed(3)),
  ];
}

function formatNumber(value: number): string {
  return value.toFixed(2);
}

function formatMinor(minor: number, currency: string): string {
  return `${(minor / 100).toFixed(2)} ${currency}`;
}

function formatHours(value: string): string {
  const hours = Number(value) || 0;
  return Number.isInteger(hours) ? `${hours}` : hours.toFixed(2);
}

function resolveWeekEndingDate(workDate: string, fallback: string): string {
  const parsed = parseIsoDate(workDate);
  if (!parsed) return fallback;

  const date = new Date(Date.UTC(parsed.year, parsed.month - 1, parsed.day));
  const daysUntilSunday = (7 - date.getUTCDay()) % 7;
  date.setUTCDate(date.getUTCDate() + daysUntilSunday);
  return formatDate(date);
}

function parseIsoDate(raw: string): { year: number; month: number; day: number } | null {
  const match = /^(\d{4})-(\d{2})-(\d{2})$/.exec(raw.trim());
  if (!match) return null;
  return {
    year: Number(match[1]),
    month: Number(match[2]),
    day: Number(match[3]),
  };
}

function formatDate(date: Date): string {
  const year = date.getUTCFullYear();
  const month = String(date.getUTCMonth() + 1).padStart(2, "0");
  const day = String(date.getUTCDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function daySortValue(dayLabel: string): number {
  const normalized = dayLabel.trim().toLowerCase();
  const index = [
    "monday",
    "tuesday",
    "wednesday",
    "thursday",
    "friday",
    "saturday",
    "sunday",
  ].indexOf(normalized);
  return index >= 0 ? index : Number.MAX_SAFE_INTEGER;
}

function circlePath(centerX: number, centerY: number, radius: number): string {
  const kappa = 0.552284749831 * radius;
  const left = centerX - radius;
  const right = centerX + radius;
  const top = centerY + radius;
  const bottom = centerY - radius;

  return [
    `${formatNumber(centerX)} ${formatNumber(top)} m`,
    `${formatNumber(centerX + kappa)} ${formatNumber(top)} ${formatNumber(right)} ${formatNumber(centerY + kappa)} ${formatNumber(right)} ${formatNumber(centerY)} c`,
    `${formatNumber(right)} ${formatNumber(centerY - kappa)} ${formatNumber(centerX + kappa)} ${formatNumber(bottom)} ${formatNumber(centerX)} ${formatNumber(bottom)} c`,
    `${formatNumber(centerX - kappa)} ${formatNumber(bottom)} ${formatNumber(left)} ${formatNumber(centerY - kappa)} ${formatNumber(left)} ${formatNumber(centerY)} c`,
    `${formatNumber(left)} ${formatNumber(centerY + kappa)} ${formatNumber(centerX - kappa)} ${formatNumber(top)} ${formatNumber(centerX)} ${formatNumber(top)} c`,
  ].join("\n");
}

function escapePdfText(value: string): string {
  return value
    .replace(/\\/g, "\\\\")
    .replace(/\(/g, "\\(")
    .replace(/\)/g, "\\)");
}

function buildPdf(pageStreams: string[]): Buffer {
  const objects: string[] = [
    "<< /Type /Catalog /Pages 2 0 R >>",
    "PAGES_PLACEHOLDER",
  ];
  const pageObjectNumbers: number[] = [];

  pageStreams.forEach((contentStream) => {
    const pageObjectNumber = objects.length + 1;
    const contentObjectNumber = objects.length + 2;
    pageObjectNumbers.push(pageObjectNumber);
    objects.push(
      `<< /Type /Page /Parent 2 0 R /MediaBox [0 0 ${PAGE_WIDTH} ${PAGE_HEIGHT}] /Resources << /Font << /F1 ${pageStreams.length * 2 + 3} 0 R /F2 ${pageStreams.length * 2 + 4} 0 R >> >> /Contents ${contentObjectNumber} 0 R >>`
    );
    objects.push(
      `<< /Length ${Buffer.byteLength(contentStream, "utf8")} >>\nstream\n${contentStream}\nendstream`
    );
  });

  objects.push("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
  objects.push("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>");
  objects[1] = `<< /Type /Pages /Kids [${pageObjectNumbers.map((number) => `${number} 0 R`).join(" ")}] /Count ${pageStreams.length} >>`;

  const chunks = ["%PDF-1.4\n"];
  const offsets: number[] = [0];
  let currentLength = Buffer.byteLength(chunks[0], "utf8");

  objects.forEach((object, index) => {
    offsets.push(currentLength);
    const entry = `${index + 1} 0 obj\n${object}\nendobj\n`;
    chunks.push(entry);
    currentLength += Buffer.byteLength(entry, "utf8");
  });

  const xrefStart = currentLength;
  const xref = [
    `xref\n0 ${objects.length + 1}\n`,
    "0000000000 65535 f \n",
    ...offsets.slice(1).map((offset) => `${String(offset).padStart(10, "0")} 00000 n \n`),
    `trailer\n<< /Size ${objects.length + 1} /Root 1 0 R >>\nstartxref\n${xrefStart}\n%%EOF`,
  ].join("");
  chunks.push(xref);

  return Buffer.from(chunks.join(""), "utf8");
}
