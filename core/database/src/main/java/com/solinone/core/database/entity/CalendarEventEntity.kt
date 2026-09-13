package com.solinone.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val persianYear: Int,
    val persianMonth: Int,
    val persianDay: Int,
    val isHoliday: Boolean = false
)
