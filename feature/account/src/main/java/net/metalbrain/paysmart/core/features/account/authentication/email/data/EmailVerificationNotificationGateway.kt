package net.metalbrain.paysmart.core.features.account.authentication.email.data

interface EmailVerificationNotificationGateway {
    suspend fun syncEmailVerifiedNotification(uid: String, email: String?)
}
