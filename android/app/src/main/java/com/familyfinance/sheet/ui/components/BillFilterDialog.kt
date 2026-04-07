package com.familyfinance.sheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.familyfinance.sheet.data.model.BillCategory
import com.familyfinance.sheet.data.model.BillType
import com.familyfinance.sheet.ui.components.PRESET_COLORS

/**
 * 账单筛选对话框
 */
@Composable
fun BillFilterDialog(
    categories: List<BillCategory>,
    selectedCategoryId: String?,
    selectedType: BillType?,
    selectedColor: Long?,
    startDate: String?,
    endDate: String?,
    minAmount: String?,
    maxAmount: String?,
    onCategorySelected: (String?) -> Unit,
    onTypeSelected: (BillType?) -> Unit,
    onColorSelected: (Long?) -> Unit,
    onStartDateChanged: (String?) -> Unit,
    onEndDateChanged: (String?) -> Unit,
    onMinAmountChanged: (String?) -> Unit,
    onMaxAmountChanged: (String?) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    var categoryId by remember { mutableStateOf(selectedCategoryId) }
    var type by remember { mutableStateOf(selectedType) }
    var color by remember { mutableStateOf(selectedColor) }
    var startDateText by remember { mutableStateOf(startDate ?: "") }
    var endDateText by remember { mutableStateOf(endDate ?: "") }
    var minAmountText by remember { mutableStateOf(minAmount ?: "") }
    var maxAmountText by remember { mutableStateOf(maxAmount ?: "") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("筛选账单") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 类型筛选
                Column {
                    Text(
                        text = "类型",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = if (type == BillType.INCOME) null else BillType.INCOME }
                                .then(
                                    if (type == BillType.INCOME) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = Color(0xFFF44336), // 红色边框
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (type == BillType.INCOME) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Text(
                                text = "收入",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                color = if (type == BillType.INCOME) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { type = if (type == BillType.EXPENSE) null else BillType.EXPENSE }
                                .then(
                                    if (type == BillType.EXPENSE) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = Color(0xFFF44336), // 红色边框
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    } else {
                                        Modifier
                                    }
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (type == BillType.EXPENSE) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                        ) {
                            Text(
                                text = "支出",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                color = if (type == BillType.EXPENSE) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                
                // 分类筛选
                Column {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Card(
                                modifier = Modifier.clickable { categoryId = null },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (categoryId == null) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Text(
                                    text = "全部",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (categoryId == null) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        items(categories) { cat ->
                            Card(
                                modifier = Modifier.clickable { 
                                    categoryId = if (categoryId == cat.id) null else cat.id 
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (categoryId == cat.id) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                    color = if (categoryId == cat.id) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                    }
                }
                
                // 颜色筛选
                Column {
                    Text(
                        text = "颜色",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // "全部"选项 - 使用一个特殊的指示器
                            val isAllSelected = color == null
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = CircleShape
                                    )
                                    .then(
                                        if (isAllSelected) {
                                            Modifier.border(
                                                width = 3.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { color = null }
                            ) {
                                // 在中心显示一个图标或文字表示"全部"
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "全部",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isAllSelected) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                        items(PRESET_COLORS) { presetColor ->
                            val isSelected = color == presetColor
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        color = Color(presetColor),
                                        shape = CircleShape
                                    )
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 3.dp,
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = CircleShape
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { color = if (isSelected) null else presetColor }
                            )
                        }
                    }
                }
                
                // 日期范围
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = startDateText,
                        onValueChange = { startDateText = it },
                        label = { Text("开始日期") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("yyyy-MM-dd") }
                    )
                    OutlinedTextField(
                        value = endDateText,
                        onValueChange = { endDateText = it },
                        label = { Text("结束日期") },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("yyyy-MM-dd") }
                    )
                }
                
                // 金额范围
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minAmountText,
                        onValueChange = { minAmountText = it },
                        label = { Text("最小金额") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxAmountText,
                        onValueChange = { maxAmountText = it },
                        label = { Text("最大金额") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = {
                    categoryId = null
                    type = null
                    color = null
                    startDateText = ""
                    endDateText = ""
                    minAmountText = ""
                    maxAmountText = ""
                    onClear()
                }) {
                    Text("清除")
                }
                TextButton(
                    onClick = {
                        onCategorySelected(categoryId)
                        onTypeSelected(type)
                        onColorSelected(color)
                        onStartDateChanged(if (startDateText.isBlank()) null else startDateText)
                        onEndDateChanged(if (endDateText.isBlank()) null else endDateText)
                        onMinAmountChanged(if (minAmountText.isBlank()) null else minAmountText)
                        onMaxAmountChanged(if (maxAmountText.isBlank()) null else maxAmountText)
                        onDismiss()
                    }
                ) {
                    Text("确定")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

