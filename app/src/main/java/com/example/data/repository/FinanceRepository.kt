package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.DatabaseInitializer
import com.example.data.local.entities.*
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val database: AppDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = database.budgetDao().getAllBudgets()
    val allBills: Flow<List<BillEntity>> = database.billDao().getAllBills()
    val allGoals: Flow<List<SavingsGoalEntity>> = database.savingsDao().getAllGoals()

    fun getLogsForGoal(goalId: Long): Flow<List<SavingsLogEntity>> = database.savingsDao().getLogsForGoal(goalId)

    // Transaction Operations
    suspend fun addTransaction(transaction: TransactionEntity): Long {
        return database.transactionDao().insertTransaction(transaction)
    }

    suspend fun updateTransaction(transaction: TransactionEntity) {
        database.transactionDao().updateTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) {
        database.transactionDao().deleteTransaction(transaction)
    }

    // Budget Operations
    suspend fun saveBudget(budget: BudgetEntity) {
        database.budgetDao().insertBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) {
        database.budgetDao().deleteBudget(budget)
    }

    // Bill Operations
    suspend fun addBill(bill: BillEntity): Long {
        return database.billDao().insertBill(bill)
    }

    suspend fun updateBill(bill: BillEntity) {
        database.billDao().updateBill(bill)
    }

    suspend fun deleteBill(bill: BillEntity) {
        database.billDao().deleteBill(bill)
    }

    suspend fun markBillAsPaid(bill: BillEntity, addAsExpense: Boolean = true) {
        val updatedBill = bill.copy(
            status = "PAID",
            paidDateTimestamp = System.currentTimeMillis()
        )
        database.billDao().updateBill(updatedBill)

        if (addAsExpense) {
            val billExpense = TransactionEntity(
                title = "Bill Paid: ${bill.name}",
                amount = bill.amount,
                type = "EXPENSE",
                category = bill.category,
                dateTimestamp = System.currentTimeMillis(),
                paymentMethod = "UPI",
                notes = "Auto-added from Bill Reminder payment",
                isDemoData = false
            )
            database.transactionDao().insertTransaction(billExpense)
        }
    }

    // Savings Goal Operations
    suspend fun saveGoal(goal: SavingsGoalEntity): Long {
        return database.savingsDao().insertGoal(goal)
    }

    suspend fun deleteGoal(goal: SavingsGoalEntity) {
        database.savingsDao().deleteGoal(goal)
    }

    suspend fun addSavingsContribution(goal: SavingsGoalEntity, amount: Double, notes: String?, isWithdrawal: Boolean = false) {
        val delta = if (isWithdrawal) -amount else amount
        val newAmount = (goal.currentAmount + delta).coerceAtLeast(0.0)
        val isCompleted = newAmount >= goal.targetAmount && goal.targetAmount > 0

        val updatedGoal = goal.copy(
            currentAmount = newAmount,
            isCompleted = isCompleted
        )
        database.savingsDao().updateGoal(updatedGoal)

        val log = SavingsLogEntity(
            goalId = goal.id,
            amount = amount,
            dateTimestamp = System.currentTimeMillis(),
            type = if (isWithdrawal) "WITHDRAWAL" else "CONTRIBUTION",
            notes = notes
        )
        database.savingsDao().insertLog(log)
    }

    // Reset & Demo Data
    suspend fun populateDemoData() {
        DatabaseInitializer.populateDemoData(database)
    }

    suspend fun clearAllData() {
        database.transactionDao().deleteAllTransactions()
        database.budgetDao().deleteAllBudgets()
        database.billDao().deleteAllBills()
        database.savingsDao().deleteAllGoals()
        database.savingsDao().deleteAllLogs()
    }
}
