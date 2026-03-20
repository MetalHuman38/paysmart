package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.InvoiceVenueEntity

@Dao
interface InvoiceVenueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: InvoiceVenueEntity)

    @Query("SELECT * FROM invoice_venues WHERE userId = :userId ORDER BY updatedAtMs DESC")
    fun observeByUserId(userId: String): Flow<List<InvoiceVenueEntity>>

    @Query("SELECT * FROM invoice_venues WHERE userId = :userId ORDER BY updatedAtMs DESC")
    suspend fun listByUserId(userId: String): List<InvoiceVenueEntity>

    @Query("DELETE FROM invoice_venues WHERE userId = :userId AND venueId = :venueId")
    suspend fun deleteByUserIdAndVenueId(userId: String, venueId: String)
}
