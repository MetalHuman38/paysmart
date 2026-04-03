package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "auth_session_logs")
data class AuthSessionLogEntity(
    @PrimaryKey val sid: String,
    val userId: String,
    val sessionVersion: Int,
    val signInAtSeconds: Long,
    val recordedAt: Long = System.currentTimeMillis()
)
