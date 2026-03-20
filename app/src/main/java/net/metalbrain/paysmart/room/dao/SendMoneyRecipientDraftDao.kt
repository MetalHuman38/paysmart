package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.SendMoneyRecipientDraftEntity

@Dao
interface SendMoneyRecipientDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SendMoneyRecipientDraftEntity)

    @Query("SELECT * FROM send_money_recipient_draft WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): SendMoneyRecipientDraftEntity?

    @Query("SELECT * FROM send_money_recipient_draft WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<SendMoneyRecipientDraftEntity?>

    @Query("DELETE FROM send_money_recipient_draft WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}
