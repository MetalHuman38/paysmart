package net.metalbrain.paysmart.room.doa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.InvoiceProfileDraftEntity

@Dao
interface InvoiceProfileDraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InvoiceProfileDraftEntity)

    @Query("SELECT * FROM invoice_profile_draft WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): InvoiceProfileDraftEntity?

    @Query("SELECT * FROM invoice_profile_draft WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<InvoiceProfileDraftEntity?>
}

