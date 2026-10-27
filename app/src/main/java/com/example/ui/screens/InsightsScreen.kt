package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SmartInsightCard
import com.example.ui.viewmodel.FinanceUiState

@Composable
fun InsightsScreen(
    uiState: FinanceUiState,
    onNavigateToBudgets: () -> Unit,
    onNavigateToGoals: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("insights_screen_column"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Smart AI Insights",
                    tint = com.example.ui.theme.TealAccent,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Smart Money Insights",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Automated local rules & pattern analysis of your expenses",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Hero Summary Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Financial Health Score",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val healthScore = when {
                        uiState.remainingBudget < 0 -> 45
                        uiState.remainingBudget > uiState.monthlyBudgetLimit * 0.3 -> 90
                        else -> 75
                    }

                    Text(
                        text = "$healthScore / 100",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (healthScore >= 75) com.example.ui.theme.EmeraldGreen else MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = when {
                            healthScore >= 80 -> "Excellent! Your income easily covers expenses and leaves a healthy budget margin for savings."
                            healthScore >= 60 -> "Good stability. Keep an eye on high category expenses to stay within target limits."
                            else -> "Budget Attention Needed. Your expenses have crossed planned limits. Review category spending below."
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Personalized Money Observations",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (uiState.smartInsights.isEmpty()) {
            item {
                Text(text = "Add transactions to generate smart insights.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            items(uiState.smartInsights, key = { it.id }) { insight ->
                SmartInsightCard(
                    insight = insight,
                    onActionClick = {
                        if (insight.actionText?.contains("Budget", ignoreCase = true) == true) {
                            onNavigateToBudgets()
                        } else if (insight.actionText?.contains("Savings", ignoreCase = true) == true) {
                            onNavigateToGoals()
                        }
                    }
                )
            }
        }
    }
}
