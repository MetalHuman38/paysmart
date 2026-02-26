import {
  IdentityProviderSessionResume,
  ResumeIdentityProviderSessionInput,
} from "../../domain/model/identityProvider.js";
import { IdentityProviderRepository } from "../../domain/repository/IdentityProviderRepository.js";

export class ResumeIdentityProviderSession {
  constructor(private readonly identityProvider: IdentityProviderRepository) {}

  async execute(
    uid: string,
    input: ResumeIdentityProviderSessionInput
  ): Promise<IdentityProviderSessionResume> {
    return this.identityProvider.resumeSession(uid, input);
  }
}
