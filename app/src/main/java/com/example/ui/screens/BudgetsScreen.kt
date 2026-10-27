package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TransactionCategory
import com.example.model.TransactionType
import com.example.model.formatRupee
import com.example.ui.components.BudgetProgressCard
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun BudgetsScreen(
    uiState: FinanceUiState,
    onEditBudgetClick: (categoryName: String, currentLimit: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalExpense = uiState.totalExpenseThisMonth
    val totalLimit = uiState.monthlyBudgetLimit
    val totalRemaining = uiState.remainingBudget

    var showCarryForwardDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("budgets_screen_column"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Budget Planner",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Control limits & prevent overspending",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = { onEditBudgetClick("TOTAL", totalLimit) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Budget", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Edit Total Limit", fontSize = 12.sp)
                }
            }
        }

        // Overspending / 80% Warnings Summary Box
        if (uiState.overbudgetCategories.isNotEmpty() || uiState.budget80PercentCategories.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.overbudgetCategories.isNotEmpty()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        else com.example.ui.theme.AmberWarning.copy(alpha = 0.15f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = if (uiState.overbudgetCategories.isNotEmpty()) MaterialTheme.colorScheme.error else com.example.ui.theme.AmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Budget Alerts & Overspending Warnings",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (uiState.overbudgetCategories.isNotEmpty()) {
                            Text(
                                text = "Exceeded limit: ${uiState.overbudgetCategories.joinToString(", ")}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (uiState.budget80PercentCategories.isNotEmpty()) {
                            Text(
                                text = "Reached 80%+ limit: ${uiState.budget80PercentCategories.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = com.example.ui.theme.AmberWarning
                            )
                        }
                    }
                }
            }
        }

        // Overall Monthly Budget Progress Card
        item {
            BudgetProgressCard(
                categoryTitle = "TOTAL",
                limitAmount = totalLimit,
                spentAmount = totalExpense,
                onEditClick = { onEditBudgetClick("TOTAL", totalLimit) }
            )
        }

        // Carry Forward Unspent Budget Option
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Carry Forward Unspent Budget",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Add remaining ${formatRupee(totalRemaining.coerceAtLeast(0.0))} to next month's target.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = { showCarryForwardDialog = true },
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("Carry Forward", fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category Budget Limits",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                IconButton(onClick = { onEditBudgetClick("FOOD", 10000.0) }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Category Limit")
                }
            }
        }

        // Category Budgets List
        val expenseCategories = TransactionCategory.entries.filter { it.defaultType == TransactionType.EXPENSE }

        items(expenseCategories, key = { it.name }) { cat ->
            val budgetObj = uiState.budgets.find { it.category == cat.name }
            val limit = budgetObj?.limitAmount ?: when (cat) {
                TransactionCategory.FOOD -> 10000.0
                TransactionCategory.RENT -> 16000.0
                TransactionCategory.TRANSPORT -> 4500.0
                TransactionCategory.SHOPPING -> 5000.0
                TransactionCategory.BILLS -> 5000.0
                else -> 3000.0
            }

            val spent = uiState.transactions
                .filter { it.type == TransactionType.EXPENSE.name && it.category == cat.name }
                .sumOf { it.amount }

            BudgetProgressCard(
                categoryTitle = cat.name,
                limitAmount = limit,
                spentAmount = spent,
                onEditClick = { onEditBudgetClick(cat.name, limit) }
            )
        }
    }

    if (showCarryForwardDialog) {
        AlertDialog(
            onDismissRequest = { showCarryForwardDialog = false },
            title = { Text("Carry Forward Unspent Budget") },
            text = { Text("Will carry forward ${formatRupee(totalRemaining.coerceAtLeast(0.0))} to next month's budget limit.") },
            confirmButton = {
                Button(
                    onClick = {
                        val newLimit = totalLimit + totalRemaining.coerceAtLeast(0.0)
                        onEditBudgetClick("TOTAL", newLimit)
                        showCarryForwardDialog = false
                    }
                ) {
                    Text("Confirm Carry Forward")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCarryForwardDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
