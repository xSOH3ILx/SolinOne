package com.solinone.core.database.dao

import androidx.room.*
import com.solinone.core.database.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE isPendingReview = 0 ORDER BY timestamp DESC")
    fun getAllConfirmedTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE isPendingReview = 1 ORDER BY timestamp DESC")
    fun getPendingReviewTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(amountRial) FROM transactions WHERE type = 'INCOME' AND isPendingReview = 0")
    fun getTotalIncome(): Flow<Long?>

    @Query("SELECT SUM(amountRial) FROM transactions WHERE type = 'EXPENSE' AND isPendingReview = 0")
    fun getTotalExpense(): Flow<Long?>

    @Query("SELECT category, SUM(amountRial) as totalAmount, COUNT(*) as count FROM transactions WHERE type = :type AND isPendingReview = 0 GROUP BY category ORDER BY totalAmount DESC")
    fun getCategorySummaries(type: String = "EXPENSE"): Flow<List<CategorySummary>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("UPDATE transactions SET isPendingReview = 0 WHERE id = :id")
    suspend fun confirmTransaction(id: Long)
}
