package net.metalbrain.paysmart.core.auth

import net.metalbrain.paysmart.core.auth.AuthPolicyClient.Companion.defaultClient
import okhttp3.OkHttpClient


class FederatedLinkingPolicy(
    private val config: AuthApiConfig,
    private val httpClient: OkHttpClient = defaultClient
)
