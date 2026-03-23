package net.metalbrain.paysmart.room

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DbMigrationsTest {

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        EncryptedAppDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    fun migrate13To15KeepsAccountLimitsTableAvailable() {
        val databaseName = "account-limits-migration-test"

        helper.createDatabase(databaseName, 13).close()

        val migratedDb = helper.runMigrationsAndValidate(
            databaseName,
            15,
            true,
            DbMigrations.MIGRATION_13_14,
            DbMigrations.MIGRATION_14_15
        )

        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'account_limits_properties'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
        }
    }

    @Test
    fun migrate15To16KeepsNotificationInboxTableAvailable() {
        val databaseName = "notification-inbox-migration-test"

        helper.createDatabase(databaseName, 15).close()

        val migratedDb = helper.runMigrationsAndValidate(
            databaseName,
            16,
            true,
            DbMigrations.MIGRATION_15_16
        )

        migratedDb.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = 'notification_inbox'"
        ).use { cursor ->
            assertTrue(cursor.moveToFirst())
        }
    }

    @Test
    fun migrate16To17AddsLaunchInterestToUserProfileCache() {
        val databaseName = "launch-interest-migration-test"

        helper.createDatabase(databaseName, 16).close()

        val migratedDb = helper.runMigrationsAndValidate(
            databaseName,
            17,
            true,
            DbMigrations.MIGRATION_16_17
        )

        migratedDb.query("PRAGMA table_info(user_profile_cache)").use { cursor ->
            var found = false
            val nameIndex = cursor.getColumnIndex("name")
            while (cursor.moveToNext()) {
                if (nameIndex >= 0 && cursor.getString(nameIndex) == "launchInterest") {
                    found = true
                    break
                }
            }
            assertTrue(found)
        }
    }
}
