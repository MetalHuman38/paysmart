package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.SendMoneyRecentRecipientEntity

@Dao
interface SendMoneyRecentRecipientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SendMoneyRecentRecipientEntity)

    @Query(
        """
        SELECT * FROM send_money_recent_recipient
        WHERE userId = :userId
        ORDER BY updatedAtMs DESC
        LIMIT :limit
        """
    )
    fun observeRecentByUserId(
        userId: String,
        limit: Int
    ): Flow<List<SendMoneyRecentRecipientEntity>>

    @Query(
        """
        SELECT * FROM send_money_recent_recipient
        WHERE userId = :userId AND recipientKey = :recipientKey
        LIMIT 1
        """
    )
    suspend fun getByUserIdAndKey(
        userId: String,
        recipientKey: String
    ): SendMoneyRecentRecipientEntity?
}
