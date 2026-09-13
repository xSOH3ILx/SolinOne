package com.solinone.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountRial: Long,
    val type: String, // "INCOME" or "EXPENSE"
    val category: String,
    val note: String = "",
    val timestamp: Long,
    val persianDate: String, // e.g., "1403/06/23"
    val isPendingReview: Boolean = false,
    val sender: String = ""
)
