package net.metalbrain.paysmart.room.doa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.metalbrain.paysmart.room.entity.AuthSessionLogEntity

@Dao
interface AuthSessionLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: AuthSessionLogEntity)

    @Query("SELECT * FROM auth_session_logs WHERE userId = :userId ORDER BY recordedAt DESC LIMIT 1")
    suspend fun latestByUserId(userId: String): AuthSessionLogEntity?
}
