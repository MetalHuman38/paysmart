package net.metalbrain.paysmart.room.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "notification_inbox",
    primaryKeys = ["userId", "notificationId"],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["userId", "createdAtMs"]),
        Index(value = ["userId", "isUnread"])
    ]
)
data class NotificationInboxEntity(
    val userId: String,
    val notificationId: String,
    val source: String,
    val type: String,
    val channel: String,
    val title: String,
    val body: String,
    val deepLink: String?,
    val isUnread: Boolean,
    val createdAtMs: Long,
    val updatedAtMs: Long
)
