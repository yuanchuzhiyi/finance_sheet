package com.familyfinance.sheet.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.familyfinance.sheet.data.model.ReportItem
import com.familyfinance.sheet.ui.theme.Indigo500
import com.familyfinance.sheet.ui.theme.Rose500
import com.familyfinance.sheet.ui.theme.Slate100
import com.familyfinance.sheet.ui.theme.Slate300
import com.familyfinance.sheet.ui.theme.Slate50
import java.text.NumberFormat
import java.util.Locale

/**
 * 科目行组件
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReportItemRow(
    item: ReportItem,
    period: String,
    depth: Int = 0,
    showNotes: Boolean = false,
    onValueChange: (Double) -> Unit,
    onNoteChange: (String) -> Unit,
    onRename: () -> Unit,
    onAddSubItem: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasChildren = !item.children.isNullOrEmpty()
    val displayValue = item.getValue(period)
    var valueText by remember(displayValue) { 
        mutableStateOf(if (displayValue == 0.0) "" else displayValue.toLong().toString())
    }
    var noteText by remember(item.note) { mutableStateOf(item.note ?: "") }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 科目名称
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(start = (depth * 16).dp)
                .combinedClickable(
                    onClick = { },
                    onDoubleClick = onRename
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (depth > 0) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(Slate300)
                )
            }
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        
        // 备注输入框（可选）
        if (showNotes) {
            BasicTextField(
                value = noteText,
                onValueChange = { 
                    noteText = it
                    onNoteChange(it)
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
                    .background(Slate50, shape = MaterialTheme.shapes.small)
                    .padding(8.dp),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(Indigo500),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box {
                        if (noteText.isEmpty()) {
                            Text(
                                text = "备注信息...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
        
        // 金额输入框
        BasicTextField(
            value = valueText,
            onValueChange = { newValue ->
                val filtered = newValue.filter { it.isDigit() || it == '.' || it == '-' }
                valueText = filtered
                filtered.toDoubleOrNull()?.let { onValueChange(it) }
            },
            modifier = Modifier
                .weight(0.8f)
                .background(Slate50, shape = MaterialTheme.shapes.small)
                .padding(8.dp),
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.End
            ),
            cursorBrush = SolidColor(Indigo500),
            singleLine = true,
            enabled = !hasChildren,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (valueText.isEmpty()) {
                        Text(
                            text = "0",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            textAlign = TextAlign.End
                        )
                    }
                    innerTextField()
                }
            }
        )
        
        // 操作按钮
        Row(
            modifier = Modifier.padding(start = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onAddSubItem,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlaylistAdd,
                    contentDescription = "新增子科目",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = Rose500.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
