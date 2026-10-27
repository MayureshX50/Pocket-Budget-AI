package com.example.data.local

import com.example.data.local.entities.*
import com.example.model.BillRecurrence
import com.example.model.BillStatus
import com.example.model.PaymentMethod
import com.example.model.TransactionCategory
import com.example.model.TransactionType

object DatabaseInitializer {

    suspend fun populateDemoData(database: AppDatabase) {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L

        // Clear existing demo data before seeding
        database.transactionDao().deleteAllTransactions()
        database.budgetDao().deleteAllBudgets()
        database.billDao().deleteAllBills()
        database.savingsDao().deleteAllGoals()
        database.savingsDao().deleteAllLogs()

        // 1. Seed Realistic Indian Transactions
        val demoTransactions = listOf(
            TransactionEntity(
                title = "Monthly Salary (TechCorp)",
                amount = 65000.0,
                type = TransactionType.INCOME.name,
                category = TransactionCategory.SALARY.name,
                dateTimestamp = now - (2 * dayMs),
                paymentMethod = PaymentMethod.BANK_TRANSFER.label,
                notes = "October Salary credited to HDFC Account",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Freelance UI Design Project",
                amount = 14500.0,
                type = TransactionType.INCOME.name,
                category = TransactionCategory.FREELANCE.name,
                dateTimestamp = now - (10 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "Design work for client",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Monthly Rent",
                amount = 16000.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.RENT.name,
                dateTimestamp = now - (3 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "Paid to Landlord via GPay",
                isDemoData = true
            ),
            TransactionEntity(
                title = "DMart Monthly Groceries",
                amount = 4850.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.FOOD.name,
                dateTimestamp = now - (1 * dayMs),
                paymentMethod = PaymentMethod.DEBIT_CARD.label,
                notes = "Monthly essentials & dry fruits",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Swiggy Food Order",
                amount = 580.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.FOOD.name,
                dateTimestamp = now - (4 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "Biryani weekend dinner",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Fuel at HP Petrol Pump",
                amount = 2200.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.TRANSPORT.name,
                dateTimestamp = now - (5 * dayMs),
                paymentMethod = PaymentMethod.CREDIT_CARD.label,
                notes = "Full tank petrol",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Uber Office Commute",
                amount = 340.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.TRANSPORT.name,
                dateTimestamp = now - (6 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "Morning peak hour ride",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Airtel Fiber Broadband Bill",
                amount = 1179.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.BILLS.name,
                dateTimestamp = now - (7 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "200 Mbps monthly plan",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Electricity Bill (BESCOM)",
                amount = 1950.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.BILLS.name,
                dateTimestamp = now - (12 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "September power bill",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Amazon Festival Sale Shopping",
                amount = 3890.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.SHOPPING.name,
                dateTimestamp = now - (8 * dayMs),
                paymentMethod = PaymentMethod.CREDIT_CARD.label,
                notes = "Shoes and smartwatch strap",
                isDemoData = true
            ),
            TransactionEntity(
                title = "PVR Movie Tickets & Snacks",
                amount = 950.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.ENTERTAINMENT.name,
                dateTimestamp = now - (9 * dayMs),
                paymentMethod = PaymentMethod.UPI.label,
                notes = "Weekend movie with friends",
                isDemoData = true
            ),
            TransactionEntity(
                title = "Pharmacy Medicines",
                amount = 640.0,
                type = TransactionType.EXPENSE.name,
                category = TransactionCategory.HEALTHCARE.name,
                dateTimestamp = now - (11 * dayMs),
                paymentMethod = PaymentMethod.CASH.label,
                notes = "Vitamin supplements & eye drops",
                isDemoData = true
            )
        )

        demoTransactions.forEach { database.transactionDao().insertTransaction(it) }

        // 2. Seed Budgets
        val demoBudgets = listOf(
            BudgetEntity(category = "TOTAL", limitAmount = 45000.0, monthYear = "ALL"),
            BudgetEntity(category = TransactionCategory.FOOD.name, limitAmount = 10000.0, monthYear = "ALL"),
            BudgetEntity(category = TransactionCategory.RENT.name, limitAmount = 16000.0, monthYear = "ALL"),
            BudgetEntity(category = TransactionCategory.TRANSPORT.name, limitAmount = 4500.0, monthYear = "ALL"),
            BudgetEntity(category = TransactionCategory.SHOPPING.name, limitAmount = 5000.0, monthYear = "ALL"),
            BudgetEntity(category = TransactionCategory.BILLS.name, limitAmount = 5000.0, monthYear = "ALL"),
            BudgetEntity(category = TransactionCategory.ENTERTAINMENT.name, limitAmount = 3000.0, monthYear = "ALL")
        )
        demoBudgets.forEach { database.budgetDao().insertBudget(it) }

        // 3. Seed Bill Reminders
        val demoBills = listOf(
            BillEntity(
                name = "Electricity Bill (BESCOM)",
                amount = 2100.0,
                dueDateTimestamp = now + (3 * dayMs),
                category = TransactionCategory.BILLS.name,
                recurrence = BillRecurrence.MONTHLY.label,
                status = BillStatus.UPCOMING.name,
                notes = "Consumer No: 832910"
            ),
            BillEntity(
                name = "House Rent Payment",
                amount = 16000.0,
                dueDateTimestamp = now + (5 * dayMs),
                category = TransactionCategory.RENT.name,
                recurrence = BillRecurrence.MONTHLY.label,
                status = BillStatus.UPCOMING.name,
                notes = "Pay via UPI before 5th"
            ),
            BillEntity(
                name = "Mobile Postpaid (Jio)",
                amount = 707.0,
                dueDateTimestamp = now + (8 * dayMs),
                category = TransactionCategory.BILLS.name,
                recurrence = BillRecurrence.MONTHLY.label,
                status = BillStatus.UPCOMING.name
            ),
            BillEntity(
                name = "Netflix Premium Subscription",
                amount = 649.0,
                dueDateTimestamp = now - (1 * dayMs),
                category = TransactionCategory.ENTERTAINMENT.name,
                recurrence = BillRecurrence.MONTHLY.label,
                status = BillStatus.PAID.name,
                paidDateTimestamp = now - (1 * dayMs)
            )
        )
        demoBills.forEach { database.billDao().insertBill(it) }

        // 4. Seed Savings Goals
        val emergencyGoalId = database.savingsDao().insertGoal(
            SavingsGoalEntity(
                title = "Emergency Safety Fund",
                targetAmount = 100000.0,
                currentAmount = 45000.0,
                targetDateTimestamp = now + (120 * dayMs),
                notes = "6 months living expenses cushion"
            )
        )

        val laptopGoalId = database.savingsDao().insertGoal(
            SavingsGoalEntity(
                title = "MacBook Air M3",
                targetAmount = 95000.0,
                currentAmount = 38000.0,
                targetDateTimestamp = now + (60 * dayMs),
                notes = "Upgrade work computer"
            )
        )

        database.savingsDao().insertGoal(
            SavingsGoalEntity(
                title = "Goa New Year Vacation",
                targetAmount = 25000.0,
                currentAmount = 18500.0,
                targetDateTimestamp = now + (45 * dayMs),
                notes = "Flight and stay booking"
            )
        )

        // Logs
        database.savingsDao().insertLog(
            SavingsLogEntity(
                goalId = emergencyGoalId,
                amount = 10000.0,
                dateTimestamp = now - (15 * dayMs),
                type = "CONTRIBUTION",
                notes = "Monthly goal auto-save"
            )
        )

        database.savingsDao().insertLog(
            SavingsLogEntity(
                goalId = laptopGoalId,
                amount = 5000.0,
                dateTimestamp = now - (7 * dayMs),
                type = "CONTRIBUTION",
                notes = "From freelance payout"
            )
        )
    }
}
