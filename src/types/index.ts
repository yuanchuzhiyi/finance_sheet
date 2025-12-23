export type CategoryType = 'income' | 'expense' | 'asset' | 'liability';

export interface ReportItem {
  id: string;
  name: string;
  // 按日期存储金额，key 例如 '2025-01-01'
  values: Record<string, number>;
  // 数量，按周期
  quantities?: Record<string, number>;
  // 单价，按周期
  unitPrices?: Record<string, number>;
  // 备注
  note?: string;
  // 子科目，支持分层汇总
  children?: ReportItem[];
}

export interface CategoryGroup {
  id: string;
  type: CategoryType;
  name: string;
  items: ReportItem[];
}

export interface SnapshotItem {
  id: string;
  name: string;
  value: number;
  children?: SnapshotItem[];
}

export interface SnapshotGroup {
  id: string;
  type: CategoryType;
  name: string;
  items: SnapshotItem[];
  total: number;
}

export interface Snapshot {
  id: string;
  date: string; // YYYY-MM-DD
  note?: string;
  groups: SnapshotGroup[];
  totals: Record<CategoryType, number>;
  netWorth: number;
  cashflow: number;
}

export interface ReportData {
  // 年/月/日列表
  years: string[];
  months: string[];
  days: string[];
  // 综合分组（兼容旧逻辑）
  groups: CategoryGroup[];
  // 按年/月统计的收支变动（income/expense）
  flowGroups: CategoryGroup[];
  // 按日统计的资产状况（asset/liability）
  balanceGroups: CategoryGroup[];
  // 其他信息（不参与汇总）
  notes: { id: string; label: string; value: string }[];
  // 日期快照
  snapshots: Snapshot[];
}
