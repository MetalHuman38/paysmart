package net.metalbrain.paysmart.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.FundingAccountEntity

@Dao
interface FundingAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: FundingAccountEntity)

    @Query("SELECT * FROM funding_accounts WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): FundingAccountEntity?

    @Query("SELECT * FROM funding_accounts WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<FundingAccountEntity?>

    @Query("DELETE FROM funding_accounts WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)
}
