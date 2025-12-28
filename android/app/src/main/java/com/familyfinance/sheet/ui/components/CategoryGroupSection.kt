package com.familyfinance.sheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyfinance.sheet.R
import com.familyfinance.sheet.data.model.CategoryGroup
import com.familyfinance.sheet.data.model.CategoryType
import com.familyfinance.sheet.data.model.ReportItem
import com.familyfinance.sheet.ui.theme.Amber50
import com.familyfinance.sheet.ui.theme.Amber600
import com.familyfinance.sheet.ui.theme.Emerald50
import com.familyfinance.sheet.ui.theme.Emerald600
import com.familyfinance.sheet.ui.theme.Indigo50
import com.familyfinance.sheet.ui.theme.Indigo600
import com.familyfinance.sheet.ui.theme.Rose50
import com.familyfinance.sheet.ui.theme.Rose600
import java.text.NumberFormat
import java.util.Locale

/**
 * 分类组区域
 */
@Composable
fun CategoryGroupSection(
    group: CategoryGroup,
    period: String,
    showNotes: Boolean,
    onAddItem: () -> Unit,
    onValueChange: (String, Double) -> Unit,
    onNoteChange: (String, String) -> Unit,
    onRenameItem: (String) -> Unit,
    onAddSubItem: (String) -> Unit,
    onDeleteItem: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, accentColor) = when (group.type) {
        CategoryType.INCOME -> Emerald50 to Emerald600
        CategoryType.EXPENSE -> Rose50 to Rose606
        CategoryType.ASSET -> Indigo50 to Indigo600
        CategoryType.LIABILITY -> Amber50 to Amber606
    }
    
    val categoryLabel = when (group.type) {
        CategoryType.INCOME -> stringResource(R.string.income)
        CategoryType.EXPENSE -> stringResource(R.string.expense)
        CategoryType.ASSET -> stringResource(R.string.asset)
        CategoryType.LIABILITY -> stringResource(R.string.liability)
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column {
            // 分组标题
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = categoryLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                    Text(
                        text = stringResource(R.string.double_click_rename),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onAddItem) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(stringResource(R.string.add_item))
                }
            }
            
            // 表头
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.column_item),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                if (showNotes) {
                    Text(
                        text = stringResource(R.string.column_note),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    text = period,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(0.8f)
                )
                Text(
                    text = stringResource(R.string.column_operation),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 科目列表
            group.items.forEachIndexed { index, item ->
                RenderItemWithChildren(
                    item = item,
                    period = period,
                    depth = 0,
                    showNotes = showNotes,
                    onValueChange = { value -> onValueChange(item.id, value) },
                    onNoteChange = { note -> onNoteChange(item.id, note) },
                    onRename = { onRenameItem(item.id) },
                    onAddSubItem = { onAddSubItem(item.id) },
                    onDelete = { onDeleteItem(item.id) },
                    onChildValueChange = onValueChange,
                    onChildNoteChange = onNoteChange,
                    onChildRename = onRenameItem,
                    onChildAddSubItem = onAddSubItem,
                    onChildDelete = onDeleteItem
                )
                if (index < group.items.size - 1) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 0.5.dp
                    )
                }
            }
            
            // 小计
            val total = group.getTotal(period)
            val formattedTotal = NumberFormat.getNumberInstance(Locale.CHINA).format(total)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.subtotal),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "¥ $formattedTotal",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Fix color references
private val Rose606 = Rose600
private val Amber606 = Amber600

@Composable
private fun RenderItemWithChildren(
    item: ReportItem,
    period: String,
    depth: Int,
    showNotes: Boolean,
    onValueChange: (Double) -> Unit,
    onNoteChange: (String) -> Unit,
    onRename: () -> Unit,
    onAddSubItem: () -> Unit,
    onDelete: () -> Unit,
    onChildValueChange: (String, Double) -> Unit,
    onChildNoteChange: (String, String) -> Unit,
    onChildRename: (String) -> Unit,
    onChildAddSubItem: (String) -> Unit,
    onChildDelete: (String) -> Unit
) {
    Column {
        ReportItemRow(
            item = item,
            period = period,
            depth = depth,
            showNotes = showNotes,
            onValueChange = onValueChange,
            onNoteChange = onNoteChange,
            onRename = onRename,
            onAddSubItem = onAddSubItem,
            onDelete = onDelete
        )
        
        item.children?.forEach { child ->
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
            RenderItemWithChildren(
                item = child,
                period = period,
                depth = depth + 1,
                showNotes = showNotes,
                onValueChange = { value -> onChildValueChange(child.id, value) },
                onNoteChange = { note -> onChildNoteChange(child.id, note) },
                onRename = { onChildRename(child.id) },
                onAddSubItem = { onChildAddSubItem(child.id) },
                onDelete = { onChildDelete(child.id) },
                onChildValueChange = onChildValueChange,
                onChildNoteChange = onChildNoteChange,
                onChildRename = onChildRename,
                onChildAddSubItem = onChildAddSubItem,
                onChildDelete = onChildDelete
            )
        }
    }
}
