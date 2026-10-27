package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val category: String, // Category name or "TOTAL"
    val limitAmount: Double,
    val monthYear: String // e.g. "2026-10" or "ALL"
)
