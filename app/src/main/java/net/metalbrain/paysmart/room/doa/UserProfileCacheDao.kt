package net.metalbrain.paysmart.room.doa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.UserProfileCacheEntity

@Dao
interface UserProfileCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProfileCacheEntity)

    @Query("SELECT * FROM user_profile_cache WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): UserProfileCacheEntity?

    @Query("SELECT * FROM user_profile_cache WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<UserProfileCacheEntity?>
}
