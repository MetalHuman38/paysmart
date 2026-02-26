import {
  IdentityProviderCallbackInput,
  IdentityProviderSession,
  IdentityProviderSessionResume,
  ResumeIdentityProviderSessionInput,
  StartIdentityProviderSessionInput,
} from "../model/identityProvider.js";

export interface IdentityProviderRepository {
  startSession(
    uid: string,
    input: StartIdentityProviderSessionInput
  ): Promise<IdentityProviderSession>;

  resumeSession(
    uid: string,
    input: ResumeIdentityProviderSessionInput
  ): Promise<IdentityProviderSessionResume>;

  submitCallback(
    uid: string,
    input: IdentityProviderCallbackInput
  ): Promise<IdentityProviderSessionResume>;
}
