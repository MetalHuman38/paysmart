package net.metalbrain.paysmart.core.features.account.authentication.email.data

/**
 * Gateway interface responsible for handling notifications related to the email verification process.
 * This component abstracts the synchronization of verification status with external systems or services.
 */
interface EmailVerificationNotificationGateway {
    suspend fun syncEmailVerifiedNotification(uid: String, email: String?)
}
