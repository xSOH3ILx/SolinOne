package com.solinone.core.database.dao

import androidx.room.*
import com.solinone.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE persianYear = :year AND persianMonth = :month ORDER BY persianDay ASC")
    fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)
}
