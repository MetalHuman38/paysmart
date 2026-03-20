import { afterEach, describe, expect, it, vi } from "vitest";
import {
  FlutterwavePaymentsService,
  buildFlutterwaveOauthTokenUrl,
} from "../../services/flutterwavePaymentsService.js";

describe("buildFlutterwaveOauthTokenUrl", () => {
  it("maps a host-only base url to the documented token endpoint", () => {
    expect(buildFlutterwaveOauthTokenUrl("https://idp.flutterwave.com")).toBe(
      "https://idp.flutterwave.com/realms/flutterwave/protocol/openid-connect/token"
    );
  });

  it("upgrades the legacy oauth2 token path to the documented endpoint", () => {
    expect(
      buildFlutterwaveOauthTokenUrl("https://idp.flutterwave.com/oauth2/token")
    ).toBe(
      "https://idp.flutterwave.com/realms/flutterwave/protocol/openid-connect/token"
    );
  });
});

describe("FlutterwavePaymentsService oauth auth", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("requests oauth tokens from the documented token endpoint", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      jsonResponse({
        access_token: "token-123",
        expires_in: 600,
      })
    );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    const header = await (service as any).resolveAuthorizationHeader();

    expect(header).toBe("Bearer token-123");
    expect(fetchMock).toHaveBeenCalledWith(
      "https://idp.flutterwave.com/realms/flutterwave/protocol/openid-connect/token",
      expect.objectContaining({
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/x-www-form-urlencoded",
        },
      })
    );
  });

  it("surfaces oauth error descriptions when auth fails", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        jsonResponse(
          {
            error: "invalid_client",
            error_description: "Client authentication failed",
          },
          {
            ok: false,
            status: 403,
          }
        )
      )
    );

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com/oauth2/token",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    await expect((service as any).resolveAuthorizationHeader()).rejects.toThrow(
      "Client authentication failed"
    );
  });
});

