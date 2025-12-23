export type CategoryType = 'income' | 'expense' | 'asset' | 'liability';

export interface ReportItem {
  id: string;
  name: string;
  values: Record<string, number>;
  quantities?: Record<string, number>;
  unitPrices?: Record<string, number>;
  children?: ReportItem[];
}

export interface CategoryGroup {
  id: string;
  type: CategoryType;
  name: string;
  items: ReportItem[];
}

export interface ReportData {
  years: string[];
  months: string[];
  days: string[];
  flowGroups: CategoryGroup[];
  balanceGroups: CategoryGroup[];
  groups: CategoryGroup[];
  notes: { id: string; label: string; value: string }[];
  snapshots?: any[];
}
