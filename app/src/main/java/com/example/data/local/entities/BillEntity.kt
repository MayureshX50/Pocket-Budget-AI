package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDateTimestamp: Long,
    val category: String,
    val recurrence: String, // Monthly, Weekly, Yearly, One-time
    val status: String,     // UPCOMING, PAID, OVERDUE
    val paidDateTimestamp: Long? = null,
    val notes: String? = null
)
