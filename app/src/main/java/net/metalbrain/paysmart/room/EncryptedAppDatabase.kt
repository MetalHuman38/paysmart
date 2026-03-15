package net.metalbrain.paysmart.room

import androidx.room.Database
import androidx.room.RoomDatabase
import net.metalbrain.paysmart.room.doa.AuthSessionLogDao
import net.metalbrain.paysmart.room.doa.CountryCapabilityDao
import net.metalbrain.paysmart.room.doa.FundingAccountDao
import net.metalbrain.paysmart.room.doa.FxQuoteCacheDao
import net.metalbrain.paysmart.room.doa.InvoiceProfileDraftDao
import net.metalbrain.paysmart.room.doa.InvoiceVenueDao
import net.metalbrain.paysmart.room.doa.InvoiceWeeklyDraftDao
import net.metalbrain.paysmart.room.doa.SecureTokenDao
import net.metalbrain.paysmart.room.doa.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.doa.SecuritySettingsDao
import net.metalbrain.paysmart.room.doa.TransactionDao
import net.metalbrain.paysmart.room.doa.UserProfileCacheDao
import net.metalbrain.paysmart.room.doa.WalletBalanceDao
import net.metalbrain.paysmart.room.entity.AuthSessionLogEntity
import net.metalbrain.paysmart.room.entity.CountryCapabilityEntity
import net.metalbrain.paysmart.room.entity.FundingAccountEntity
import net.metalbrain.paysmart.room.entity.FxQuoteCacheEntity
import net.metalbrain.paysmart.room.entity.InvoiceProfileDraftEntity
import net.metalbrain.paysmart.room.entity.InvoiceVenueEntity
import net.metalbrain.paysmart.room.entity.InvoiceWeeklyDraftEntity
import net.metalbrain.paysmart.room.entity.SecureTokenEntity
import net.metalbrain.paysmart.room.entity.SendMoneyRecipientDraftEntity
import net.metalbrain.paysmart.room.entity.SecuritySettingsEntity
import net.metalbrain.paysmart.room.entity.TransactionEntity
import net.metalbrain.paysmart.room.entity.UserProfileCacheEntity
import net.metalbrain.paysmart.room.entity.WalletBalanceEntity

@Database(
    entities = [
        SecureTokenEntity::class,
        SecuritySettingsEntity::class,
        AuthSessionLogEntity::class,
        WalletBalanceEntity::class,
        UserProfileCacheEntity::class,
        FxQuoteCacheEntity::class,
        CountryCapabilityEntity::class,
        FundingAccountEntity::class,
        SendMoneyRecipientDraftEntity::class,
        InvoiceProfileDraftEntity::class,
        InvoiceVenueEntity::class,
        InvoiceWeeklyDraftEntity::class,
        TransactionEntity::class
    ],
    version = 13,
    exportSchema = true
)
abstract class EncryptedAppDatabase : RoomDatabase() {
    abstract fun secureTokenDao(): SecureTokenDao

    abstract fun securitySettingsDao(): SecuritySettingsDao

    abstract fun authSessionLogDao(): AuthSessionLogDao

    abstract fun walletBalanceDao(): WalletBalanceDao

    abstract fun userProfileCacheDao(): UserProfileCacheDao

    abstract fun fxQuoteCacheDao(): FxQuoteCacheDao

    abstract fun countryCapabilityDao(): CountryCapabilityDao

    abstract fun fundingAccountDao(): FundingAccountDao

    abstract fun sendMoneyRecipientDraftDao(): SendMoneyRecipientDraftDao

    abstract fun invoiceProfileDraftDao(): InvoiceProfileDraftDao

    abstract fun invoiceVenueDao(): InvoiceVenueDao

    abstract fun invoiceWeeklyDraftDao(): InvoiceWeeklyDraftDao
    
    abstract fun transactionDao(): TransactionDao
}
