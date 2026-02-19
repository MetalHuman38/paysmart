package net.metalbrain.paysmart.room

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DbMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS auth_session_logs (
                    sid TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    sessionVersion INTEGER NOT NULL,
                    signInAtSeconds INTEGER NOT NULL,
                    recordedAt INTEGER NOT NULL,
                    PRIMARY KEY(sid)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_2_3: Migration = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS wallet_balances (
                    userId TEXT NOT NULL,
                    jsonData TEXT NOT NULL,
                    salt TEXT NOT NULL,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(userId)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_3_4: Migration = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_profile_cache (
                    userId TEXT NOT NULL,
                    displayName TEXT NOT NULL,
                    photoURL TEXT,
                    email TEXT,
                    phoneNumber TEXT,
                    updatedAt INTEGER NOT NULL,
                    PRIMARY KEY(userId)
                )
                """.trimIndent()
            )
        }
    }
}
