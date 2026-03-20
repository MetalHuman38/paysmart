package net.metalbrain.paysmart.room.query

import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import net.metalbrain.paysmart.domain.model.TransactionHistoryQuery
import java.util.Locale

object TransactionPagingQueryFactory {
    fun build(
        userId: String,
        query: TransactionHistoryQuery
    ): SupportSQLiteQuery {
        val clauses = mutableListOf("userId = ?")
        val args = mutableListOf<Any>(userId.trim())

        val statuses = query.statuses
            .mapNotNull { status -> status.trim().takeIf { it.isNotEmpty() } }
            .distinct()
            .sorted()
        if (statuses.isNotEmpty()) {
            clauses += statuses.asInClause("status")
            args.addAll(statuses)
        }

        val currencies = query.currencies
            .mapNotNull { currency ->
                currency.trim()
                    .uppercase(Locale.US)
                    .takeIf { it.isNotEmpty() }
            }
            .distinct()
            .sorted()
        if (currencies.isNotEmpty()) {
            clauses += currencies.asInClause("currency")
            args.addAll(currencies)
        }

        query.fromCreatedAtMs?.let { fromCreatedAtMs ->
            clauses += "createdAtMs >= ?"
            args += fromCreatedAtMs
        }

        query.toCreatedAtMs?.let { toCreatedAtMs ->
            clauses += "createdAtMs <= ?"
            args += toCreatedAtMs
        }

        val sql = buildString {
            append("SELECT * FROM transactions")
            append(" WHERE ")
            append(clauses.joinToString(separator = " AND "))
            append(" ORDER BY createdAtMs DESC, updatedAtMs DESC")
        }

        return SimpleSQLiteQuery(sql, args.toTypedArray())
    }
}

private fun List<String>.asInClause(columnName: String): String {
    return buildString {
        append(columnName)
        append(" IN (")
        append(joinToString(separator = ",") { "?" })
        append(")")
    }
}
