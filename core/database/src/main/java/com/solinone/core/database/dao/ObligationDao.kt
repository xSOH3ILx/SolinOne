package com.solinone.core.database.dao

import androidx.room.*
import com.solinone.core.database.entity.ObligationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ObligationDao {
    @Query("SELECT * FROM obligations WHERE isPaid = 0 ORDER BY dueDatePersian ASC")
    fun getPendingObligations(): Flow<List<ObligationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObligation(obligation: ObligationEntity): Long

    @Update
    suspend fun updateObligation(obligation: ObligationEntity)

    @Delete
    suspend fun deleteObligation(obligation: ObligationEntity)
}