describe("FlutterwavePaymentsService provider requests", () => {
  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("surfaces Flutterwave validation errors from invalid virtual account requests", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse({
          access_token: "token-123",
          expires_in: 600,
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "customer-123",
          },
        })
      )
      .mockResolvedValueOnce(
        jsonResponse(
          {
            message: "Request is not valid",
            code: "REQUEST_NOT_VALID",
            validation_errors: [
              {
                field: "reference",
                message: "Reference must be unique",
              },
              {
                field: "amount",
                message: "Amount must be greater than zero",
              },
            ],
          },
          {
            ok: false,
            status: 400,
          }
        )
      );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    await expect(
      service.createTopupSession({
        uid: "uid-1",
        amountMinor: 150000,
        currency: "NGN",
        reference: "reference-123",
        customer: {
          email: "ada@example.com",
          firstName: "Ada",
          lastName: "Lovelace",
        },
      })
    ).rejects.toMatchObject({
      name: "FlutterwaveProviderRequestError",
      status: 400,
      code: "REQUEST_NOT_VALID",
      details: [
        "reference: Reference must be unique",
        "amount: Amount must be greater than zero",
      ],
    });

    const request = fetchMock.mock.calls[2][1] as RequestInit;
    expect(request.headers).toEqual(
      expect.objectContaining({
        Accept: "application/json",
      })
    );
  });

  it("uses a customer-name narration for virtual account creation", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse({
          access_token: "token-123",
          expires_in: 600,
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "customer-123",
          },
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "session-123",
            amount: 1500,
            account_number: "4032866864",
            account_bank_name: "WEMA BANK",
            reference: "reference-123",
            status: "pending",
            account_expiration_datetime: "2026-03-10T13:00:00.000Z",
            note: "Please make a bank transfer to Ada Lovelace",
            created_datetime: "2026-03-10T12:00:00.000Z",
          },
        })
      );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    await service.createTopupSession({
      uid: "uid-1",
      amountMinor: 150000,
      currency: "NGN",
      reference: "reference-123",
      customer: {
        email: "ada@example.com",
        firstName: "Ada",
        lastName: "Lovelace",
      },
    });

    const request = fetchMock.mock.calls[2][1] as RequestInit;
    const body = JSON.parse((request.body as string) || "{}");

    expect(body).toMatchObject({
      narration: "Ada Lovelace",
      reference: "reference-123",
      amount: 1500,
      currency: "NGN",
      customer_id: "customer-123",
    });
  });

  it("reuses a cached customer id without creating a new Flutterwave customer", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse({
          access_token: "token-123",
          expires_in: 600,
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "session-123",
            amount: 1500,
            account_number: "4032866864",
            account_bank_name: "WEMA BANK",
            reference: "reference-123",
            status: "pending",
            account_expiration_datetime: "2026-03-10T13:00:00.000Z",
            note: "Please make a bank transfer to Ada Lovelace",
            created_datetime: "2026-03-10T12:00:00.000Z",
          },
        })
      );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    const session = await service.createTopupSession({
      uid: "uid-1",
      amountMinor: 150000,
      currency: "NGN",
      reference: "reference-123",
      customerId: "customer-cached-123",
      customer: {
        email: "ada@example.com",
        firstName: "Ada",
        lastName: "Lovelace",
      },
    });

    expect(session.customerId).toBe("customer-cached-123");
    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock.mock.calls[1][0]).toBe(
      "https://developersandbox-api.flutterwave.com/virtual-accounts"
    );
  });

  it("recovers from existing customer conflicts by looking up the customer email", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse({
          access_token: "token-123",
          expires_in: 600,
        })
      )
      .mockResolvedValueOnce(
        jsonResponse(
          {
            message: "Customer already exists",
            code: "10409",
            type: "RESOURCE_CONFLICT",
          },
          {
            ok: false,
            status: 409,
          }
        )
      )
      .mockResolvedValueOnce(
        jsonResponse(
          {
            message: "Request is not valid",
          },
          {
            ok: false,
            status: 400,
          }
        )
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: [
            {
              id: "customer-existing-123",
              email: "ada@example.com",
            },
          ],
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "session-123",
            amount: 1500,
            account_number: "4032866864",
            account_bank_name: "WEMA BANK",
            reference: "reference-123",
            status: "pending",
            account_expiration_datetime: "2026-03-10T13:00:00.000Z",
            note: "Please make a bank transfer to Ada Lovelace",
            created_datetime: "2026-03-10T12:00:00.000Z",
          },
        })
      );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    const session = await service.createTopupSession({
      uid: "uid-1",
      amountMinor: 150000,
      currency: "NGN",
      reference: "reference-123",
      customer: {
        email: "ada@example.com",
        firstName: "Ada",
        lastName: "Lovelace",
      },
    });

    expect(session.customerId).toBe("customer-existing-123");
    expect(fetchMock.mock.calls[2][0]).toBe(
      "https://developersandbox-api.flutterwave.com/customers?email=ada%40example.com"
    );
    expect(fetchMock.mock.calls[3][0]).toBe(
      "https://developersandbox-api.flutterwave.com/customers/search"
    );
  });

  it("creates a permanent funding account with static account type and injected NG KYC", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse({
          access_token: "token-123",
          expires_in: 600,
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "customer-123",
          },
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "van-123",
            account_number: "1234567890",
            bank_name: "Wema Bank",
            account_name: "Ada Lovelace",
            reference: "funding-ref-123",
            status: "active",
            customer_id: "customer-123",
            note: "Transfer to this account to fund your wallet",
            created_datetime: "2026-03-10T12:00:00.000Z",
            updated_datetime: "2026-03-10T12:05:00.000Z",
          },
        })
      );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    const account = await service.createPermanentFundingAccount({
      uid: "uid-1",
      reference: "funding-ref-123",
      customer: {
        email: "ada@example.com",
        firstName: "Ada",
        lastName: "Lovelace",
      },
      kyc: {
        bvn: "12345678901",
        nin: "10987654321",
      },
    });

    expect(account).toMatchObject({
      accountId: "van-123",
      accountNumber: "1234567890",
      bankName: "Wema Bank",
      accountName: "Ada Lovelace",
      reference: "funding-ref-123",
      status: "active",
      customerId: "customer-123",
    });

    const request = fetchMock.mock.calls[2][1] as RequestInit;
    const body = JSON.parse((request.body as string) || "{}");
    expect(body).toMatchObject({
      reference: "funding-ref-123",
      account_type: "static",
      amount: 0,
      currency: "NGN",
      narration: "Ada Lovelace",
      bvn: "12345678901",
      nin: "10987654321",
      customer_id: "customer-123",
    });
  });

  it("retrieves a permanent funding account by account id", async () => {
    const fetchMock = vi
      .fn()
      .mockResolvedValueOnce(
        jsonResponse({
          access_token: "token-123",
          expires_in: 600,
        })
      )
      .mockResolvedValueOnce(
        jsonResponse({
          data: {
            id: "van-123",
            account_number: "1234567890",
            bank: {
              name: "Wema Bank",
            },
            account_name: "Ada Lovelace",
            reference: "funding-ref-123",
            status: "active",
            customer_id: "customer-123",
            created_datetime: "2026-03-10T12:00:00.000Z",
            updated_datetime: "2026-03-10T12:05:00.000Z",
          },
        })
      );
    vi.stubGlobal("fetch", fetchMock);

    const service = new FlutterwavePaymentsService({
      secretKey: "",
      webhookSecretHash: "",
      allowUnsignedWebhooks: true,
      baseUrl: "https://developersandbox-api.flutterwave.com",
      idpBaseUrl: "https://idp.flutterwave.com",
      clientId: "client-id",
      clientSecret: "client-secret",
      virtualAccountExpirySeconds: 3600,
    });

    const account = await service.retrievePermanentFundingAccount("van-123");

    expect(account.accountId).toBe("van-123");
    expect(account.bankName).toBe("Wema Bank");
    expect(fetchMock.mock.calls[1][0]).toBe(
      "https://developersandbox-api.flutterwave.com/virtual-accounts/van-123"
    );
  });
});

function jsonResponse(
  body: Record<string, unknown>,
  overrides?: {
    ok?: boolean;
    status?: number;
  }
) {
  return {
    ok: overrides?.ok ?? true,
    status: overrides?.status ?? 200,
    text: async () => JSON.stringify(body),
  };
}
