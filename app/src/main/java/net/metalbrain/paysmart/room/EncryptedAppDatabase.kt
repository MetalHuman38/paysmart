package net.metalbrain.paysmart.room

import androidx.room.Database
import androidx.room.RoomDatabase
import net.metalbrain.paysmart.room.dao.AuthSessionLogDao
import net.metalbrain.paysmart.room.dao.CountryAccountLimitDao
import net.metalbrain.paysmart.room.dao.CountryCapabilityDao
import net.metalbrain.paysmart.room.dao.FundingAccountDao
import net.metalbrain.paysmart.room.dao.FxQuoteCacheDao
import net.metalbrain.paysmart.room.dao.InvoiceProfileDraftDao
import net.metalbrain.paysmart.room.dao.InvoiceVenueDao
import net.metalbrain.paysmart.room.dao.InvoiceWeeklyDraftDao
import net.metalbrain.paysmart.room.dao.ManagedCardDao
import net.metalbrain.paysmart.room.dao.SecureTokenDao
import net.metalbrain.paysmart.room.dao.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.dao.SecuritySettingsDao
import net.metalbrain.paysmart.room.dao.TransactionDao
import net.metalbrain.paysmart.room.dao.UserProfileCacheDao
import net.metalbrain.paysmart.room.dao.WalletBalanceDao
import net.metalbrain.paysmart.room.entity.AuthSessionLogEntity
import net.metalbrain.paysmart.room.entity.CountryAccountLimitEntity
import net.metalbrain.paysmart.room.entity.CountryCapabilityEntity
import net.metalbrain.paysmart.room.entity.FundingAccountEntity
import net.metalbrain.paysmart.room.entity.FxQuoteCacheEntity
import net.metalbrain.paysmart.room.entity.InvoiceProfileDraftEntity
import net.metalbrain.paysmart.room.entity.InvoiceVenueEntity
import net.metalbrain.paysmart.room.entity.InvoiceWeeklyDraftEntity
import net.metalbrain.paysmart.room.entity.ManagedCardEntity
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
        CountryAccountLimitEntity::class,
        FundingAccountEntity::class,
        ManagedCardEntity::class,
        SendMoneyRecipientDraftEntity::class,
        InvoiceProfileDraftEntity::class,
        InvoiceVenueEntity::class,
        InvoiceWeeklyDraftEntity::class,
        TransactionEntity::class
    ],
    version = 15,
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

    abstract fun managedCardDao(): ManagedCardDao

    abstract fun sendMoneyRecipientDraftDao(): SendMoneyRecipientDraftDao

    abstract fun invoiceProfileDraftDao(): InvoiceProfileDraftDao

    abstract fun invoiceVenueDao(): InvoiceVenueDao

    abstract fun invoiceWeeklyDraftDao(): InvoiceWeeklyDraftDao
    
    abstract fun transactionDao(): TransactionDao

    abstract fun countAccountLimitDao(): CountryAccountLimitDao
}
