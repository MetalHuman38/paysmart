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

    val MIGRATION_4_5: Migration = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE user_profile_cache ADD COLUMN dateOfBirth TEXT")
            db.execSQL("ALTER TABLE user_profile_cache ADD COLUMN addressLine1 TEXT")
            db.execSQL("ALTER TABLE user_profile_cache ADD COLUMN addressLine2 TEXT")
            db.execSQL("ALTER TABLE user_profile_cache ADD COLUMN city TEXT")
            db.execSQL("ALTER TABLE user_profile_cache ADD COLUMN country TEXT")
            db.execSQL("ALTER TABLE user_profile_cache ADD COLUMN postalCode TEXT")
        }
    }

    val MIGRATION_5_6: Migration = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS fx_quote_cache (
                    cacheKey TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    sourceCurrency TEXT NOT NULL,
                    targetCurrency TEXT NOT NULL,
                    sourceAmount REAL NOT NULL,
                    method TEXT NOT NULL,
                    rate REAL NOT NULL,
                    recipientAmount REAL NOT NULL,
                    feesJson TEXT NOT NULL,
                    guaranteeSeconds INTEGER NOT NULL,
                    arrivalSeconds INTEGER NOT NULL,
                    rateSource TEXT NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(cacheKey)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_6_7: Migration = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS country_capability_catalog (
                    iso2 TEXT NOT NULL,
                    countryName TEXT NOT NULL,
                    flagEmoji TEXT NOT NULL,
                    currencyCode TEXT NOT NULL,
                    addMoneyMethodsJson TEXT NOT NULL,
                    capabilitiesJson TEXT NOT NULL,
                    catalogVersion TEXT NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(iso2)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_7_8: Migration = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS send_money_recipient_draft (
                    userId TEXT NOT NULL,
                    selectedMethod TEXT NOT NULL,
                    step TEXT NOT NULL,
                    sourceCurrency TEXT NOT NULL,
                    targetCurrency TEXT NOT NULL,
                    sourceAmountInput TEXT NOT NULL,
                    voltTag TEXT NOT NULL,
                    lookupEmail TEXT NOT NULL,
                    lookupMobile TEXT NOT NULL,
                    lookupNote TEXT NOT NULL,
                    bankFullName TEXT NOT NULL,
                    bankIban TEXT NOT NULL,
                    bankBic TEXT NOT NULL,
                    bankSwift TEXT NOT NULL,
                    bankName TEXT NOT NULL,
                    bankAddress TEXT NOT NULL,
                    bankCity TEXT NOT NULL,
                    bankCountry TEXT NOT NULL,
                    bankPostalCode TEXT NOT NULL,
                    documentFileRef TEXT NOT NULL,
                    documentType TEXT NOT NULL,
                    documentNote TEXT NOT NULL,
                    requestEmail TEXT NOT NULL,
                    requestFullName TEXT NOT NULL,
                    requestNote TEXT NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(userId)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_8_9: Migration = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE send_money_recipient_draft ADD COLUMN quoteMethod TEXT NOT NULL DEFAULT 'wire'"
            )
            db.execSQL(
                "ALTER TABLE send_money_recipient_draft ADD COLUMN quotePayloadJson TEXT"
            )
            db.execSQL(
                "ALTER TABLE send_money_recipient_draft ADD COLUMN quoteDataSource TEXT"
            )
        }
    }
}
