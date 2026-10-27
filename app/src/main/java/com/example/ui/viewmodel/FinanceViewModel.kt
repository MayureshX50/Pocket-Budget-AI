package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.*
import com.example.data.repository.FinanceRepository
import com.example.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

data class FinanceUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val bills: List<BillEntity> = emptyList(),
    val goals: List<SavingsGoalEntity> = emptyList(),
    
    // Summary Calculations
    val totalIncomeThisMonth: Double = 0.0,
    val totalExpenseThisMonth: Double = 0.0,
    val monthlyBudgetLimit: Double = 45000.0,
    val remainingBudget: Double = 45000.0,
    val totalSavedAmount: Double = 0.0,
    val totalSavingsGoalTarget: Double = 0.0,
    
    // Analytics
    val categorySpends: List<CategorySpend> = emptyList(),
    val weeklySpends: List<WeeklySpend> = emptyList(),
    val overbudgetCategories: List<String> = emptyList(),
    val budget80PercentCategories: List<String> = emptyList(),
    val smartInsights: List<SmartInsight> = emptyList(),
    
    // Filters & UI State
    val searchQuery: String = "",
    val selectedTypeFilter: TransactionType? = null,
    val selectedCategoryFilter: TransactionCategory? = null,
    val isDemoDataActive: Boolean = true,
    val isLoading: Boolean = false,
    val userMessage: String? = null,
    
    // Preferences
    val isDarkTheme: Boolean = true,
    val isOnboardingComplete: Boolean = true
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository

    private val _searchQuery = MutableStateFlow("")
    private val _selectedTypeFilter = MutableStateFlow<TransactionType?>(null)
    private val _selectedCategoryFilter = MutableStateFlow<TransactionCategory?>(null)
    private val _userMessage = MutableStateFlow<String?>(null)
    private val _isDarkTheme = MutableStateFlow(true)
    private val _isOnboardingComplete = MutableStateFlow(true)

    val uiState: StateFlow<FinanceUiState>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = FinanceRepository(db)

        // Seed demo data on first launch if DB is empty
        viewModelScope.launch {
            repository.allTransactions.firstOrNull()?.let { list ->
                if (list.isEmpty()) {
                    repository.populateDemoData()
                }
            }
        }

        uiState = combine(
            repository.allTransactions,
            repository.allBudgets,
            repository.allBills,
            repository.allGoals,
            _searchQuery,
            _selectedTypeFilter,
            _selectedCategoryFilter,
            _isDarkTheme,
            _isOnboardingComplete
        ) { args ->
            val transactions = args[0] as List<TransactionEntity>
            val budgets = args[1] as List<BudgetEntity>
            val bills = args[2] as List<BillEntity>
            val goals = args[3] as List<SavingsGoalEntity>
            val query = args[4] as String
            val typeFilter = args[5] as TransactionType?
            val catFilter = args[6] as TransactionCategory?
            val darkTheme = args[7] as Boolean
            val onboardingDone = args[8] as Boolean

            // 1. Calculate Monthly Totals
            val (income, expense) = calculateMonthlyTotals(transactions)
            val totalBudgetObj = budgets.find { it.category == "TOTAL" }
            val budgetLimit = totalBudgetObj?.limitAmount ?: 45000.0
            val remaining = budgetLimit - expense

            // 2. Savings Totals
            val totalSaved = goals.sumOf { it.currentAmount }
            val totalTarget = goals.sumOf { it.targetAmount }

            // 3. Filter Transactions
            val filtered = transactions.filter { tx ->
                val matchesQuery = query.isEmpty() ||
                        tx.title.contains(query, ignoreCase = true) ||
                        (tx.notes?.contains(query, ignoreCase = true) == true) ||
                        tx.category.contains(query, ignoreCase = true)
                val matchesType = typeFilter == null || tx.type == typeFilter.name
                val matchesCat = catFilter == null || tx.category == catFilter.name
                matchesQuery && matchesType && matchesCat
            }

            // 4. Category Breakdown
            val catSpends = calculateCategorySpends(transactions, expense)

            // 5. Weekly Spending Breakdown
            val weeklySpends = calculateWeeklySpends(transactions)

            // 6. Overbudget Check
            val overbudget = mutableListOf<String>()
            val near80 = mutableListOf<String>()

            // Check overall
            if (expense >= budgetLimit) {
                overbudget.add("Total Monthly Budget")
            } else if (expense >= budgetLimit * 0.8) {
                near80.add("Total Monthly Budget")
            }

            // Check category budgets
            budgets.filter { it.category != "TOTAL" }.forEach { b ->
                val spentInCat = transactions
                    .filter { it.type == TransactionType.EXPENSE.name && it.category == b.category }
                    .sumOf { it.amount }
                if (spentInCat >= b.limitAmount) {
                    overbudget.add(b.category)
                } else if (spentInCat >= b.limitAmount * 0.8) {
                    near80.add(b.category)
                }
            }

            // 7. Generate Smart Money Insights
            val insights = generateInsights(transactions, expense, remaining, budgetLimit, catSpends, goals)

            val hasDemoData = transactions.any { it.isDemoData }

            FinanceUiState(
                transactions = transactions,
                filteredTransactions = filtered,
                budgets = budgets,
                bills = bills,
                goals = goals,
                totalIncomeThisMonth = income,
                totalExpenseThisMonth = expense,
                monthlyBudgetLimit = budgetLimit,
                remainingBudget = remaining,
                totalSavedAmount = totalSaved,
                totalSavingsGoalTarget = totalTarget,
                categorySpends = catSpends,
                weeklySpends = weeklySpends,
                overbudgetCategories = overbudget,
                budget80PercentCategories = near80,
                smartInsights = insights,
                searchQuery = query,
                selectedTypeFilter = typeFilter,
                selectedCategoryFilter = catFilter,
                isDemoDataActive = hasDemoData,
                isDarkTheme = darkTheme,
                isOnboardingComplete = onboardingDone
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FinanceUiState(isLoading = true)
        )
    }

    // Filter updates
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setTypeFilter(type: TransactionType?) {
        _selectedTypeFilter.value = type
    }

    fun setCategoryFilter(category: TransactionCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun clearFilters() {
        _searchQuery.value = ""
        _selectedTypeFilter.value = null
        _selectedCategoryFilter.value = null
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setOnboardingComplete(complete: Boolean) {
        _isOnboardingComplete.value = complete
    }

    // Transaction Actions
    fun addTransaction(title: String, amount: Double, type: TransactionType, category: TransactionCategory, paymentMethod: PaymentMethod, notes: String?) {
        viewModelScope.launch {
            repository.addTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    type = type.name,
                    category = category.name,
                    dateTimestamp = System.currentTimeMillis(),
                    paymentMethod = paymentMethod.label,
                    notes = notes,
                    isDemoData = false
                )
            )
            _userMessage.value = "Transaction added successfully"
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            _userMessage.value = "Transaction updated"
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            _userMessage.value = "Transaction deleted"
        }
    }

    // Budget Actions
    fun setBudgetLimit(categoryName: String, limitAmount: Double) {
        viewModelScope.launch {
            repository.saveBudget(
                BudgetEntity(
                    category = categoryName,
                    limitAmount = limitAmount,
                    monthYear = "ALL"
                )
            )
            _userMessage.value = "Budget limit updated to ${formatRupee(limitAmount)}"
        }
    }

    // Bill Actions
    fun addBill(name: String, amount: Double, dueDateTimestamp: Long, category: TransactionCategory, recurrence: BillRecurrence, notes: String?) {
        viewModelScope.launch {
            repository.addBill(
                BillEntity(
                    name = name,
                    amount = amount,
                    dueDateTimestamp = dueDateTimestamp,
                    category = category.name,
                    recurrence = recurrence.label,
                    status = BillStatus.UPCOMING.name,
                    notes = notes
                )
            )
            _userMessage.value = "Bill reminder added"
        }
    }

    fun markBillAsPaid(bill: BillEntity, addExpenseTransaction: Boolean = true) {
        viewModelScope.launch {
            repository.markBillAsPaid(bill, addExpenseTransaction)
            _userMessage.value = "Bill marked as paid"
        }
    }

    fun deleteBill(bill: BillEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
            _userMessage.value = "Bill deleted"
        }
    }

    // Savings Goal Actions
    fun addGoal(title: String, targetAmount: Double, currentSaved: Double, targetDateTimestamp: Long?, notes: String?) {
        viewModelScope.launch {
            repository.saveGoal(
                SavingsGoalEntity(
                    title = title,
                    targetAmount = targetAmount,
                    currentAmount = currentSaved,
                    targetDateTimestamp = targetDateTimestamp,
                    notes = notes
                )
            )
            _userMessage.value = "Savings goal created"
        }
    }

    fun addSavingsContribution(goal: SavingsGoalEntity, amount: Double, notes: String?, isWithdrawal: Boolean = false) {
        viewModelScope.launch {
            repository.addSavingsContribution(goal, amount, notes, isWithdrawal)
            val actionName = if (isWithdrawal) "Withdrawal recorded" else "Contribution added"
            _userMessage.value = "$actionName for ${goal.title}"
        }
    }

    fun deleteGoal(goal: SavingsGoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
            _userMessage.value = "Goal removed"
        }
    }

    // Data Management
    fun resetDemoData() {
        viewModelScope.launch {
            repository.populateDemoData()
            _userMessage.value = "Demo data reloaded"
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            repository.clearAllData()
            _userMessage.value = "All records cleared"
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Calculations Helpers
    private fun calculateMonthlyTotals(transactions: List<TransactionEntity>): Pair<Double, Double> {
        val cal = Calendar.getInstance()
        val currentMonth = cal.get(Calendar.MONTH)
        val currentYear = cal.get(Calendar.YEAR)

        var income = 0.0
        var expense = 0.0

        transactions.forEach { tx ->
            cal.timeInMillis = tx.dateTimestamp
            if (cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear) {
                if (tx.type == TransactionType.INCOME.name) {
                    income += tx.amount
                } else {
                    expense += tx.amount
                }
            } else if (transactions.size < 20) {
                // If overall dataset is small/recent, include in general summary
                if (tx.type == TransactionType.INCOME.name) {
                    income += tx.amount
                } else {
                    expense += tx.amount
                }
            }
        }
        return Pair(income, expense)
    }

    private fun calculateCategorySpends(transactions: List<TransactionEntity>, totalExpense: Double): List<CategorySpend> {
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE.name }
        val map = expenses.groupBy { it.category }

        return map.map { (catName, txList) ->
            val sum = txList.sumOf { it.amount }
            val catEnum = TransactionCategory.fromString(catName)
            val pct = if (totalExpense > 0) (sum / totalExpense).toFloat() else 0f
            CategorySpend(catEnum, sum, pct)
        }.sortedByDescending { it.totalAmount }
    }

    private fun calculateWeeklySpends(transactions: List<TransactionEntity>): List<WeeklySpend> {
        val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val daySpendMap = days.associateWith { 0.0 }.toMutableMap()

        val cal = Calendar.getInstance()
        transactions.filter { it.type == TransactionType.EXPENSE.name }.forEach { tx ->
            cal.timeInMillis = tx.dateTimestamp
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
            val dayLabel = when (dayOfWeek) {
                Calendar.MONDAY -> "Mon"
                Calendar.TUESDAY -> "Tue"
                Calendar.WEDNESDAY -> "Wed"
                Calendar.THURSDAY -> "Thu"
                Calendar.FRIDAY -> "Fri"
                Calendar.SATURDAY -> "Sat"
                Calendar.SUNDAY -> "Sun"
                else -> "Mon"
            }
            daySpendMap[dayLabel] = (daySpendMap[dayLabel] ?: 0.0) + tx.amount
        }

        return days.map { WeeklySpend(it, daySpendMap[it] ?: 0.0) }
    }

    private fun generateInsights(
        transactions: List<TransactionEntity>,
        totalExpense: Double,
        remainingBudget: Double,
        budgetLimit: Double,
        categorySpends: List<CategorySpend>,
        goals: List<SavingsGoalEntity>
    ): List<SmartInsight> {
        val list = mutableListOf<SmartInsight>()

        // 1. Budget remaining insight
        if (remainingBudget < 0) {
            list.add(
                SmartInsight(
                    id = "ins_overbudget",
                    title = "Over Budget Warning",
                    description = "You have exceeded your monthly budget by ${formatRupee(-remainingBudget)}. Consider reviewing recent non-essential expenses.",
                    type = InsightType.SPENDING_ALERT,
                    actionText = "View Budgets"
                )
            )
        } else {
            list.add(
                SmartInsight(
                    id = "ins_remaining",
                    title = "Monthly Budget Status",
                    description = "You have ${formatRupee(remainingBudget)} remaining in your monthly budget (${String.format(Locale.getDefault(), "%.0f", (remainingBudget / budgetLimit) * 100)}% left).",
                    type = InsightType.BUDGET_STATUS
                )
            )
        }

        // 2. Top Spending Category
        if (categorySpends.isNotEmpty()) {
            val topCat = categorySpends.first()
            val catPct = (topCat.percentage * 100).toInt()
            list.add(
                SmartInsight(
                    id = "ins_top_cat",
                    title = "Highest Expense Area",
                    description = "${topCat.category.title} accounts for $catPct% of your total expenses (${formatRupee(topCat.totalAmount)} spent).",
                    type = InsightType.SPENDING_ALERT
                )
            )
        }

        // 3. Food spending rule
        val foodSpend = categorySpends.find { it.category == TransactionCategory.FOOD }?.totalAmount ?: 0.0
        if (foodSpend > 4000) {
            list.add(
                SmartInsight(
                    id = "ins_food_spend",
                    title = "Food & Dining Insight",
                    description = "Food & Groceries is your major regular expense at ${formatRupee(foodSpend)}. Planning weekly meals can save up to ₹1,500 monthly.",
                    type = InsightType.SAVINGS_TIP
                )
            )
        }

        // 4. Daily Savings Recommendation
        val activeGoal = goals.firstOrNull { !it.isCompleted }
        if (activeGoal != null) {
            val remainingGoalAmount = activeGoal.targetAmount - activeGoal.currentAmount
            val dailyAmount = (remainingGoalAmount / 60).coerceAtLeast(50.0)
            list.add(
                SmartInsight(
                    id = "ins_daily_save",
                    title = "Daily Goal Booster",
                    description = "Saving just ${formatRupee(dailyAmount)} per day will help you reach your '${activeGoal.title}' goal faster!",
                    type = InsightType.SAVINGS_TIP,
                    actionText = "Add Savings"
                )
            )
        }

        // 5. Positive Savings Trend
        if (totalExpense < budgetLimit * 0.7 && totalExpense > 0) {
            list.add(
                SmartInsight(
                    id = "ins_positive",
                    title = "Great Money Habits!",
                    description = "You've kept your spending well under 70% of your budget limit. Excellent financial discipline!",
                    type = InsightType.POSITIVE_TREND
                )
            )
        }

        return list
    }
}
