package com.familyfinance.sheet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.familyfinance.sheet.R
import com.familyfinance.sheet.data.model.ViewMode
import com.familyfinance.sheet.ui.theme.Indigo50
import com.familyfinance.sheet.ui.theme.Indigo500
import com.familyfinance.sheet.ui.theme.Indigo700

/**
 * 视图模式选择器
 */
@Composable
fun ViewModeSelector(
    currentMode: ViewMode,
    onModeChange: (ViewMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ViewMode.entries.forEach { mode ->
            val isSelected = mode == currentMode
            OutlinedButton(
                onClick = { onModeChange(mode) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isSelected) Indigo50 else MaterialTheme.colorScheme.surface,
                    contentColor = if (isSelected) Indigo700 else MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isSelected) Indigo500 else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Text(
                    text = when (mode) {
                        ViewMode.YEAR -> stringResource(R.string.view_mode_year)
                        ViewMode.MONTH -> stringResource(R.string.view_mode_month)
                        ViewMode.DAY -> stringResource(R.string.view_mode_day)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
