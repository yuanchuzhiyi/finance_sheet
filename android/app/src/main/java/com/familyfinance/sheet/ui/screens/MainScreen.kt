package com.familyfinance.sheet.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyfinance.sheet.R
import com.familyfinance.sheet.data.model.ReportItem
import com.familyfinance.sheet.data.model.ViewMode
import com.familyfinance.sheet.ui.components.CategoryColors
import com.familyfinance.sheet.ui.components.CategoryGroupSection
import com.familyfinance.sheet.ui.components.ConfirmDialog
import com.familyfinance.sheet.ui.components.InputDialog
import com.familyfinance.sheet.ui.components.NotesSection
import com.familyfinance.sheet.ui.components.PeriodDropdown
import com.familyfinance.sheet.ui.components.SummaryCard
import com.familyfinance.sheet.ui.components.ViewModeSelector
import com.familyfinance.sheet.util.PdfGenerator
import com.familyfinance.sheet.viewmodel.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 主页面
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val reportData by viewModel.reportData.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedDay by viewModel.selectedDay.collectAsState()
    val currentPeriod by viewModel.currentPeriod.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val displayGroups by viewModel.displayGroups.collectAsState()
    val showNotes by viewModel.showNotes.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // PDF 生成器
    val pdfGenerator = remember { PdfGenerator(context) }
    
    // 对话框状态
    var showAddDayDialog by remember { mutableStateOf(false) }
    var showAddMonthDialog by remember { mutableStateOf(false) }
    var showAddYearDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    // 科目操作对话框状态
    var showAddItemDialog by remember { mutableStateOf<String?>(null) }
    var showAddSubItemDialog by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showRenameDialog by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var showDeleteItemDialog by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    
    // PDF 导出函数
    fun exportPdf(openAfterExport: Boolean = false, share: Boolean = false) {
        scope.launch {
            val file = withContext(Dispatchers.IO) {
                pdfGenerator.generatePdf(
                    reportData = reportData,
                    viewMode = viewMode,
                    period = currentPeriod,
                    summary = summary
                )
            }
            
            if (file != null) {
                if (share) {
                    val shareIntent = pdfGenerator.sharePdf(file)
                    context.startActivity(Intent.createChooser(shareIntent, "分享 PDF"))
                } else if (openAfterExport) {
                    try {
                        val openIntent = pdfGenerator.openPdf(file)
                        context.startActivity(openIntent)
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("PDF 已保存: ${file.name}")
                    }
                } else {
                    snackbarHostState.showSnackbar("PDF 已导出: ${file.name}")
                }
            } else {
                snackbarHostState.showSnackbar("PDF 导出失败")
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "FAMILY FINANCE",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = if (viewMode == ViewMode.DAY) 
                                stringResource(R.string.balance_sheet) 
                            else 
                                stringResource(R.string.income_statement),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 描述文字
            item {
                Text(
                    text = if (viewMode == ViewMode.DAY)
                        stringResource(R.string.balance_sheet_desc)
                    else
                        stringResource(R.string.income_statement_desc),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // 操作按钮
            item {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { showAddDayDialog = true }) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.add_date))
                    }
                    OutlinedButton(onClick = { showAddMonthDialog = true }) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.add_month))
                    }
                    OutlinedButton(onClick = { showAddYearDialog = true }) {
                        Icon(Icons.Default.CalendarMonth, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.add_year))
                    }
                    OutlinedButton(onClick = { showResetDialog = true }) {
                        Icon(Icons.Default.Refresh, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.reset_template))
                    }
                    OutlinedButton(onClick = {
                        viewModel.saveReport()
                        scope.launch {
                            snackbarHostState.showSnackbar("报表已保存")
                        }
                    }) {
                        Icon(Icons.Default.Save, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.save_report))
                    }
                    // PDF 预览按钮
                    OutlinedButton(onClick = { exportPdf(openAfterExport = true) }) {
                        Icon(Icons.Default.PictureAsPdf, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.preview_pdf))
                    }
                    // PDF 导出按钮
                    OutlinedButton(onClick = { exportPdf(openAfterExport = false) }) {
                        Icon(Icons.Default.FileDownload, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.export_pdf))
                    }
                    // PDF 分享按钮
                    OutlinedButton(onClick = { exportPdf(share = true) }) {
                        Icon(Icons.Default.Share, null, Modifier.padding(end = 4.dp))
                        Text("分享PDF")
                    }
                    OutlinedButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, null, Modifier.padding(end = 4.dp))
                        Text(stringResource(R.string.delete_report))
                    }
                }
            }
            
            // 汇总卡片
            item {
                if (viewMode == ViewMode.DAY) {
                    // 资产负债表汇总
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = stringResource(R.string.total_asset),
                            value = summary.asset,
                            accentColor = CategoryColors.asset,
                            icon = Icons.Default.Calculate,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = stringResource(R.string.total_liability),
                            value = summary.liability,
                            accentColor = CategoryColors.liability,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryCard(
                        title = stringResource(R.string.net_worth),
                        value = summary.netWorth,
                        accentColor = CategoryColors.getPositiveNegativeColor(summary.netWorth),
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    // 利润表汇总
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SummaryCard(
                            title = stringResource(R.string.period_income),
                            value = summary.income,
                            accentColor = CategoryColors.income,
                            icon = Icons.Default.Calculate,
                            modifier = Modifier.weight(1f)
                        )
                        SummaryCard(
                            title = stringResource(R.string.period_expense),
                            value = summary.expense,
                            accentColor = CategoryColors.expense,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    SummaryCard(
                        title = stringResource(R.string.period_profit),
                        value = summary.cashflow,
                        accentColor = CategoryColors.getPositiveNegativeColor(summary.cashflow),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // 视图模式和周期选择
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "查看维度：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        ViewModeSelector(
                            currentMode = viewMode,
                            onModeChange = { viewModel.setViewMode(it) }
                        )
                    }
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (viewMode) {
                            ViewMode.YEAR -> PeriodDropdown(
                                periods = reportData.years,
                                selectedPeriod = selectedYear,
                                onPeriodChange = { viewModel.selectYear(it) }
                            )
                            ViewMode.MONTH -> PeriodDropdown(
                                periods = reportData.months,
                                selectedPeriod = selectedMonth,
                                onPeriodChange = { viewModel.selectMonth(it) }
                            )
                            ViewMode.DAY -> PeriodDropdown(
                                periods = reportData.days,
                                selectedPeriod = selectedDay,
                                onPeriodChange = { viewModel.selectDay(it) }
                            )
                        }
                        
                        OutlinedButton(onClick = { viewModel.toggleShowNotes() }) {
                            Text(if (showNotes) "折叠备注" else "展开备注")
                        }
                    }
                }
            }
            
            // 分类组
            items(displayGroups, key = { it.id }) { group ->
                CategoryGroupSection(
                    group = group,
                    period = currentPeriod,
                    showNotes = showNotes,
                    onAddItem = { showAddItemDialog = group.id },
                    onValueChange = { itemId, value ->
                        viewModel.updateItemValue(group.id, itemId, currentPeriod, value)
                    },
                    onNoteChange = { itemId, note ->
                        viewModel.updateItemNote(group.id, itemId, note)
                    },
                    onRenameItem = { itemId ->
                        val item = findItem(group.items, itemId)
                        if (item != null) {
                            showRenameDialog = Triple(group.id, itemId, item.name)
                        }
                    },
                    onAddSubItem = { parentId ->
                        showAddSubItemDialog = Pair(group.id, parentId)
                    },
                    onDeleteItem = { itemId ->
                        val item = findItem(group.items, itemId)
                        if (item != null) {
                            showDeleteItemDialog = Triple(group.id, itemId, item.name)
                        }
                    }
                )
            }
            
            // 附注区域
            item {
                NotesSection(
                    notes = reportData.notes,
                    onAddNote = { viewModel.addNote() },
                    onUpdateNote = { noteId, label, value ->
                        viewModel.updateNote(noteId, label, value)
                    },
                    onDeleteNote = { viewModel.deleteNote(it) }
                )
            }
        }
    }
    
    // 对话框
    if (showAddDayDialog) {
        InputDialog(
            title = stringResource(R.string.add_date),
            hint = stringResource(R.string.input_date_hint),
            onConfirm = { day ->
                if (!viewModel.addDay(day)) {
                    scope.launch {
                        snackbarHostState.showSnackbar("日期格式错误或已存在")
                    }
                }
                showAddDayDialog = false
            },
            onDismiss = { showAddDayDialog = false }
        )
    }
    
    if (showAddMonthDialog) {
        InputDialog(
            title = stringResource(R.string.add_month),
            hint = stringResource(R.string.input_month_hint),
            onConfirm = { month ->
                if (!viewModel.addMonth(month)) {
                    scope.launch {
                        snackbarHostState.showSnackbar("月份格式错误或已存在")
                    }
                }
                showAddMonthDialog = false
            },
            onDismiss = { showAddMonthDialog = false }
        )
    }
    
    if (showAddYearDialog) {
        InputDialog(
            title = stringResource(R.string.add_year),
            hint = stringResource(R.string.input_year_hint),
            onConfirm = { year ->
                if (!viewModel.addYear(year)) {
                    scope.launch {
                        snackbarHostState.showSnackbar("年份格式错误或已存在")
                    }
                }
                showAddYearDialog = false
            },
            onDismiss = { showAddYearDialog = false }
        )
    }
    
    if (showResetDialog) {
        ConfirmDialog(
            title = stringResource(R.string.reset_template),
            message = stringResource(R.string.confirm_reset),
            onConfirm = {
                viewModel.resetReport()
                showResetDialog = false
            },
            onDismiss = { showResetDialog = false }
        )
    }
    
    if (showDeleteDialog) {
        ConfirmDialog(
            title = stringResource(R.string.delete_report),
            message = stringResource(R.string.confirm_delete),
            onConfirm = {
                viewModel.deleteReport()
                showDeleteDialog = false
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
    
    // 添加科目对话框
    showAddItemDialog?.let { groupId ->
        InputDialog(
            title = stringResource(R.string.add_item),
            hint = stringResource(R.string.input_item_name),
            onConfirm = { name ->
                viewModel.addItem(groupId, name)
                showAddItemDialog = null
            },
            onDismiss = { showAddItemDialog = null }
        )
    }
    
    // 添加子科目对话框
    showAddSubItemDialog?.let { (groupId, parentId) ->
        InputDialog(
            title = stringResource(R.string.add_sub_item),
            hint = stringResource(R.string.input_item_name),
            onConfirm = { name ->
                viewModel.addSubItem(groupId, parentId, name)
                showAddSubItemDialog = null
            },
            onDismiss = { showAddSubItemDialog = null }
        )
    }
    
    // 重命名对话框
    showRenameDialog?.let { (groupId, itemId, currentName) ->
        InputDialog(
            title = stringResource(R.string.rename_item),
            initialValue = currentName,
            onConfirm = { newName ->
                viewModel.renameItem(groupId, itemId, newName)
                showRenameDialog = null
            },
            onDismiss = { showRenameDialog = null }
        )
    }
    
    // 删除科目确认对话框
    showDeleteItemDialog?.let { (groupId, itemId, itemName) ->
        ConfirmDialog(
            title = "删除科目",
            message = "确定删除科目\"$itemName\"吗？",
            onConfirm = {
                viewModel.deleteItem(groupId, itemId)
                showDeleteItemDialog = null
            },
            onDismiss = { showDeleteItemDialog = null }
        )
    }
}

// 辅助函数：递归查找科目
private fun findItem(items: List<ReportItem>, id: String): ReportItem? {
    for (item in items) {
        if (item.id == id) return item
        item.children?.let { children ->
            findItem(children, id)?.let { return it }
        }
    }
    return null
}
