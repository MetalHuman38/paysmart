package net.metalbrain.paysmart.room.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.sqlite.db.SupportSQLiteQuery
import kotlinx.coroutines.flow.Flow
import net.metalbrain.paysmart.room.entity.TransactionEntity

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: TransactionEntity)

    @Query(
        """
        SELECT * FROM transactions
        WHERE userId = :userId
        ORDER BY createdAtMs DESC, updatedAtMs DESC
        """
    )
    fun observeByUserId(userId: String): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE userId = :userId
        ORDER BY createdAtMs DESC, updatedAtMs DESC
        """
    )
    suspend fun getByUserId(userId: String): List<TransactionEntity>

    @RawQuery(observedEntities = [TransactionEntity::class])
    fun pagingSource(query: SupportSQLiteQuery): PagingSource<Int, TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE userId = :userId AND id = :id
        LIMIT 1
        """
    )
    fun observeByUserIdAndId(userId: String, id: String): Flow<TransactionEntity?>

    @Query(
        """
        SELECT DISTINCT status FROM transactions
        WHERE userId = :userId
        ORDER BY status ASC
        """
    )
    fun observeAvailableStatuses(userId: String): Flow<List<String>>

    @Query("""
        SELECT DISTINCT currency FROM transactions
        WHERE userId = :userId
        ORDER BY currency ASC
    """)
    fun observeAvailableCurrencies(userId: String): Flow<List<String>>

    @Query(
        """
        SELECT * FROM transactions
        WHERE userId = :userId AND id = :id
        LIMIT 1
        """
    )
    suspend fun getByUserIdAndId(userId: String, id: String): TransactionEntity?
}
