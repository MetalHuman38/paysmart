package net.metalbrain.paysmart.room

import androidx.room.Database
import androidx.room.RoomDatabase
import net.metalbrain.paysmart.room.doa.AuthSessionLogDao
import net.metalbrain.paysmart.room.doa.SecureTokenDao
import net.metalbrain.paysmart.room.doa.SecuritySettingsDao
import net.metalbrain.paysmart.room.doa.UserProfileCacheDao
import net.metalbrain.paysmart.room.doa.WalletBalanceDao
import net.metalbrain.paysmart.room.entity.AuthSessionLogEntity
import net.metalbrain.paysmart.room.entity.SecureTokenEntity
import net.metalbrain.paysmart.room.entity.SecuritySettingsEntity
import net.metalbrain.paysmart.room.entity.UserProfileCacheEntity
import net.metalbrain.paysmart.room.entity.WalletBalanceEntity

@Database(
    entities = [
        SecureTokenEntity::class,
        SecuritySettingsEntity::class,
        AuthSessionLogEntity::class,
        WalletBalanceEntity::class,
        UserProfileCacheEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class EncryptedAppDatabase : RoomDatabase() {
    abstract fun secureTokenDao(): SecureTokenDao

    abstract fun securitySettingsDao(): SecuritySettingsDao

    abstract fun authSessionLogDao(): AuthSessionLogDao

    abstract fun walletBalanceDao(): WalletBalanceDao

    abstract fun userProfileCacheDao(): UserProfileCacheDao

}
