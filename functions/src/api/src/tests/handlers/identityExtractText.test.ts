import { beforeEach, describe, expect, it, vi } from "vitest";

const verifyIdToken = vi.fn();
const extract = vi.fn();

vi.mock("../../dependencies.js", () => ({
  initDeps: () => ({
    auth: {
      verifyIdToken,
    },
    getConfig: () => ({
      identityMaxPayloadBytes: 1024 * 1024,
    }),
  }),
}));

vi.mock("../../infrastructure/di/authContainer.js", () => ({
  authContainer: () => ({
    identityTextExtraction: {
      extract,
    },
  }),
}));

import { identityExtractTextHandler } from "../../handlers/identityExtractText.js";

type TestReq = {
  headers: {
    authorization?: string;
  };
  body?: Record<string, unknown>;
};

function createResponseRecorder() {
  return {
    statusCode: 200,
    payload: undefined as unknown,
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

describe("identityExtractTextHandler", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("returns 401 when token is missing", async () => {
    const req = {
      headers: {},
      body: {},
    } as TestReq;
    const res = createResponseRecorder();

    await identityExtractTextHandler(req as any, res as any);

    expect(res.statusCode).toBe(401);
    expect(res.payload).toEqual({ error: "Missing token" });
    expect(verifyIdToken).not.toHaveBeenCalled();
  });

  it("returns extraction payload on success", async () => {
    verifyIdToken.mockResolvedValue({ uid: "uid-1" });
    extract.mockResolvedValue({
      fullText: "P<GBRSMITH<<JOHN<PAUL",
      candidateFullName: "JOHN PAUL SMITH",
      provider: "google_cloud_vision_document_text_detection_v1",
    });

    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        mimeType: "image/jpeg",
        payloadBase64: Buffer.from("test").toString("base64"),
      },
    } as TestReq;
    const res = createResponseRecorder();

    await identityExtractTextHandler(req as any, res as any);

    expect(res.statusCode).toBe(200);
    expect(verifyIdToken).toHaveBeenCalledWith("token-1");
    expect(extract).toHaveBeenCalled();
    expect(res.payload).toEqual(
      expect.objectContaining({
        candidateFullName: "JOHN PAUL SMITH",
      })
    );
  });

  it("returns 400 when payloadBase64 is missing", async () => {
    const req = {
      headers: {
        authorization: "Bearer token-1",
      },
      body: {
        mimeType: "image/jpeg",
      },
    } as TestReq;
    const res = createResponseRecorder();

    await identityExtractTextHandler(req as any, res as any);

    expect(res.statusCode).toBe(400);
    expect(res.payload).toEqual({ error: "Missing payloadBase64" });
  });
});
