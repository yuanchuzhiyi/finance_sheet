package com.familyfinance.sheet.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyfinance.sheet.R
import com.familyfinance.sheet.data.model.BillType
import com.familyfinance.sheet.ui.components.BillCard
import com.familyfinance.sheet.ui.components.BillDialog
import com.familyfinance.sheet.ui.components.BillFilterDialog
import com.familyfinance.sheet.ui.components.CategoryDialog
import com.familyfinance.sheet.ui.components.ConfirmDialog
import com.familyfinance.sheet.ui.components.CustomDatePickerDialog
import com.familyfinance.sheet.ui.components.SummaryCard
import com.familyfinance.sheet.ui.screens.CategoryManagementScreen
import com.familyfinance.sheet.viewmodel.BillViewModel
import kotlinx.coroutines.launch

/**
 * 记账簿主界面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BillScreen(
    viewModel: BillViewModel = viewModel()
) {
    val context = LocalContext.current
    val billBookData by viewModel.billBookData.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val filterBySelectedDate by viewModel.filterBySelectedDate.collectAsState()
    val selectedBillType by viewModel.selectedBillType.collectAsState()
    val currentDateBills by viewModel.currentDateBills.collectAsState()
    val currentDateIncome by viewModel.currentDateIncome.collectAsState()
    val currentDateExpense by viewModel.currentDateExpense.collectAsState()
    val currentDateBalance by viewModel.currentDateBalance.collectAsState()
    val filteredCategories by viewModel.filteredCategories.collectAsState()
    val filterCategoryId by viewModel.filterCategoryId.collectAsState()
    val filterColor by viewModel.filterColor.collectAsState()
    val filterStartDate by viewModel.filterStartDate.collectAsState()
    val filterEndDate by viewModel.filterEndDate.collectAsState()
    val filterMinAmount by viewModel.filterMinAmount.collectAsState()
    val filterMaxAmount by viewModel.filterMaxAmount.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 对话框状态
    var showAddBillDialog by remember { mutableStateOf(false) }
    var showEditBillDialog by remember { mutableStateOf<com.familyfinance.sheet.data.model.Bill?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf<com.familyfinance.sheet.data.model.BillCategory?>(null) }
    var showDeleteBillDialog by remember { mutableStateOf<com.familyfinance.sheet.data.model.Bill?>(null) }
    var showDeleteCategoryDialog by remember { mutableStateOf<com.familyfinance.sheet.data.model.BillCategory?>(null) }
    var showCategoryManagement by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showSelectedDatePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "记账簿",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "筛选",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { showCategoryManagement = true }) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "管理分类"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddBillDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加账单",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 筛选状态提示
            val hasActiveFilters = filterBySelectedDate ||
                selectedBillType != null ||
                filterCategoryId != null || 
                filterColor != null || 
                filterStartDate != null || 
                filterEndDate != null || 
                filterMinAmount != null || 
                filterMaxAmount != null
            
            if (hasActiveFilters) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "筛选已启用",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                // 显示筛选条件摘要
                                val filterTexts = mutableListOf<String>()
                                if (filterBySelectedDate) {
                                    filterTexts.add(
                                        stringResource(R.string.quick_date_filter) + " $selectedDate"
                                    )
                                }
                                if (selectedBillType != null) {
                                    filterTexts.add(if (selectedBillType == BillType.INCOME) "收入" else "支出")
                                }
                                val currentCategoryId = filterCategoryId
                                if (currentCategoryId != null) {
                                    val cat = billBookData.getCategory(currentCategoryId)
                                    cat?.let { filterTexts.add(it.name) }
                                }
                                if (filterColor != null) {
                                    filterTexts.add("颜色")
                                }
                                if (filterStartDate != null || filterEndDate != null) {
                                    filterTexts.add("日期范围")
                                }
                                if (filterMinAmount != null || filterMaxAmount != null) {
                                    filterTexts.add("金额范围")
                                }
                                if (filterTexts.isNotEmpty()) {
                                    Text(
                                        text = "(${filterTexts.joinToString(", ")})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.clearFilters() }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "清除筛选",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
            
            // 账单类型筛选
            item {
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedBillType == null,
                        onClick = { viewModel.setSelectedBillType(null) },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("全部")
                    }
                    SegmentedButton(
                        selected = selectedBillType == BillType.INCOME,
                        onClick = { viewModel.setSelectedBillType(BillType.INCOME) },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("收入")
                    }
                    SegmentedButton(
                        selected = selectedBillType == BillType.EXPENSE,
                        onClick = { viewModel.setSelectedBillType(BillType.EXPENSE) },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("支出")
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.date_focus_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (filterBySelectedDate) {
                                stringResource(R.string.date_focus_selected, selectedDate)
                            } else {
                                stringResource(R.string.date_focus_all)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(onClick = { showSelectedDatePicker = true }) {
                                Text(selectedDate.ifBlank { stringResource(R.string.focus_date) })
                            }
                            if (filterBySelectedDate) {
                                TextButton(onClick = { viewModel.showAllDates() }) {
                                    Text(stringResource(R.string.show_all_dates))
                                }
                            }
                        }
                    }
                }
            }
            
            // 汇总卡片
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryCard(
                        title = "收入",
                        value = currentDateIncome,
                        accentColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryCard(
                        title = "支出",
                        value = currentDateExpense,
                        accentColor = Color(0xFFF44336),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            item {
                SummaryCard(
                    title = "结余",
                    value = currentDateBalance,
                    accentColor = if (currentDateBalance >= 0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // 账单列表标题
            item {
                Text(
                    text = if (filterBySelectedDate) {
                        "账单列表 · $selectedDate (${currentDateBills.size})"
                    } else {
                        "账单列表 (${currentDateBills.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            // 账单列表（按日期分组显示）
            val filteredBills = currentDateBills
            
            if (filteredBills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (filterBySelectedDate) {
                                stringResource(R.string.empty_selected_date)
                            } else {
                                "暂无账单记录"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // 按日期分组
                val billsByDate = filteredBills.groupBy { it.date }
                    .toList()
                    .sortedByDescending { it.first } // 按日期从新到旧排序
                
                billsByDate.forEach { (date, bills) ->
                    // 日期分隔符
                    item(key = "date_header_$date") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // 该日期下的账单
                    items(bills, key = { it.id }) { bill ->
                        val category = billBookData.getCategory(bill.categoryId)
                        BillCard(
                            bill = bill,
                            category = category,
                            onEdit = { showEditBillDialog = bill },
                            onDelete = { showDeleteBillDialog = bill },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
    
    // 添加账单对话框
    if (showAddBillDialog) {
        BillDialog(
            title = "添加账单",
            initialDate = selectedDate,
            categories = billBookData.categories,
            onConfirm = { amount, categoryId, type, color, date, note ->
                viewModel.addBill(amount, categoryId, type, color, date, note)
                showAddBillDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("账单已添加")
                }
            },
            onDismiss = { showAddBillDialog = false }
        )
    }

    if (showSelectedDatePicker) {
        CustomDatePickerDialog(
            initialDate = selectedDate,
            onDateSelected = { date ->
                viewModel.focusOnDate(date)
                showSelectedDatePicker = false
            },
            onDismiss = { showSelectedDatePicker = false }
        )
    }
    
    // 编辑账单对话框
    showEditBillDialog?.let { bill ->
        BillDialog(
            title = "编辑账单",
            initialAmount = bill.amount.toString(),
            initialCategoryId = bill.categoryId,
            initialType = bill.type,
            initialColor = bill.color,
            initialDate = bill.date,
            initialNote = bill.note,
            categories = billBookData.categories,
            onConfirm = { amount, categoryId, type, color, date, note ->
                viewModel.updateBill(
                    billId = bill.id,
                    amount = amount,
                    categoryId = categoryId,
                    type = type,
                    color = color,
                    date = date,
                    note = note
                )
                showEditBillDialog = null
                scope.launch {
                    snackbarHostState.showSnackbar("账单已更新")
                }
            },
            onDismiss = { showEditBillDialog = null }
        )
    }
    
    // 添加分类对话框
    if (showAddCategoryDialog) {
        CategoryDialog(
            title = "添加分类",
            onConfirm = { name, type ->
                viewModel.addCategory(name, type)
                showAddCategoryDialog = false
                scope.launch {
                    snackbarHostState.showSnackbar("分类已添加")
                }
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }
    
    // 编辑分类对话框
    showEditCategoryDialog?.let { category ->
        CategoryDialog(
            title = "编辑分类",
            initialName = category.name,
            initialType = category.type,
            onConfirm = { name, type ->
                viewModel.updateCategory(
                    categoryId = category.id,
                    name = name,
                    type = type
                )
                showEditCategoryDialog = null
                scope.launch {
                    snackbarHostState.showSnackbar("分类已更新")
                }
            },
            onDismiss = { showEditCategoryDialog = null }
        )
    }
    
    // 筛选对话框
    if (showFilterDialog) {
        BillFilterDialog(
            categories = billBookData.categories,
            selectedCategoryId = filterCategoryId,
            selectedType = selectedBillType,
            selectedColor = filterColor,
            startDate = filterStartDate,
            endDate = filterEndDate,
            minAmount = filterMinAmount?.toString(),
            maxAmount = filterMaxAmount?.toString(),
            onCategorySelected = { viewModel.setFilterCategory(it) },
            onTypeSelected = { viewModel.setSelectedBillType(it) },
            onColorSelected = { viewModel.setFilterColor(it) },
            onStartDateChanged = { viewModel.setFilterDateRange(it, filterEndDate) },
            onEndDateChanged = { viewModel.setFilterDateRange(filterStartDate, it) },
            onMinAmountChanged = { 
                val min = it?.toDoubleOrNull()
                viewModel.setFilterAmountRange(min, filterMaxAmount)
            },
            onMaxAmountChanged = { 
                val max = it?.toDoubleOrNull()
                viewModel.setFilterAmountRange(filterMinAmount, max)
            },
            onClear = { viewModel.clearFilters() },
            onDismiss = { showFilterDialog = false }
        )
    }
    
    // 删除账单确认对话框
    showDeleteBillDialog?.let { bill ->
        ConfirmDialog(
            title = "删除账单",
            message = "确定要删除这条账单吗？",
            onConfirm = {
                viewModel.deleteBill(bill.id)
                showDeleteBillDialog = null
                scope.launch {
                    snackbarHostState.showSnackbar("账单已删除")
                }
            },
            onDismiss = { showDeleteBillDialog = null }
        )
    }
    
    // 删除分类确认对话框
    showDeleteCategoryDialog?.let { category ->
        ConfirmDialog(
            title = "删除分类",
            message = "确定要删除分类\"${category.name}\"吗？删除后该分类下的所有账单也将被删除。",
            onConfirm = {
                viewModel.deleteCategory(category.id)
                showDeleteCategoryDialog = null
                scope.launch {
                    snackbarHostState.showSnackbar("分类已删除")
                }
            },
            onDismiss = { showDeleteCategoryDialog = null }
        )
    }
    
    // 分类管理界面（使用全屏对话框）
    if (showCategoryManagement) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCategoryManagement = false },
            sheetState = sheetState
        ) {
            CategoryManagementScreen(
                viewModel = viewModel,
                onBack = { showCategoryManagement = false }
            )
        }
    }
}
