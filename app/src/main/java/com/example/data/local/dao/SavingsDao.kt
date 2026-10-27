package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.SavingsGoalEntity
import com.example.data.local.entities.SavingsLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings_goals ORDER BY isCompleted ASC, id DESC")
    fun getAllGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoalEntity)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoalEntity)

    @Query("DELETE FROM savings_goals")
    suspend fun deleteAllGoals()

    @Query("SELECT * FROM savings_logs WHERE goalId = :goalId ORDER BY dateTimestamp DESC")
    fun getLogsForGoal(goalId: Long): Flow<List<SavingsLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: SavingsLogEntity)

    @Query("DELETE FROM savings_logs")
    suspend fun deleteAllLogs()
}
