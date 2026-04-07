package com.familyfinance.sheet.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.familyfinance.sheet.data.model.BillType
import com.familyfinance.sheet.ui.components.CategoryCard
import com.familyfinance.sheet.ui.components.CategoryDialog
import com.familyfinance.sheet.ui.components.ConfirmDialog
import com.familyfinance.sheet.viewmodel.BillViewModel
import kotlinx.coroutines.launch

/**
 * 分类管理界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: BillViewModel = viewModel(),
    onBack: () -> Unit = {}
) {
    val billBookData by viewModel.billBookData.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // 对话框状态
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showEditCategoryDialog by remember { mutableStateOf<com.familyfinance.sheet.data.model.BillCategory?>(null) }
    var showDeleteCategoryDialog by remember { mutableStateOf<com.familyfinance.sheet.data.model.BillCategory?>(null) }
    
    val incomeCategories = billBookData.getCategoriesByType(BillType.INCOME)
    val expenseCategories = billBookData.getCategoriesByType(BillType.EXPENSE)
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "分类管理",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddCategoryDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加分类",
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
            // 收入分类
            item {
                Text(
                    text = "收入分类 (${incomeCategories.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (incomeCategories.isEmpty()) {
                item {
                    Text(
                        text = "暂无收入分类",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(incomeCategories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = { showEditCategoryDialog = category },
                        onDelete = { showDeleteCategoryDialog = category },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // 支出分类
            item {
                Text(
                    text = "支出分类 (${expenseCategories.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
            
            if (expenseCategories.isEmpty()) {
                item {
                    Text(
                        text = "暂无支出分类",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(expenseCategories, key = { it.id }) { category ->
                    CategoryCard(
                        category = category,
                        onEdit = { showEditCategoryDialog = category },
                        onDelete = { showDeleteCategoryDialog = category },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
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
}

