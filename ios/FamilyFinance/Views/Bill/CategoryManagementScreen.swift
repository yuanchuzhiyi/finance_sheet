import SwiftUI

struct CategoryManagementScreen: View {
    @EnvironmentObject private var viewModel: BillViewModel
    @Environment(\.dismiss) private var dismiss

    @State private var showAdd = false
    @State private var editingCategory: BillCategory?
    @State private var deletingCategory: BillCategory?

    private var incomeCategories: [BillCategory] {
        viewModel.billBookData.categories(of: .income)
    }

    private var expenseCategories: [BillCategory] {
        viewModel.billBookData.categories(of: .expense)
    }

    var body: some View {
        NavigationStack {
            List {
                Section("收入分类 (\(incomeCategories.count))") {
                    if incomeCategories.isEmpty {
                        Text("暂无收入分类")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(incomeCategories) { category in
                            Text(category.name)
                                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                    Button {
                                        editingCategory = category
                                    } label: {
                                        Label("编辑", systemImage: "pencil")
                                    }
                                    .tint(.blue)

                                    Button(role: .destructive) {
                                        deletingCategory = category
                                    } label: {
                                        Label("删除", systemImage: "trash")
                                    }
                                }
                        }
                    }
                }

                Section("支出分类 (\(expenseCategories.count))") {
                    if expenseCategories.isEmpty {
                        Text("暂无支出分类")
                            .foregroundStyle(.secondary)
                    } else {
                        ForEach(expenseCategories) { category in
                            Text(category.name)
                                .swipeActions(edge: .trailing, allowsFullSwipe: false) {
                                    Button {
                                        editingCategory = category
                                    } label: {
                                        Label("编辑", systemImage: "pencil")
                                    }
                                    .tint(.blue)

                                    Button(role: .destructive) {
                                        deletingCategory = category
                                    } label: {
                                        Label("删除", systemImage: "trash")
                                    }
                                }
                        }
                    }
                }
            }
            .navigationTitle("分类管理")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("关闭") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button {
                        showAdd = true
                    } label: {
                        Image(systemName: "plus")
                    }
                }
            }
            .sheet(isPresented: $showAdd) {
                CategoryEditorView(mode: .add) { name, type in
                    viewModel.addCategory(name: name, type: type)
                }
            }
            .sheet(item: $editingCategory) { category in
                CategoryEditorView(mode: .edit(category)) { name, type in
                    viewModel.updateCategory(categoryId: category.id, name: name, type: type)
                }
            }
            .alert("删除分类", isPresented: Binding(get: { deletingCategory != nil }, set: { if !$0 { deletingCategory = nil } })) {
                Button("删除", role: .destructive) {
                    if let category = deletingCategory {
                        viewModel.deleteCategory(category.id)
                    }
                    deletingCategory = nil
                }
                Button("取消", role: .cancel) { deletingCategory = nil }
            } message: {
                if let category = deletingCategory {
                    Text("确定要删除分类“\(category.name)”吗？删除后该分类下的所有账单也将被删除。")
                } else {
                    Text("")
                }
            }
        }
    }
}

