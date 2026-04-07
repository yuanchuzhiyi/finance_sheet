package com.familyfinance.sheet.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyfinance.sheet.data.model.MetricComparison
import com.familyfinance.sheet.ui.theme.Amber600
import com.familyfinance.sheet.ui.theme.Emerald600
import com.familyfinance.sheet.ui.theme.Indigo600
import com.familyfinance.sheet.ui.theme.Rose600
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * 汇总卡片组件
 */
@Composable
fun SummaryCard(
    title: String,
    value: Double,
    accentColor: Color,
    comparison: MetricComparison? = null,
    comparisonLabel: String? = null,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    val formattedValue = NumberFormat.getNumberInstance(Locale.CHINA).format(value)
    
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "¥ $formattedValue",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor
                )
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor.copy(alpha = 0.8f)
                    )
                }
            }
            if (comparison != null && !comparisonLabel.isNullOrBlank()) {
                Text(
                    text = buildComparisonText(comparisonLabel, comparison),
                    style = MaterialTheme.typography.bodySmall,
                    color = CategoryColors.getPositiveNegativeColor(comparison.delta),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

private fun buildComparisonText(label: String, comparison: MetricComparison): String {
    val amountFormatter = NumberFormat.getCurrencyInstance(Locale.CHINA).apply {
        currency = Currency.getInstance("CNY")
        maximumFractionDigits = 2
    }
    val percentText = comparison.percentChange?.let {
        NumberFormat.getPercentInstance(Locale.CHINA).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 2
        }.format(it)
    } ?: "--"
    val sign = if (comparison.delta > 0) "+" else ""
    return "较 $label ${sign}${amountFormatter.format(comparison.delta)} / $percentText"
}

/**
 * 获取分类颜色
 */
object CategoryColors {
    val income = Emerald600
    val expense = Rose600
    val asset = Indigo600
    val liability = Amber600
    
    fun getPositiveNegativeColor(value: Double): Color {
        return if (value >= 0) Emerald600 else Rose600
    }
}
