package net.metalbrain.paysmart

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import net.metalbrain.paysmart.room.EncryptedAppDatabase
import net.metalbrain.paysmart.room.doa.AuthSessionLogDao
import net.metalbrain.paysmart.room.doa.CountryCapabilityDao
import net.metalbrain.paysmart.room.doa.FundingAccountDao
import net.metalbrain.paysmart.room.doa.FxQuoteCacheDao
import net.metalbrain.paysmart.room.doa.InvoiceProfileDraftDao
import net.metalbrain.paysmart.room.doa.InvoiceVenueDao
import net.metalbrain.paysmart.room.doa.InvoiceWeeklyDraftDao
import net.metalbrain.paysmart.room.doa.SecuritySettingsDao
import net.metalbrain.paysmart.room.doa.SecureTokenDao
import net.metalbrain.paysmart.room.doa.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.doa.TransactionDao
import net.metalbrain.paysmart.room.doa.UserProfileCacheDao
import net.metalbrain.paysmart.room.doa.WalletBalanceDao
import net.metalbrain.paysmart.room.di.RoomProvidesModule
import net.metalbrain.paysmart.utils.RoomKeyProvider
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RoomProvidesModule::class]
)
object TestRoomModule {

    // 🔐 Fake key provider for JNI tests
    @Provides
    @Singleton
    fun provideRoomKeyProvider(): RoomKeyProvider =
        object : RoomKeyProvider {
            override fun getKeyHex(): String =
                "00112233445566778899aabbccddeeff00112233445566778899aabbccddeeff"
        }

    // 🧪 In-memory DB
    @Provides
    @Singleton
    fun provideInMemoryDb(
        @ApplicationContext context: Context
    ): EncryptedAppDatabase =
        Room.inMemoryDatabaseBuilder(
            context,
            EncryptedAppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

    @Provides
    fun provideSecuritySettingsDao(
        db: EncryptedAppDatabase
    ): SecuritySettingsDao = db.securitySettingsDao()

    @Provides
    fun provideSecureTokenDao(
        db: EncryptedAppDatabase
    ): SecureTokenDao = db.secureTokenDao()

    @Provides
    fun provideAuthSessionLogDao(
        db: EncryptedAppDatabase
    ): AuthSessionLogDao = db.authSessionLogDao()

    @Provides
    fun provideWalletBalanceDao(
        db: EncryptedAppDatabase
    ): WalletBalanceDao = db.walletBalanceDao()

    @Provides
    fun provideUserProfileCacheDao(
        db: EncryptedAppDatabase
    ): UserProfileCacheDao = db.userProfileCacheDao()

    @Provides
    fun provideFxQuoteCacheDao(
        db: EncryptedAppDatabase
    ): FxQuoteCacheDao = db.fxQuoteCacheDao()

    @Provides
    fun provideCountryCapabilityDao(
        db: EncryptedAppDatabase
    ): CountryCapabilityDao = db.countryCapabilityDao()

    @Provides
    fun provideFundingAccountDao(
        db: EncryptedAppDatabase
    ): FundingAccountDao = db.fundingAccountDao()

    @Provides
    fun provideSendMoneyRecipientDraftDao(
        db: EncryptedAppDatabase
    ): SendMoneyRecipientDraftDao = db.sendMoneyRecipientDraftDao()

    @Provides
    fun provideInvoiceProfileDraftDao(
        db: EncryptedAppDatabase
    ): InvoiceProfileDraftDao = db.invoiceProfileDraftDao()

    @Provides
    fun provideInvoiceVenueDao(
        db: EncryptedAppDatabase
    ): InvoiceVenueDao = db.invoiceVenueDao()

    @Provides
    fun provideInvoiceWeeklyDraftDao(
        db: EncryptedAppDatabase
    ): InvoiceWeeklyDraftDao = db.invoiceWeeklyDraftDao()

    @Provides
    fun provideTransactionDao(
        db: EncryptedAppDatabase
    ): TransactionDao = db.transactionDao()
}
