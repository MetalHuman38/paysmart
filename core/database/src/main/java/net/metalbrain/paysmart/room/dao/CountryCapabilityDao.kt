package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.CountryCapabilityEntity

@Dao
interface CountryCapabilityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entities: List<CountryCapabilityEntity>)

    @Query("SELECT * FROM country_capability_catalog WHERE iso2 = :iso2 LIMIT 1")
    fun observeByIso2(iso2: String): Flow<CountryCapabilityEntity?>

    @Query("SELECT * FROM country_capability_catalog ORDER BY countryName ASC")
    fun observeAll(): Flow<List<CountryCapabilityEntity>>

    @Query("SELECT catalogVersion FROM country_capability_catalog LIMIT 1")
    suspend fun getCatalogVersion(): String?

    @Query("SELECT COUNT(*) FROM country_capability_catalog")
    suspend fun count(): Int

    @Query("DELETE FROM country_capability_catalog")
    suspend fun clearAll()
}
