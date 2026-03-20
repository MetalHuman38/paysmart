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

    val MIGRATION_9_10: Migration = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS invoice_profile_draft (
                    userId TEXT NOT NULL,
                    fullName TEXT NOT NULL,
                    address TEXT NOT NULL,
                    badgeNumber TEXT NOT NULL,
                    badgeExpiryDate TEXT NOT NULL,
                    utrNumber TEXT NOT NULL,
                    email TEXT NOT NULL,
                    contactPhone TEXT NOT NULL,
                    paymentMethod TEXT NOT NULL,
                    accountNumber TEXT NOT NULL,
                    sortCode TEXT NOT NULL,
                    paymentInstructions TEXT NOT NULL,
                    defaultHourlyRateInput TEXT NOT NULL,
                    declaration TEXT NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(userId)
                )
                """.trimIndent()
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS invoice_venues (
                    venueId TEXT NOT NULL,
                    userId TEXT NOT NULL,
                    venueName TEXT NOT NULL,
                    venueAddress TEXT NOT NULL,
                    defaultHourlyRateInput TEXT NOT NULL,
                    createdAtMs INTEGER NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(venueId)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_invoice_venues_userId ON invoice_venues(userId)"
            )

            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS invoice_weekly_draft (
                    userId TEXT NOT NULL,
                    selectedVenueId TEXT NOT NULL,
                    invoiceDate TEXT NOT NULL,
                    weekEndingDate TEXT NOT NULL,
                    shiftsJson TEXT NOT NULL,
                    hourlyRateInput TEXT NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(userId)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_10_11: Migration = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS transactions (
                    userId TEXT NOT NULL,
                    id TEXT NOT NULL,
                    title TEXT NOT NULL,
                    amount REAL NOT NULL,
                    currency TEXT NOT NULL,
                    status TEXT NOT NULL,
                    iconRes INTEGER NOT NULL,
                    createdAtMs INTEGER NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    provider TEXT,
                    paymentRail TEXT,
                    reference TEXT NOT NULL,
                    externalReference TEXT,
                    statusTimelineJson TEXT NOT NULL,
                    PRIMARY KEY(userId, id)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_transactions_userId ON transactions(userId)"
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_transactions_userId_createdAtMs
                ON transactions(userId, createdAtMs)
                """.trimIndent()
            )
        }
    }

    val MIGRATION_11_12: Migration = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                ALTER TABLE country_capability_catalog
                ADD COLUMN addMoneyProvidersJson TEXT NOT NULL DEFAULT '[]'
                """.trimIndent()
            )
        }
    }

    val MIGRATION_12_13: Migration = object : Migration(12, 13) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS funding_accounts (
                    userId TEXT NOT NULL,
                    accountId TEXT NOT NULL,
                    provider TEXT NOT NULL,
                    currency TEXT NOT NULL,
                    accountNumber TEXT NOT NULL,
                    bankName TEXT NOT NULL,
                    accountName TEXT NOT NULL,
                    reference TEXT NOT NULL,
                    status TEXT NOT NULL,
                    providerStatus TEXT NOT NULL,
                    customerId TEXT NOT NULL,
                    note TEXT,
                    createdAtMs INTEGER NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(userId)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_13_14: Migration = object : Migration(13, 14) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS managed_cards (
                    userId TEXT NOT NULL,
                    id TEXT NOT NULL,
                    provider TEXT NOT NULL,
                    brand TEXT NOT NULL,
                    last4 TEXT NOT NULL,
                    expMonth INTEGER NOT NULL,
                    expYear INTEGER NOT NULL,
                    funding TEXT,
                    country TEXT,
                    fingerprint TEXT,
                    isDefault INTEGER NOT NULL,
                    status TEXT NOT NULL,
                    createdAtMs INTEGER NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(userId, id)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE INDEX IF NOT EXISTS index_managed_cards_userId
                ON managed_cards(userId)
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS account_limits_properties (
                    iso2 TEXT NOT NULL,
                    tabsJson TEXT NOT NULL,
                    sectionsJson TEXT NOT NULL,
                    catalogVersion TEXT NOT NULL,
                    updatedAtMs INTEGER NOT NULL,
                    PRIMARY KEY(iso2)
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_14_15: Migration = object : Migration(14, 15) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Version 15 keeps the same schema as version 14.
        }
    }
}
