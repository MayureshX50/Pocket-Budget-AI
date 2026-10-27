package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.BillEntity
import com.example.ui.components.BillReminderCard
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun BillsScreen(
    uiState: FinanceUiState,
    onAddBillClick: () -> Unit,
    onMarkBillPaid: (BillEntity) -> Unit,
    onDeleteBill: (BillEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Upcoming", "Paid Records")

    val upcomingBills = uiState.bills.filter { it.status != "PAID" }
    val paidBills = uiState.bills.filter { it.status == "PAID" }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBillClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_bill")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Bill")
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("bills_screen_column")
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Bill Reminders",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Track electricity, rent, internet & subscription due dates",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = if (index == 0) "$title (${upcomingBills.size})" else "$title (${paidBills.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val currentList = if (selectedTab == 0) upcomingBills else paidBills

            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedTab == 0) "No upcoming bills due!" else "No paid bill history.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (selectedTab == 0) {
                            Button(onClick = onAddBillClick) {
                                Text("Add Bill Reminder")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(currentList, key = { it.id }) { bill ->
                        BillReminderCard(
                            bill = bill,
                            onMarkPaidClick = { onMarkBillPaid(bill) },
                            onDeleteClick = { onDeleteBill(bill) }
                        )
                    }
                }
            }
        }
    }
}
