package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.InvoiceWeeklyDraftEntity

@Dao
interface InvoiceWeeklyDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InvoiceWeeklyDraftEntity)

    @Query("SELECT * FROM invoice_weekly_draft WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): InvoiceWeeklyDraftEntity?

    @Query("SELECT * FROM invoice_weekly_draft WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<InvoiceWeeklyDraftEntity?>
}
