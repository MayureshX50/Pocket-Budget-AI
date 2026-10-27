package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.TransactionEntity
import com.example.model.*
import com.example.ui.components.TransactionItemCard
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun TransactionsScreen(
    uiState: FinanceUiState,
    onSearchQueryChange: (String) -> Unit,
    onTypeFilterSelect: (TransactionType?) -> Unit,
    onCategoryFilterSelect: (TransactionCategory?) -> Unit,
    onClearFilters: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredList = uiState.filteredTransactions
    val totalFilteredIncome = filteredList.filter { it.type == TransactionType.INCOME.name }.sumOf { it.amount }
    val totalFilteredExpense = filteredList.filter { it.type == TransactionType.EXPENSE.name }.sumOf { it.amount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransactionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("transactions_screen_column")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transaction History",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by title, notes, or category...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_tx_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = uiState.selectedTypeFilter == null && uiState.selectedCategoryFilter == null,
                        onClick = onClearFilters,
                        label = { Text("All", fontSize = 12.sp) }
                    )
                }

                item {
                    FilterChip(
                        selected = uiState.selectedTypeFilter == TransactionType.INCOME,
                        onClick = {
                            if (uiState.selectedTypeFilter == TransactionType.INCOME) onTypeFilterSelect(null)
                            else onTypeFilterSelect(TransactionType.INCOME)
                        },
                        label = { Text("Income Only", fontSize = 12.sp) }
                    )
                }

                item {
                    FilterChip(
                        selected = uiState.selectedTypeFilter == TransactionType.EXPENSE,
                        onClick = {
                            if (uiState.selectedTypeFilter == TransactionType.EXPENSE) onTypeFilterSelect(null)
                            else onTypeFilterSelect(TransactionType.EXPENSE)
                        },
                        label = { Text("Expense Only", fontSize = 12.sp) }
                    )
                }

                items(TransactionCategory.entries) { cat ->
                    FilterChip(
                        selected = uiState.selectedCategoryFilter == cat,
                        onClick = {
                            if (uiState.selectedCategoryFilter == cat) onCategoryFilterSelect(null)
                            else onCategoryFilterSelect(cat)
                        },
                        label = { Text(cat.title, fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Bar for Current Filter
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filteredList.size} Records Found",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row {
                        if (totalFilteredIncome > 0) {
                            Text(
                                text = "+${formatRupee(totalFilteredIncome)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        if (totalFilteredExpense > 0) {
                            Text(
                                text = "-${formatRupee(totalFilteredExpense)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No transactions found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onClearFilters) {
                            Text("Reset Search & Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredList, key = { it.id }) { tx ->
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        TransactionItemCard(
                            transaction = tx,
                            onEditClick = { showDeleteDialog = true },
                            onDeleteClick = { onDeleteTransaction(tx) }
                        )

                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Delete Transaction?") },
                                text = { Text("Are you sure you want to delete '${tx.title}' of ${formatRupee(tx.amount)}?") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            onDeleteTransaction(tx)
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
