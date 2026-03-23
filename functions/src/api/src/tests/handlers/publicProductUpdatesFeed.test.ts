import { beforeEach, describe, expect, it, vi } from "vitest";

const listPublishedCampaigns = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    firestore: {},
  }),
}));

vi.mock(
  "../../infrastructure/firestore/FirestoreProductUpdateCampaignRepository.js",
  () => ({
    FirestoreProductUpdateCampaignRepository: class {
      listPublishedCampaigns = listPublishedCampaigns;
    },
  })
);

import {
  mapCampaignToPublicProductUpdateFeedItem,
  publicProductUpdatesFeedHandler,
} from "../../handlers/publicProductUpdatesFeed.js";

function createResponseRecorder() {
  return {
    headers: {} as Record<string, string>,
    statusCode: 200,
    payload: undefined as unknown,
    setHeader(name: string, value: string) {
      this.headers[name] = value;
      return this;
    },
    status(code: number) {
      this.statusCode = code;
      return this;
    },
    json(body: unknown) {
      this.payload = body;
      return this;
    },
  };
}

describe("publicProductUpdatesFeedHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns published campaigns in the public website feed shape", async () => {
    listPublishedCampaigns.mockResolvedValue([
      {
        campaignId: "campaign-1",
        status: "sent",
        title: "Identity flow refresh",
        subject: "Identity flow refresh",
        summary: "Identity updates are now live.",
        body: "We redesigned the review path.",
        area: "Identity",
        releaseStatus: "Shipped",
        highlights: ["Intro screen", "Pending review state"],
        ctaLabel: "Read the update",
        ctaUrl: "https://pay-smart.net/updates/",
        sendPush: true,
        sendEmail: true,
        sentAtMs: Date.UTC(2026, 2, 20, 9, 30, 0),
      },
    ]);
    const req = { query: {} };
    const res = createResponseRecorder();

    await publicProductUpdatesFeedHandler(req as any, res as any);

    expect(listPublishedCampaigns).toHaveBeenCalledWith(20);
    expect(res.statusCode).toBe(200);
    expect(res.headers["Cache-Control"]).toBe("public, max-age=300");
    expect(res.headers["Content-Type"]).toBe("application/json; charset=utf-8");
    expect(res.payload).toEqual([
      {
        campaignId: "campaign-1",
        date: "2026-03-20",
        area: "Identity",
        status: "Shipped",
        title: "Identity flow refresh",
        summary: "Identity updates are now live.",
        highlights: ["Intro screen", "Pending review state"],
        body: "We redesigned the review path.",
        ctaLabel: "Read the update",
        ctaUrl: "https://pay-smart.net/updates/",
      },
    ]);
  });

  it("clamps the optional limit query parameter", async () => {
    listPublishedCampaigns.mockResolvedValue([]);
    const req = { query: { limit: "500" } };
    const res = createResponseRecorder();

    await publicProductUpdatesFeedHandler(req as any, res as any);

    expect(listPublishedCampaigns).toHaveBeenCalledWith(50);
    expect(res.statusCode).toBe(200);
    expect(res.payload).toEqual([]);
  });

  it("returns a 500 payload when feed loading fails", async () => {
    listPublishedCampaigns.mockRejectedValue(new Error("firestore offline"));
    const req = { query: {} };
    const res = createResponseRecorder();

    await publicProductUpdatesFeedHandler(req as any, res as any);

    expect(res.statusCode).toBe(500);
    expect(res.headers["Cache-Control"]).toBe("no-cache");
    expect(res.payload).toEqual({ error: "Could not load product updates" });
  });
});

describe("mapCampaignToPublicProductUpdateFeedItem", () => {
  it("applies stable defaults for optional fields", () => {
    const item = mapCampaignToPublicProductUpdateFeedItem({
      campaignId: "campaign-2",
      status: "sent",
      title: "FX cards improved",
      subject: "FX cards improved",
      summary: "Exchange rate rows are cleaner on small screens.",
      highlights: [],
      sendPush: true,
      sendEmail: true,
      sentAtMs: Date.UTC(2026, 2, 18, 12, 0, 0),
    });

    expect(item).toEqual({
      campaignId: "campaign-2",
      date: "2026-03-18",
      area: "General",
      status: "Shipped",
      title: "FX cards improved",
      summary: "Exchange rate rows are cleaner on small screens.",
      highlights: [],
    });
  });
});
