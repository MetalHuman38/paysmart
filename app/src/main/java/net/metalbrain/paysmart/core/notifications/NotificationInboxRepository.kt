package net.metalbrain.paysmart.core.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import net.metalbrain.paysmart.R
import net.metalbrain.paysmart.domain.auth.UserManager
import net.metalbrain.paysmart.domain.auth.state.AuthState
import net.metalbrain.paysmart.room.dao.NotificationInboxDao
import net.metalbrain.paysmart.room.entity.NotificationInboxEntity
import java.util.concurrent.atomic.AtomicBoolean

@Singleton
@OptIn(ExperimentalCoroutinesApi::class)
class NotificationInboxRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val notificationInboxDao: NotificationInboxDao,
    private val userManager: UserManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val started = AtomicBoolean(false)
    private var registration: ListenerRegistration? = null

    fun start() {
        if (!started.compareAndSet(false, true)) {
            return
        }

        scope.launch {
            userManager.authState.collect { authState ->
                registration?.remove()
                registration = null

                if (authState is AuthState.Authenticated) {
                    registration = observeRemoteInbox(authState.uid)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeLatestNotification(): Flow<NotificationInboxItem?> {
        return userManager.authState.flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> notificationInboxDao.observeLatestForUser(authState.uid)
                    .map { entity -> entity?.toDomain() }

                else -> flowOf(null)
            }
        }
    }

    fun observeNotifications(): Flow<List<NotificationInboxItem>> {
        return userManager.authState.flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> notificationInboxDao.observeAllForUser(authState.uid)
                    .map { entities -> entities.map { it.toDomain() } }

                else -> flowOf(emptyList())
            }
        }
    }


    fun observeUnreadCount(): Flow<Int> {
        return userManager.authState.flatMapLatest { authState ->
            when (authState) {
                is AuthState.Authenticated -> notificationInboxDao.observeUnreadCountForUser(authState.uid)
                else -> flowOf(0)
            }
        }
    }

    suspend fun markAllRead() {
        val uid = currentAuthenticatedUid() ?: return
        val now = System.currentTimeMillis()
        val unreadRemoteIds = notificationInboxDao.getUnreadIdsBySource(uid, NotificationInboxDao.REMOTE_SOURCE)

        notificationInboxDao.markAllReadForUser(uid, now)
        if (unreadRemoteIds.isNotEmpty()) {
            syncRemoteReadState(uid, unreadRemoteIds)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        val uid = currentAuthenticatedUid() ?: return
        val entity = notificationInboxDao.getById(uid, notificationId) ?: return
        val now = System.currentTimeMillis()

        notificationInboxDao.markReadForUser(uid, notificationId, now)
        if (entity.source == NotificationInboxDao.REMOTE_SOURCE) {
            syncRemoteReadState(uid, listOf(notificationId))
        }
    }

    suspend fun syncAppUpdateNotification(
        uid: String,
        showRestartPrompt: Boolean,
        versionCode: Int?,
    ) {
        if (!showRestartPrompt || versionCode == null) {
            notificationInboxDao.deleteBySourceAndType(
                userId = uid,
                source = LOCAL_SOURCE,
                type = TYPE_APP_UPDATE_READY
            )
            return
        }

        val existing = notificationInboxDao.getById(uid, APP_UPDATE_NOTIFICATION_ID)
        val now = System.currentTimeMillis()
        notificationInboxDao.upsert(
            NotificationInboxEntity(
                userId = uid,
                notificationId = APP_UPDATE_NOTIFICATION_ID,
                source = LOCAL_SOURCE,
                type = TYPE_APP_UPDATE_READY,
                channel = NotificationChannels.APP_UPDATES,
                title = context.getString(R.string.home_notification_update_title),
                body = context.getString(R.string.app_update_downloaded_message),
                deepLink = null,
                isUnread = existing?.isUnread ?: true,
                createdAtMs = existing?.createdAtMs ?: now,
                updatedAtMs = now
            )
        )
    }

    suspend fun syncEmailVerifiedNotification(
        uid: String,
        email: String?,
    ) {
        val existing = notificationInboxDao.getById(uid, EMAIL_VERIFIED_NOTIFICATION_ID)
        val now = System.currentTimeMillis()
        val resolvedBody = email?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let { verifiedEmail ->
                context.getString(
                    R.string.email_verification_success_subtitle
                ) + "\n" + verifiedEmail
            }
            ?: context.getString(R.string.email_verification_success_subtitle)

        notificationInboxDao.upsert(
            NotificationInboxEntity(
                userId = uid,
                notificationId = EMAIL_VERIFIED_NOTIFICATION_ID,
                source = LOCAL_SOURCE,
                type = TYPE_EMAIL_VERIFIED,
                channel = NotificationChannels.ACCOUNT_UPDATES,
                title = context.getString(R.string.email_verification_success_title),
                body = resolvedBody,
                deepLink = null,
                isUnread = existing?.isUnread ?: true,
                createdAtMs = existing?.createdAtMs ?: now,
                updatedAtMs = now
            )
        )
    }

    private fun observeRemoteInbox(uid: String): ListenerRegistration {
        return firestore.collection("users")
            .document(uid)
            .collection("notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(MAX_REMOTE_NOTIFICATIONS.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Remote inbox listener failed for uid=$uid", error)
                    return@addSnapshotListener
                }

                val entities = snapshot?.documents
                    ?.mapNotNull { document ->
                        val title = document.getString("title")?.trim().orEmpty()
                        val body = document.getString("body")?.trim().orEmpty()
                        if (title.isBlank() || body.isBlank()) {
                            return@mapNotNull null
                        }

                        val createdAtMs = timestampMillis(document.get("createdAt")) ?: System.currentTimeMillis()
                        NotificationInboxEntity(
                            userId = uid,
                            notificationId = document.id,
                            source = NotificationInboxDao.REMOTE_SOURCE,
                            type = document.getString("type")?.trim().orEmpty().ifBlank { "general" },
                            channel = document.getString("channel")?.trim().orEmpty().ifBlank { "account_updates" },
                            title = title,
                            body = body,
                            deepLink = document.getString("deepLink")?.trim()?.takeIf { it.isNotEmpty() },
                            isUnread = document.get("readAt") == null,
                            createdAtMs = createdAtMs,
                            updatedAtMs = timestampMillis(document.get("updatedAt")) ?: createdAtMs
                        )
                    }
                    .orEmpty()

                scope.launch {
                    notificationInboxDao.replaceRemoteForUser(uid, entities)
                }
            }
    }

    private fun NotificationInboxEntity.toDomain(): NotificationInboxItem {
        return NotificationInboxItem(
            notificationId = notificationId,
            source = source,
            type = type,
            channel = channel,
            title = title,
            body = body,
            deepLink = deepLink,
            isUnread = isUnread,
            createdAtMs = createdAtMs,
            updatedAtMs = updatedAtMs
        )
    }

    private fun timestampMillis(value: Any?): Long? {
        return when (value) {
            is Timestamp -> value.toDate().time
            is Number -> value.toLong()
            else -> null
        }
    }

    private suspend fun syncRemoteReadState(uid: String, notificationIds: List<String>) {
        runCatching {
            val batch = firestore.batch()
            notificationIds.forEach { notificationId ->
                val ref = firestore.collection("users")
                    .document(uid)
                    .collection("notifications")
                    .document(notificationId)
                batch.update(
                    ref,
                    mapOf(
                        "readAt" to FieldValue.serverTimestamp(),
                        "updatedAt" to FieldValue.serverTimestamp(),
                    )
                )
            }
            batch.commit().await()
        }.onFailure { error ->
            Log.w(TAG, "Failed to sync remote read state for uid=$uid", error)
        }
    }

    private suspend fun currentAuthenticatedUid(): String? {
        return (userManager.authState.first { it !is AuthState.Loading } as? AuthState.Authenticated)?.uid
    }

    companion object {
        const val TAG = "NotificationInboxRepo"
        const val MAX_REMOTE_NOTIFICATIONS = 50
        const val LOCAL_SOURCE = "local"
        const val TYPE_APP_UPDATE_READY = "app_update_ready"
        const val TYPE_EMAIL_VERIFIED = "email_verified"
        private const val APP_UPDATE_NOTIFICATION_ID = "local_app_update_ready"
        private const val EMAIL_VERIFIED_NOTIFICATION_ID = "local_email_verified"
    }
}
