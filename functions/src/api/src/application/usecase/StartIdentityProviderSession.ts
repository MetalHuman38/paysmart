import {
  IdentityProviderSession,
  StartIdentityProviderSessionInput,
} from "../../domain/model/identityProvider.js";
import { IdentityProviderRepository } from "../../domain/repository/IdentityProviderRepository.js";

export class StartIdentityProviderSession {
  constructor(private readonly identityProvider: IdentityProviderRepository) {}

  async execute(
    uid: string,
    input: StartIdentityProviderSessionInput
  ): Promise<IdentityProviderSession> {
    return this.identityProvider.startSession(uid, input);
  }
}
