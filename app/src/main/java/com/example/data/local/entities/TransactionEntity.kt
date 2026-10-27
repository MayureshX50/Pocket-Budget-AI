package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // INCOME or EXPENSE
    val category: String,
    val dateTimestamp: Long,
    val paymentMethod: String,
    val notes: String? = null,
    val isDemoData: Boolean = false
)
