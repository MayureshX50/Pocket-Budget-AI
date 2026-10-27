package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.*
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Helper function to format amount in Indian Rupees (₹)
fun formatRupee(amount: Double, showSymbol: Boolean = true): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    formatter.maximumFractionDigits = 0
    val formatted = formatter.format(amount)
    return if (showSymbol) {
        // Ensure standard ₹ symbol
        if (formatted.startsWith("₹") || formatted.startsWith("INR")) {
            formatted.replace("INR", "₹")
        } else {
            "₹" + String.format(Locale("en", "IN"), "%,.0f", amount)
        }
    } else {
        String.format(Locale("en", "IN"), "%,.0f", amount)
    }
}

fun formatDate(timestamp: Long, pattern: String = "dd MMM, yyyy"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(timestamp))
}

enum class TransactionType {
    INCOME,
    EXPENSE
}

enum class PaymentMethod(val label: String) {
    CASH("Cash"),
    UPI("UPI"),
    DEBIT_CARD("Debit Card"),
    CREDIT_CARD("Credit Card"),
    BANK_TRANSFER("Bank Transfer"),
    OTHER("Other")
}

enum class TransactionCategory(
    val title: String,
    val defaultType: TransactionType,
    val icon: ImageVector,
    val color: Color
) {
    FOOD("Food & Groceries", TransactionType.EXPENSE, Icons.Default.Restaurant, Color(0xFFF97316)),
    TRANSPORT("Transport", TransactionType.EXPENSE, Icons.Default.DirectionsCar, Color(0xFF3B82F6)),
    SHOPPING("Shopping", TransactionType.EXPENSE, Icons.Default.ShoppingBag, Color(0xFFEC4899)),
    RENT("Rent & Housing", TransactionType.EXPENSE, Icons.Default.Home, Color(0xFF8B5CF6)),
    EDUCATION("Education", TransactionType.EXPENSE, Icons.Default.School, Color(0xFF06B6D4)),
    HEALTHCARE("Healthcare", TransactionType.EXPENSE, Icons.Default.LocalHospital, Color(0xFFEF4444)),
    BILLS("Bills & Utilities", TransactionType.EXPENSE, Icons.Default.Receipt, Color(0xFFEAB308)),
    ENTERTAINMENT("Entertainment", TransactionType.EXPENSE, Icons.Default.Movie, Color(0xFFA855F7)),
    SALARY("Salary", TransactionType.INCOME, Icons.Default.Payments, Color(0xFF10B981)),
    FREELANCE("Freelance Income", TransactionType.INCOME, Icons.Default.Work, Color(0xFF14B8A6)),
    INVESTMENT("Investment / Interest", TransactionType.INCOME, Icons.Default.TrendingUp, Color(0xFF22C55E)),
    OTHER("Other", TransactionType.EXPENSE, Icons.Default.Category, Color(0xFF64748B));

    companion object {
        fun fromString(name: String): TransactionCategory {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.title.equals(name, ignoreCase = true) }
                ?: OTHER
        }
    }
}

enum class BillStatus {
    UPCOMING,
    PAID,
    OVERDUE
}

enum class BillRecurrence(val label: String) {
    MONTHLY("Monthly"),
    WEEKLY("Weekly"),
    YEARLY("Yearly"),
    ONE_TIME("One-time")
}

data class SmartInsight(
    val id: String,
    val title: String,
    val description: String,
    val type: InsightType,
    val actionText: String? = null
)

enum class InsightType {
    SPENDING_ALERT,   // Overbudget or high spending
    SAVINGS_TIP,      // Ways to save
    BUDGET_STATUS,    // Budget remaining
    POSITIVE_TREND    // Saved money / decreased spending
}

data class CategorySpend(
    val category: TransactionCategory,
    val totalAmount: Double,
    val percentage: Float
)

data class WeeklySpend(
    val dayLabel: String,
    val amount: Double
)
