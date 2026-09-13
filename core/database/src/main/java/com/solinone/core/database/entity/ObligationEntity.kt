package com.solinone.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "obligations")
data class ObligationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amountRial: Long,
    val dueDatePersian: String,
    val isPaid: Boolean = false,
    val reminderHour: Int = 9,
    val reminderMinute: Int = 0
)
