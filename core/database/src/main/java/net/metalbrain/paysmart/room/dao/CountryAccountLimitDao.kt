package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.CountryAccountLimitEntity

@Dao
interface CountryAccountLimitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CountryAccountLimitEntity>)

    @Query("SELECT * FROM account_limits_properties WHERE iso2 = :iso2 LIMIT 1")
    fun observeByIso2(iso2: String): Flow<CountryAccountLimitEntity?>

    @Query("SELECT catalogVersion FROM account_limits_properties LIMIT 1")
    suspend fun getCatalogVersion(): String?

    @Query("SELECT COUNT(*) FROM account_limits_properties")
    suspend fun count(): Int

    @Query("DELETE FROM account_limits_properties")
    suspend fun clearAll()
}
