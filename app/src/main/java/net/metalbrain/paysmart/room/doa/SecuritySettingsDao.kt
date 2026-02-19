package net.metalbrain.paysmart.room.doa

import androidx.room.*
import net.metalbrain.paysmart.room.entity.SecuritySettingsEntity

@Dao
interface SecuritySettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SecuritySettingsEntity)

    @Query("SELECT * FROM security_settings WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): SecuritySettingsEntity?
}
