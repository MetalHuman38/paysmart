package net.metalbrain.paysmart.room.doa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.FxQuoteCacheEntity

@Dao
interface FxQuoteCacheDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FxQuoteCacheEntity)

    @Query("SELECT * FROM fx_quote_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getByCacheKey(cacheKey: String): FxQuoteCacheEntity?

    @Query("SELECT * FROM fx_quote_cache WHERE cacheKey = :cacheKey LIMIT 1")
    fun observeByCacheKey(cacheKey: String): Flow<FxQuoteCacheEntity?>
}
