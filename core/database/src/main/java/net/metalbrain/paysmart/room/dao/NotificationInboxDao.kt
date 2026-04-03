package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.NotificationInboxEntity

@Dao
interface NotificationInboxDao {

    @Query(
        """
        SELECT * FROM notification_inbox
        WHERE userId = :userId
        ORDER BY isUnread DESC, createdAtMs DESC
        LIMIT 1
        """
    )
    fun observeLatestForUser(userId: String): Flow<NotificationInboxEntity?>

    @Query(
        """
        SELECT * FROM notification_inbox
        WHERE userId = :userId
        ORDER BY isUnread DESC, createdAtMs DESC
        """
    )
    fun observeAllForUser(userId: String): Flow<List<NotificationInboxEntity>>

    @Query(
        """
        SELECT * FROM notification_inbox
        WHERE userId = :userId AND notificationId = :notificationId
        LIMIT 1
        """
    )
    suspend fun getById(userId: String, notificationId: String): NotificationInboxEntity?

    @Query(
        """
        SELECT notificationId FROM notification_inbox
        WHERE userId = :userId AND source = :source AND isUnread = 1
        """
    )
    suspend fun getUnreadIdsBySource(userId: String, source: String): List<String>

    @Query(
        """
        SELECT COUNT(*) FROM notification_inbox
        WHERE userId = :userId AND isUnread = 1
        """
    )
    fun observeUnreadCountForUser(userId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<NotificationInboxEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NotificationInboxEntity)

    @Query(
        """
        DELETE FROM notification_inbox
        WHERE userId = :userId AND source = :source
        """
    )
    suspend fun deleteByUserIdAndSource(userId: String, source: String)

    @Query(
        """
        DELETE FROM notification_inbox
        WHERE userId = :userId AND notificationId = :notificationId
        """
    )
    suspend fun deleteById(userId: String, notificationId: String)

    @Query(
        """
        DELETE FROM notification_inbox
        WHERE userId = :userId AND source = :source AND type = :type
        """
    )
    suspend fun deleteBySourceAndType(userId: String, source: String, type: String)

    @Query(
        """
        UPDATE notification_inbox
        SET isUnread = 0, updatedAtMs = :updatedAtMs
        WHERE userId = :userId AND isUnread = 1
        """
    )
    suspend fun markAllReadForUser(userId: String, updatedAtMs: Long)

    @Query(
        """
        UPDATE notification_inbox
        SET isUnread = 0, updatedAtMs = :updatedAtMs
        WHERE userId = :userId AND notificationId = :notificationId
        """
    )
    suspend fun markReadForUser(userId: String, notificationId: String, updatedAtMs: Long)

    @Transaction
    suspend fun replaceRemoteForUser(
        userId: String,
        entities: List<NotificationInboxEntity>
    ) {
        deleteByUserIdAndSource(userId, REMOTE_SOURCE)
        if (entities.isNotEmpty()) {
            upsertAll(entities)
        }
    }

    companion object {
        const val REMOTE_SOURCE = "remote"
    }
}
