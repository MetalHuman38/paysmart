package net.metalbrain.paysmart.room.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_settings")
data class SecuritySettingsEntity(
    @PrimaryKey val userId: String,
    val jsonData: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
