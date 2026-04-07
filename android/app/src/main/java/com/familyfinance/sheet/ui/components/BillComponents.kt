package com.familyfinance.sheet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.familyfinance.sheet.data.model.Bill
import com.familyfinance.sheet.data.model.BillCategory
import com.familyfinance.sheet.data.model.BillType
import com.familyfinance.sheet.ui.components.CustomDatePickerDialog
import java.text.NumberFormat
import java.util.Locale

/**
 * 预设颜色列表
 */
val PRESET_COLORS = listOf(
    0xFFF44336L,  // 红色
    0xFFE91E63L,  // 粉红色
    0xFF9C27B0L,  // 紫色
    0xFF673AB7L,  // 深紫色
    0xFF3F51B5L,  // 靛蓝色
    0xFF2196F3L,  // 蓝色
    0xFF00BCD4L,  // 青色
    0xFF009688L,  // 青绿色
    0xFF4CAF50L,  // 绿色
    0xFF8BC34AL,  // 浅绿色
    0xFFCDDC39L,  // 黄绿色
    0xFFFFEB3BL,   // 黄色
    0xFFFFC107L,   // 琥珀色
    0xFFFF9800L,   // 橙色
    0xFFFF5722L,   // 深橙色
    0xFF795548L,   // 棕色
    0xFF9E9E9EL,   // 灰色
    0xFF607D8BL    // 蓝灰色
)

/**
 * 颜色选择器
 */
@Composable
fun ColorPicker(
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "选择颜色",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(PRESET_COLORS) { color ->
                val isSelected = color == selectedColor
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = Color(color),
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
                        .clickable { onColorSelected(color) }
                )
            }
        }
    }
}

/**
 * 分类卡片
 */
@Composable
fun CategoryCard(
    category: BillCategory,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * 账单卡片（边框显示颜色）
 */
@Composable
fun BillCard(
    bill: Bill,
    category: BillCategory?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val formattedAmount = NumberFormat.getNumberInstance(Locale.CHINA).format(bill.amount)
    val billColor = Color(bill.color)
    
    Card(
        modifier = modifier
            .border(
                width = 2.dp,
                color = billColor,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface // 使用默认背景色
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            color = billColor,
                            shape = CircleShape
                        )
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category?.name ?: "未知分类",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (bill.note.isNotEmpty()) {
                        Text(
                            text = bill.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "¥$formattedAmount",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.type == BillType.INCOME) {
                        Color(0xFF4CAF50)
                    } else {
                        Color(0xFFF44336)
                    }
                )
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 添加/编辑分类对话框
 */
@Composable
fun CategoryDialog(
    title: String,
    initialName: String = "",
    initialType: BillType = BillType.EXPENSE,
    onConfirm: (String, BillType) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedType by remember { mutableStateOf(initialType) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Column {
                    Text(
                        text = "类型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = BillType.INCOME }
                                .then(
                                    if (selectedType == BillType.INCOME) {
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
                                containerColor = if (selectedType == BillType.INCOME) {
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
                                    .padding(16.dp),
                                color = if (selectedType == BillType.INCOME) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = BillType.EXPENSE }
                                .then(
                                    if (selectedType == BillType.EXPENSE) {
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
                                containerColor = if (selectedType == BillType.EXPENSE) {
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
                                    .padding(16.dp),
                                color = if (selectedType == BillType.EXPENSE) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedType)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 添加/编辑账单对话框
 */
@Composable
fun BillDialog(
    title: String,
    initialAmount: String = "",
    initialCategoryId: String = "",
    initialType: BillType = BillType.EXPENSE,
    initialColor: Long = PRESET_COLORS[0],
    initialDate: String = "",
    initialNote: String = "",
    categories: List<BillCategory>,
    onConfirm: (Double, String, BillType, Long, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var amount by remember { mutableStateOf(initialAmount) }
    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var selectedType by remember { mutableStateOf(initialType) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var date by remember { mutableStateOf(initialDate) }
    var note by remember { mutableStateOf(initialNote) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    
    // 分类不再按类型过滤，显示所有分类
    val filteredCategories = categories
    
    // 如果没有选中分类且分类列表不为空，默认选择第一个
    if (selectedCategoryId.isBlank() && filteredCategories.isNotEmpty()) {
        selectedCategoryId = filteredCategories[0].id
    }
    
    val selectedCategory = filteredCategories.find { it.id == selectedCategoryId }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 类型选择
                Column {
                    Text(
                        text = "类型",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    selectedType = BillType.INCOME
                                    selectedCategoryId = "" // 切换类型时清空分类选择
                                }
                                .then(
                                    if (selectedType == BillType.INCOME) {
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
                                containerColor = if (selectedType == BillType.INCOME) {
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
                                    .padding(16.dp),
                                color = if (selectedType == BillType.INCOME) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { 
                                    selectedType = BillType.EXPENSE
                                    selectedCategoryId = "" // 切换类型时清空分类选择
                                }
                                .then(
                                    if (selectedType == BillType.EXPENSE) {
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
                                containerColor = if (selectedType == BillType.EXPENSE) {
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
                                    .padding(16.dp),
                                color = if (selectedType == BillType.EXPENSE) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
                
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("金额") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                var showDatePicker by remember { mutableStateOf(false) }
                
                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    label = { Text("日期 (yyyy-MM-dd)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDatePicker = true },
                    singleLine = true,
                    readOnly = true,
                    enabled = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { showDatePicker = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "选择日期",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )
                
                if (showDatePicker) {
                    CustomDatePickerDialog(
                        initialDate = if (date.isNotBlank()) date else null,
                        onDateSelected = { selectedDate ->
                            date = selectedDate
                            showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }
                
                // 分类选择
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCategoryPicker = true },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCategory?.name ?: "请选择分类",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (selectedCategory != null) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "选择分类",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                // 颜色选择
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it }
                )
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && 
                        selectedCategoryId.isNotBlank() && date.isNotBlank()) {
                        onConfirm(amountValue, selectedCategoryId, selectedType, selectedColor, date, note)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
    
    // 分类选择对话框
    if (showCategoryPicker) {
        AlertDialog(
            onDismissRequest = { showCategoryPicker = false },
            title = { Text("选择分类") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filteredCategories.forEach { category ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCategoryId = category.id
                                    showCategoryPicker = false
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (category.id == selectedCategoryId) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCategoryPicker = false }) {
                    Text("取消")
                }
            }
        )
    }
}

