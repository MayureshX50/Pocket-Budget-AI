package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun HomeScreen(
    uiState: FinanceUiState,
    onAddTransactionClick: () -> Unit,
    onViewAllTransactionsClick: () -> Unit,
    onViewBudgetsClick: () -> Unit,
    onViewBillsClick: () -> Unit,
    onViewGoalsClick: () -> Unit,
    onMarkBillPaid: (bill: com.example.data.local.entities.BillEntity) -> Unit,
    onClearDemoClick: () -> Unit,
    onAddContributionClick: (goal: com.example.data.local.entities.SavingsGoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Header / Tagline
        item {
            Column {
                Text(
                    text = "Pocket Budget AI",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Manage Money Smarter. Save More Every Month.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Demo Data Notice if active
        if (uiState.isDemoDataActive) {
            item {
                DemoDataBanner(onClearDemoClick = onClearDemoClick)
            }
        }

        // Net Balance & Budget Summary Hero Card
        item {
            NetBalanceCard(
                totalIncome = uiState.totalIncomeThisMonth,
                totalExpense = uiState.totalExpenseThisMonth,
                budgetLimit = uiState.monthlyBudgetLimit,
                remainingBudget = uiState.remainingBudget,
                onAddTransactionClick = onAddTransactionClick
            )
        }

        // Top Smart Money Insight Callout
        if (uiState.smartInsights.isNotEmpty()) {
            item {
                SmartInsightCard(
                    insight = uiState.smartInsights.first(),
                    onActionClick = onViewBudgetsClick
                )
            }
        }

        // Weekly Spending Chart
        if (uiState.weeklySpends.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Weekly Spending Trend",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        WeeklySpendingBarChart(
                            weeklySpends = uiState.weeklySpends,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Upcoming Bill Reminders Preview
        if (uiState.bills.any { it.status == "UPCOMING" }) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Bills",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "View All →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onViewBillsClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.bills.filter { it.status == "UPCOMING" }.take(2).forEach { bill ->
                        BillReminderCard(
                            bill = bill,
                            onMarkPaidClick = { onMarkBillPaid(bill) },
                            onDeleteClick = {},
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }

        // Active Savings Goals Preview
        if (uiState.goals.isNotEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Savings Goals",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "View All →",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onViewGoalsClick() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.goals.take(1).forEach { goal ->
                        SavingsGoalCard(
                            goal = goal,
                            onAddContributionClick = { onAddContributionClick(goal) },
                            onDeleteClick = {}
                        )
                    }
                }
            }
        }

        // Recent Transactions Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Transactions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "See All →",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onViewAllTransactionsClick() }
                )
            }
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "No transactions recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = onAddTransactionClick) {
                            Text("Add Your First Transaction")
                        }
                    }
                }
            }
        } else {
            items(uiState.transactions.take(5), key = { it.id }) { tx ->
                TransactionItemCard(
                    transaction = tx,
                    onEditClick = {},
                    onDeleteClick = {}
                )
            }
        }
    }
}
