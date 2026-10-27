package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_logs")
data class SavingsLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val dateTimestamp: Long,
    val type: String, // CONTRIBUTION or WITHDRAWAL
    val notes: String? = null
)
