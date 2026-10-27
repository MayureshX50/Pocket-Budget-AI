package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MoreScreen(
    onNavigateToBills: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("more_screen_column")
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "More Financial Tools",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Bill reminders, AI insights, analytics & preferences",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Grid Items
        MoreFeatureItemCard(
            title = "Bill Reminders",
            subtitle = "Track electricity, rent & postpaid due dates",
            icon = Icons.Default.Event,
            iconTint = com.example.ui.theme.AmberWarning,
            onClick = onNavigateToBills
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreFeatureItemCard(
            title = "Smart Money Insights",
            subtitle = "Local rule-based pattern analysis & spending advice",
            icon = Icons.Default.AutoAwesome,
            iconTint = com.example.ui.theme.TealAccent,
            onClick = onNavigateToInsights
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreFeatureItemCard(
            title = "Monthly Reports & Analytics",
            subtitle = "Income vs Expense trends & category donut charts",
            icon = Icons.Default.PieChart,
            iconTint = com.example.ui.theme.EmeraldGreen,
            onClick = onNavigateToReports
        )

        Spacer(modifier = Modifier.height(12.dp))

        MoreFeatureItemCard(
            title = "Settings & Privacy",
            subtitle = "Dark theme toggle, clear records & demo data",
            icon = Icons.Default.Settings,
            iconTint = com.example.ui.theme.RoyalBlue,
            onClick = onNavigateToSettings
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun MoreFeatureItemCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
