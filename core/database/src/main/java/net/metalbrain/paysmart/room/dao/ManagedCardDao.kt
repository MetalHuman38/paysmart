package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.ManagedCardEntity

@Dao
interface ManagedCardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<ManagedCardEntity>)

    @Query(
        """
        SELECT * FROM managed_cards
        WHERE userId = :userId
        ORDER BY isDefault DESC, updatedAtMs DESC
        """
    )
    fun observeByUserId(userId: String): Flow<List<ManagedCardEntity>>

    @Query("DELETE FROM managed_cards WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    @Transaction
    suspend fun replaceForUserId(userId: String, entities: List<ManagedCardEntity>) {
        deleteByUserId(userId)
        if (entities.isNotEmpty()) {
            upsert(entities)
        }
    }
}
