import SwiftUI

struct BillScreen: View {
    @EnvironmentObject private var viewModel: BillViewModel

    @State private var showAddBill = false
    @State private var editingBill: Bill?
    @State private var deletingBill: Bill?

    @State private var showCategoryManagement = false
    @State private var showFilter = false

    @State private var messageAlert: MessageAlert?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if viewModel.hasActiveFilters {
                    filterStatusCard
                }

                typeFilter

                summaryCards

                Text("账单列表 (\(viewModel.currentBills.count))")
                    .font(.headline.weight(.semibold))
                    .padding(.vertical, 4)

                billsList
            }
            .padding(16)
        }
        .navigationTitle("记账簿")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showFilter = true
                } label: {
                    Image(systemName: "line.3.horizontal.decrease.circle")
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showCategoryManagement = true
                } label: {
                    Image(systemName: "square.grid.2x2")
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    showAddBill = true
                } label: {
                    Image(systemName: "plus")
                }
            }
        }
        .sheet(isPresented: $showAddBill) {
            BillEditorView(mode: .add, categories: viewModel.billBookData.categories) { amount, categoryId, type, color, date, note in
                viewModel.addBill(amount: amount, categoryId: categoryId, type: type, color: color, date: date, note: note)
                messageAlert = MessageAlert(title: "已添加", message: "账单已添加。")
            }
        }
        .sheet(item: $editingBill) { bill in
            BillEditorView(mode: .edit(bill), categories: viewModel.billBookData.categories) { amount, categoryId, type, color, date, note in
                viewModel.updateBill(
                    billId: bill.id,
                    amount: amount,
                    categoryId: categoryId,
                    type: type,
                    color: color,
                    date: date,
                    note: note
                )
                messageAlert = MessageAlert(title: "已更新", message: "账单已更新。")
            }
        }
        .sheet(isPresented: $showCategoryManagement) {
            CategoryManagementScreen()
        }
        .sheet(isPresented: $showFilter) {
            BillFilterView(
                categories: viewModel.billBookData.categories,
                selectedCategoryId: viewModel.filterCategoryId,
                selectedType: viewModel.selectedBillType,
                selectedColor: viewModel.filterColor,
                startDate: viewModel.filterStartDate,
                endDate: viewModel.filterEndDate,
                minAmount: viewModel.filterMinAmount,
                maxAmount: viewModel.filterMaxAmount,
                onApply: { categoryId, type, color, startDate, endDate, minAmount, maxAmount in
                    viewModel.setFilterCategory(categoryId)
                    viewModel.setSelectedBillType(type)
                    viewModel.setFilterColor(color)
                    viewModel.setFilterDateRange(startDate: startDate, endDate: endDate)
                    viewModel.setFilterAmountRange(minAmount: minAmount, maxAmount: maxAmount)
                },
                onClear: {
                    viewModel.clearFilters()
                }
            )
        }
        .alert(item: $messageAlert) { alert in
            Alert(title: Text(alert.title), message: Text(alert.message), dismissButton: .default(Text("确定")))
        }
        .alert("删除账单", isPresented: Binding(get: { deletingBill != nil }, set: { if !$0 { deletingBill = nil } })) {
            Button("删除", role: .destructive) {
                if let bill = deletingBill {
                    viewModel.deleteBill(bill.id)
                    messageAlert = MessageAlert(title: "已删除", message: "账单已删除。")
                }
                deletingBill = nil
            }
            Button("取消", role: .cancel) { deletingBill = nil }
        } message: {
            Text("确定要删除这条账单吗？")
        }
    }

    private var typeFilter: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("类型筛选")
                .font(.subheadline)
                .foregroundStyle(.secondary)

            Picker("类型", selection: $viewModel.selectedBillType) {
                Text("全部").tag(nil as BillType?)
                Text("收入").tag(BillType.income as BillType?)
                Text("支出").tag(BillType.expense as BillType?)
            }
            .pickerStyle(.segmented)
        }
    }

    private var summaryCards: some View {
        VStack(spacing: 12) {
            HStack(spacing: 12) {
                SummaryCardView(title: "收入", value: viewModel.currentIncome, accentColor: .green)
                SummaryCardView(title: "支出", value: viewModel.currentExpense, accentColor: .red)
            }

            SummaryCardView(
                title: "结余",
                value: viewModel.currentBalance,
                accentColor: viewModel.currentBalance >= 0 ? .green : .red
            )
        }
    }

    private var billsList: some View {
        let bills = viewModel.currentBills
        guard !bills.isEmpty else {
            return AnyView(
                Text("暂无账单记录")
                    .font(.body)
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity, minHeight: 160, alignment: .center)
            )
        }

        let grouped = Dictionary(grouping: bills, by: { $0.date })
        let dates = grouped.keys.sorted(by: >)

        return AnyView(
            VStack(alignment: .leading, spacing: 12) {
                ForEach(dates, id: \.self) { date in
                    VStack(alignment: .leading, spacing: 8) {
                        dateHeader(date)

                        ForEach((grouped[date] ?? []).sorted(by: { $0.createdAt > $1.createdAt })) { bill in
                            let category = viewModel.billBookData.category(for: bill.categoryId)
                            BillRowView(
                                bill: bill,
                                category: category,
                                onEdit: { editingBill = bill },
                                onDelete: { deletingBill = bill }
                            )
                        }
                    }
                }
            }
        )
    }

    private func dateHeader(_ date: String) -> some View {
        Text(date)
            .font(.subheadline.weight(.bold))
            .foregroundStyle(.secondary)
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(.secondary.opacity(0.08), in: RoundedRectangle(cornerRadius: 10, style: .continuous))
    }

    private var filterStatusCard: some View {
        let texts: [String] = {
            var result: [String] = []
            if let type = viewModel.selectedBillType {
                result.append(type == .income ? "收入" : "支出")
            }
            if let categoryId = viewModel.filterCategoryId, let category = viewModel.billBookData.category(for: categoryId) {
                result.append(category.name)
            }
            if viewModel.filterColor != nil { result.append("颜色") }
            if viewModel.filterStartDate != nil || viewModel.filterEndDate != nil { result.append("日期范围") }
            if viewModel.filterMinAmount != nil || viewModel.filterMaxAmount != nil { result.append("金额范围") }
            return result
        }()

        return VStack(alignment: .leading, spacing: 10) {
            HStack {
                Label("筛选已启用", systemImage: "line.3.horizontal.decrease.circle")
                    .font(.subheadline.weight(.medium))
                    .foregroundStyle(.tint)

                Spacer()

                Button {
                    viewModel.clearFilters()
                } label: {
                    Image(systemName: "xmark.circle.fill")
                        .foregroundStyle(.tint)
                }
                .buttonStyle(.borderless)
            }

            if !texts.isEmpty {
                Text("(\(texts.joined(separator: "，")))")
                    .font(.footnote)
                    .foregroundStyle(.secondary)
            }
        }
        .padding(12)
        .background(.tint.opacity(0.10), in: RoundedRectangle(cornerRadius: 12, style: .continuous))
    }
}

private struct MessageAlert: Identifiable {
    let id = UUID()
    let title: String
    let message: String
}

