import { CloudAccessTokenProvider } from "./googleCloudAccessTokenProvider.js";

type InvoicePdfTaskConfig = {
  projectId: string;
  queue: string;
  location: string;
  targetUrl: string;
  token: string;
};

type InvoicePdfTaskPayload = {
  uid: string;
  invoiceId: string;
};

export class InvoicePdfTaskService {
  constructor(
    private readonly accessTokenProvider: CloudAccessTokenProvider,
    private readonly config: InvoicePdfTaskConfig
  ) {}

  isConfigured(): boolean {
    if ((process.env.FUNCTIONS_EMULATOR || "").toLowerCase() === "true") {
      return false;
    }
    return Boolean(
      this.config.projectId &&
        this.config.queue &&
        this.config.location &&
        this.config.targetUrl &&
        this.config.token
    );
  }

  async enqueue(payload: InvoicePdfTaskPayload): Promise<void> {
    if (!this.isConfigured()) {
      throw new Error("Invoice PDF task queue is not configured");
    }

    const parent = [
      "projects",
      this.config.projectId,
      "locations",
      this.config.location,
      "queues",
      this.config.queue,
    ].join("/");

    const accessToken = await this.accessTokenProvider.getAccessToken([
      "https://www.googleapis.com/auth/cloud-platform",
    ]);

    const response = await fetch(`https://cloudtasks.googleapis.com/v2/${parent}/tasks`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        "Content-Type": "application/json",
      },
      body: JSON.stringify({
        task: {
          httpRequest: {
            httpMethod: "POST",
            url: this.config.targetUrl,
            headers: {
              "Content-Type": "application/json",
              "X-Invoice-Task-Token": this.config.token,
            },
            body: Buffer.from(JSON.stringify(payload), "utf8").toString("base64"),
          },
        },
      }),
    });

    if (!response.ok) {
      const message = await response.text();
      throw new Error(`Failed to enqueue invoice PDF task: ${message || response.status}`);
    }
  }
}
