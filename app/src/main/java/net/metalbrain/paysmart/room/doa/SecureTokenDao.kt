package net.metalbrain.paysmart.room.doa

import androidx.room.*
import net.metalbrain.paysmart.room.entity.SecureTokenEntity

@Dao
interface SecureTokenDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(token: SecureTokenEntity)

    @Query("SELECT * FROM secure_tokens WHERE userId = :userId")
    suspend fun getTokensByUser(userId: String): List<SecureTokenEntity>

    @Query("DELETE FROM secure_tokens")
    suspend fun clearAll()
}
