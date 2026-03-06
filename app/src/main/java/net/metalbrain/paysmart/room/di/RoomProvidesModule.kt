package net.metalbrain.paysmart.room.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.metalbrain.paysmart.room.DbMigrations
import net.metalbrain.paysmart.room.EncryptedAppDatabase
import net.metalbrain.paysmart.room.doa.AuthSessionLogDao
import net.metalbrain.paysmart.room.doa.CountryCapabilityDao
import net.metalbrain.paysmart.room.doa.FxQuoteCacheDao
import net.metalbrain.paysmart.room.doa.InvoiceProfileDraftDao
import net.metalbrain.paysmart.room.doa.InvoiceVenueDao
import net.metalbrain.paysmart.room.doa.InvoiceWeeklyDraftDao
import net.metalbrain.paysmart.room.doa.SecureTokenDao
import net.metalbrain.paysmart.room.doa.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.doa.SecuritySettingsDao
import net.metalbrain.paysmart.room.doa.UserProfileCacheDao
import net.metalbrain.paysmart.room.doa.WalletBalanceDao
import net.metalbrain.paysmart.utils.RoomKeyProvider
import net.metalbrain.paysmart.utils.SecureRoomKeyProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomProvidesModule {

    @Provides
    @Singleton
    fun provideRoomKeyProvider(): RoomKeyProvider = SecureRoomKeyProvider()

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): EncryptedAppDatabase {
        return Room.databaseBuilder(
                context,
                EncryptedAppDatabase::class.java,
                "encrypted_local.db"
            )
            .addMigrations(
                DbMigrations.MIGRATION_1_2,
                DbMigrations.MIGRATION_2_3,
                DbMigrations.MIGRATION_3_4,
                DbMigrations.MIGRATION_4_5,
                DbMigrations.MIGRATION_5_6,
                DbMigrations.MIGRATION_6_7,
                DbMigrations.MIGRATION_7_8,
                DbMigrations.MIGRATION_8_9,
                DbMigrations.MIGRATION_9_10
            )
            .fallbackToDestructiveMigration(false)
            .build()
    }

    @Provides
    fun provideSecureTokenDao(db: EncryptedAppDatabase): SecureTokenDao = db.secureTokenDao()

    @Provides
    fun provideSecuritySettingsDao(db: EncryptedAppDatabase): SecuritySettingsDao =
        db.securitySettingsDao()

    @Provides
    fun provideAuthSessionLogDao(db: EncryptedAppDatabase): AuthSessionLogDao =
        db.authSessionLogDao()

    @Provides
    fun provideWalletBalanceDao(db: EncryptedAppDatabase): WalletBalanceDao =
        db.walletBalanceDao()

    @Provides
    fun provideUserProfileCacheDao(db: EncryptedAppDatabase): UserProfileCacheDao =
        db.userProfileCacheDao()

    @Provides
    fun provideFxQuoteCacheDao(db: EncryptedAppDatabase): FxQuoteCacheDao =
        db.fxQuoteCacheDao()

    @Provides
    fun provideCountryCapabilityDao(db: EncryptedAppDatabase): CountryCapabilityDao =
        db.countryCapabilityDao()

    @Provides
    fun provideSendMoneyRecipientDraftDao(db: EncryptedAppDatabase): SendMoneyRecipientDraftDao =
        db.sendMoneyRecipientDraftDao()

    @Provides
    fun provideInvoiceProfileDraftDao(db: EncryptedAppDatabase): InvoiceProfileDraftDao =
        db.invoiceProfileDraftDao()

    @Provides
    fun provideInvoiceVenueDao(db: EncryptedAppDatabase): InvoiceVenueDao =
        db.invoiceVenueDao()

    @Provides
    fun provideInvoiceWeeklyDraftDao(db: EncryptedAppDatabase): InvoiceWeeklyDraftDao =
        db.invoiceWeeklyDraftDao()
}
