package com.solinone.core.database.dao

import androidx.room.*
import com.solinone.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Query("SELECT * FROM calendar_events WHERE (persianYear = :year OR persianYear = 0) AND persianMonth = :month ORDER BY persianDay ASC")
    fun getEventsForMonth(year: Int, month: Int): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_events WHERE (persianYear = :year OR persianYear = 0) AND persianMonth = :month AND persianDay = :day ORDER BY isHoliday DESC, id ASC")
    fun getEventsForDay(year: Int, month: Int, day: Int): Flow<List<CalendarEventEntity>>

    @Query("SELECT COUNT(*) FROM calendar_events")
    suspend fun getEventCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CalendarEventEntity): Long

    @Delete
    suspend fun deleteEvent(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)
}
