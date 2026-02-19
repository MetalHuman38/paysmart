package net.metalbrain.paysmart.room.doa

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.WalletBalanceEntity

@Dao
interface WalletBalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: WalletBalanceEntity)

    @Query("SELECT * FROM wallet_balances WHERE userId = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): WalletBalanceEntity?

    @Query("SELECT * FROM wallet_balances WHERE userId = :userId LIMIT 1")
    fun observeByUserId(userId: String): Flow<WalletBalanceEntity?>
}
