package net.metalbrain.paysmart.room

import androidx.room.Database
import androidx.room.RoomDatabase
import net.metalbrain.paysmart.room.doa.AuthSessionLogDao
import net.metalbrain.paysmart.room.doa.CountryCapabilityDao
import net.metalbrain.paysmart.room.doa.FxQuoteCacheDao
import net.metalbrain.paysmart.room.doa.SecureTokenDao
import net.metalbrain.paysmart.room.doa.SendMoneyRecipientDraftDao
import net.metalbrain.paysmart.room.doa.SecuritySettingsDao
import net.metalbrain.paysmart.room.doa.UserProfileCacheDao
import net.metalbrain.paysmart.room.doa.WalletBalanceDao
import net.metalbrain.paysmart.room.entity.AuthSessionLogEntity
import net.metalbrain.paysmart.room.entity.CountryCapabilityEntity
import net.metalbrain.paysmart.room.entity.FxQuoteCacheEntity
import net.metalbrain.paysmart.room.entity.SecureTokenEntity
import net.metalbrain.paysmart.room.entity.SendMoneyRecipientDraftEntity
import net.metalbrain.paysmart.room.entity.SecuritySettingsEntity
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
        SendMoneyRecipientDraftEntity::class
    ],
    version = 9,
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

    abstract fun sendMoneyRecipientDraftDao(): SendMoneyRecipientDraftDao

}
