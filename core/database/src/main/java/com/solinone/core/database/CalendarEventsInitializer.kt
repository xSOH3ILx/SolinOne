package com.solinone.core.database

import android.content.Context
import com.solinone.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object CalendarEventsInitializer {

    suspend fun populateEventsIfNeeded(context: Context, database: SolinOneDatabase) {
        withContext(Dispatchers.IO) {
            try {
                val dao = database.calendarEventDao()
                if (dao.getEventCount() > 0) return@withContext

                val jsonStr = context.assets.open("iran_events.json").bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(jsonStr)
                val list = ArrayList<CalendarEventEntity>(jsonArray.length())

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val title = obj.getString("title")
                    val month = obj.getInt("month")
                    val day = obj.getInt("day")
                    val isHoliday = obj.optBoolean("isHoliday", false)
                    val type = obj.optString("type", "Iran")

                    list.add(
                        CalendarEventEntity(
                            title = title,
                            description = if (type == "AncientIran") "جشن باستانی ایران" else if (type == "International") "مناسبت بین‌المللی" else "مناسبت رسمی ایران",
                            persianYear = 0, // 0 means repeating every year
                            persianMonth = month,
                            persianDay = day,
                            isHoliday = isHoliday
                        )
                    )
                }

                dao.insertAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
