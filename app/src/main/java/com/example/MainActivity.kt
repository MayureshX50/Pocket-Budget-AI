package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.entities.BillEntity
import com.example.data.local.entities.SavingsGoalEntity
import com.example.model.TransactionCategory
import com.example.ui.components.NavScreen
import com.example.ui.components.PocketBudgetBottomNavigation
import com.example.ui.screens.*
import com.example.ui.theme.PocketBudgetTheme
import com.example.ui.viewmodel.FinanceViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: FinanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val snackbarHostState = remember { SnackbarHostState() }

            // Navigation Screen States
            var currentNavTab by remember { mutableStateOf(NavScreen.HOME) }
            var activeSubScreen by remember { mutableStateOf<SubScreen?>(null) }

            // Modal Sheets States
            var showAddTxSheet by remember { mutableStateOf(false) }
            var showAddBillSheet by remember { mutableStateOf(false) }
            var showAddGoalSheet by remember { mutableStateOf(false) }

            var editBudgetCategory by remember { mutableStateOf<String?>(null) }
            var editBudgetLimit by remember { mutableDoubleStateOf(45000.0) }

            var selectedGoalForContribution by remember { mutableStateOf<SavingsGoalEntity?>(null) }

            // Handle user feedback messages
            LaunchedEffect(uiState.userMessage) {
                uiState.userMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearUserMessage()
                }
            }

            // Android Hardware Back Handler
            if (activeSubScreen != null) {
                BackHandler {
                    activeSubScreen = null
                }
            } else if (currentNavTab != NavScreen.HOME) {
                BackHandler {
                    currentNavTab = NavScreen.HOME
                }
            }

            PocketBudgetTheme(darkTheme = uiState.isDarkTheme) {
                if (!uiState.isOnboardingComplete) {
                    OnboardingScreen(
                        onGetStartedClick = { viewModel.setOnboardingComplete(true) }
                    )
                } else {
                    Scaffold(
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            PocketBudgetBottomNavigation(
                                currentScreen = currentNavTab,
                                onScreenSelected = { tab ->
                                    currentNavTab = tab
                                    activeSubScreen = null // reset sub-screen when switching main tabs
                                }
                            )
                        },
                        modifier = Modifier.fillMaxSize()
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Sub-screens take priority if opened from "More"
                            when (val sub = activeSubScreen) {
                                SubScreen.BILLS -> {
                                    BillsScreen(
                                        uiState = uiState,
                                        onAddBillClick = { showAddBillSheet = true },
                                        onMarkBillPaid = { bill -> viewModel.markBillAsPaid(bill) },
                                        onDeleteBill = { bill -> viewModel.deleteBill(bill) }
                                    )
                                }
                                SubScreen.INSIGHTS -> {
                                    InsightsScreen(
                                        uiState = uiState,
                                        onNavigateToBudgets = {
                                            activeSubScreen = null
                                            currentNavTab = NavScreen.BUDGETS
                                        },
                                        onNavigateToGoals = {
                                            activeSubScreen = null
                                            currentNavTab = NavScreen.GOALS
                                        }
                                    )
                                }
                                SubScreen.REPORTS -> {
                                    ReportsScreen(uiState = uiState)
                                }
                                SubScreen.SETTINGS -> {
                                    SettingsScreen(
                                        uiState = uiState,
                                        onToggleTheme = { viewModel.toggleTheme() },
                                        onResetDemoData = { viewModel.resetDemoData() },
                                        onClearAllData = { viewModel.clearAllData() }
                                    )
                                }
                                null -> {
                                    // Main Navigation Tabs
                                    when (currentNavTab) {
                                        NavScreen.HOME -> {
                                            HomeScreen(
                                                uiState = uiState,
                                                onAddTransactionClick = { showAddTxSheet = true },
                                                onViewAllTransactionsClick = { currentNavTab = NavScreen.TRANSACTIONS },
                                                onViewBudgetsClick = { currentNavTab = NavScreen.BUDGETS },
                                                onViewBillsClick = { activeSubScreen = SubScreen.BILLS },
                                                onViewGoalsClick = { currentNavTab = NavScreen.GOALS },
                                                onMarkBillPaid = { bill -> viewModel.markBillAsPaid(bill) },
                                                onClearDemoClick = { viewModel.clearAllData() },
                                                onAddContributionClick = { goal -> selectedGoalForContribution = goal }
                                            )
                                        }
                                        NavScreen.TRANSACTIONS -> {
                                            TransactionsScreen(
                                                uiState = uiState,
                                                onSearchQueryChange = { q -> viewModel.updateSearchQuery(q) },
                                                onTypeFilterSelect = { t -> viewModel.setTypeFilter(t) },
                                                onCategoryFilterSelect = { c -> viewModel.setCategoryFilter(c) },
                                                onClearFilters = { viewModel.clearFilters() },
                                                onAddTransactionClick = { showAddTxSheet = true },
                                                onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
                                            )
                                        }
                                        NavScreen.BUDGETS -> {
                                            BudgetsScreen(
                                                uiState = uiState,
                                                onEditBudgetClick = { cat, limit ->
                                                    editBudgetCategory = cat
                                                    editBudgetLimit = limit
                                                }
                                            )
                                        }
                                        NavScreen.GOALS -> {
                                            GoalsScreen(
                                                uiState = uiState,
                                                onAddGoalClick = { showAddGoalSheet = true },
                                                onAddContributionClick = { goal -> selectedGoalForContribution = goal },
                                                onDeleteGoal = { goal -> viewModel.deleteGoal(goal) }
                                            )
                                        }
                                        NavScreen.MORE -> {
                                            MoreScreen(
                                                onNavigateToBills = { activeSubScreen = SubScreen.BILLS },
                                                onNavigateToInsights = { activeSubScreen = SubScreen.INSIGHTS },
                                                onNavigateToReports = { activeSubScreen = SubScreen.REPORTS },
                                                onNavigateToSettings = { activeSubScreen = SubScreen.SETTINGS }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Sheet Modals
                    if (showAddTxSheet) {
                        AddEditTransactionSheet(
                            onDismiss = { showAddTxSheet = false },
                            onSave = { title, amount, type, category, paymentMethod, notes ->
                                viewModel.addTransaction(title, amount, type, category, paymentMethod, notes)
                            }
                        )
                    }

                    if (showAddBillSheet) {
                        AddEditBillSheet(
                            onDismiss = { showAddBillSheet = false },
                            onSave = { name, amount, dueDateMs, category, recurrence, notes ->
                                viewModel.addBill(name, amount, dueDateMs, category, recurrence, notes)
                            }
                        )
                    }

                    if (showAddGoalSheet) {
                        AddEditGoalSheet(
                            onDismiss = { showAddGoalSheet = false },
                            onSave = { title, targetAmt, initialAmt, days, notes ->
                                val dueMs = days?.let { System.currentTimeMillis() + (it * 86400000L) }
                                viewModel.addGoal(title, targetAmt, initialAmt, dueMs, notes)
                            }
                        )
                    }

                    if (editBudgetCategory != null) {
                        AddEditBudgetSheet(
                            initialCategory = editBudgetCategory!!,
                            initialLimit = editBudgetLimit,
                            onDismiss = { editBudgetCategory = null },
                            onSave = { categoryName, limitAmount ->
                                viewModel.setBudgetLimit(categoryName, limitAmount)
                            }
                        )
                    }

                    if (selectedGoalForContribution != null) {
                        AddContributionSheet(
                            goal = selectedGoalForContribution!!,
                            onDismiss = { selectedGoalForContribution = null },
                            onSave = { amount, notes, isWithdrawal ->
                                viewModel.addSavingsContribution(selectedGoalForContribution!!, amount, notes, isWithdrawal)
                            }
                        )
                    }
                }
            }
        }
    }
}

enum class SubScreen {
    BILLS,
    INSIGHTS,
    REPORTS,
    SETTINGS
}
