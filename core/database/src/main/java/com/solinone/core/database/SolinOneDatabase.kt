package com.solinone.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.solinone.core.database.dao.CalendarEventDao
import com.solinone.core.database.dao.ObligationDao
import com.solinone.core.database.dao.TransactionDao
import com.solinone.core.database.entity.CalendarEventEntity
import com.solinone.core.database.entity.ObligationEntity
import com.solinone.core.database.entity.TransactionEntity

@Database(
    entities = [
        TransactionEntity::class,
        ObligationEntity::class,
        CalendarEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SolinOneDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun obligationDao(): ObligationDao
    abstract fun calendarEventDao(): CalendarEventDao

    companion object {
        @Volatile
        private var INSTANCE: SolinOneDatabase? = null

        fun getDatabase(context: Context): SolinOneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SolinOneDatabase::class.java,
                    "solinone_database.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
