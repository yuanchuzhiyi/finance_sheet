import AsyncStorage from '@react-native-async-storage/async-storage';
import type { ReportData, CategoryGroup, ReportItem } from './types';

const STORAGE_KEY = 'family_financial_report_v2_mobile';

export const defaultData = (): ReportData => ({
  years: ['2025'],
  months: ['2025-01', '2025-02'],
  days: ['2025-01-01'],
  notes: [],
  flowGroups: [],
  balanceGroups: [],
  groups: [],
  snapshots: [],
});

export const loadLocal = async (): Promise<ReportData> => {
  try {
    const saved = await AsyncStorage.getItem(STORAGE_KEY);
    if (saved) return migrate(JSON.parse(saved));
  } catch {}
  return migrate(defaultData());
};

export const saveLocal = async (data: ReportData) => {
  await AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(data));
};

export const deleteLocal = async () => {
  await AsyncStorage.removeItem(STORAGE_KEY);
};

const ensurePeriods = (values: Record<string, number>, periods: string[]) => {
  const acc: Record<string, number> = { ...values };
  periods.forEach(p => {
    if (!(p in acc)) acc[p] = 0;
  });
  return acc;
};

const migrateItems = (items: any[], periods: string[]): ReportItem[] =>
  (items || []).map((it: any) => {
    const values = ensurePeriods(it.values || {}, periods);
    const quantities: Record<string, number> = { ...(it.quantities || {}) };
    const unitPrices: Record<string, number> = { ...(it.unitPrices || {}) };
    Object.keys(values).forEach(p => {
      if (!(p in quantities)) quantities[p] = 1;
      if (!(p in unitPrices)) unitPrices[p] = values[p];
    });
    return {
      id: it.id,
      name: it.name,
      values,
      quantities,
      unitPrices,
      children: it.children ? migrateItems(it.children, periods) : undefined,
    };
  });

const migrateGroups = (groups: any[], periods: string[]): CategoryGroup[] =>
  (groups || []).map(g => ({
    id: g.id,
    type: g.type,
    name: g.name,
    items: migrateItems(g.items || [], periods),
  }));

export const migrate = (raw: any): ReportData => {
  const years: string[] = raw?.years?.length ? raw.years : ['2025'];
  const months: string[] = raw?.months?.length ? raw.months : [years[0] + '-01'];
  const days: string[] = raw?.days?.length ? raw.days : [`${years[0]}-01-01`];
  const periods = [...years, ...months, ...days];
  const groups = migrateGroups(raw?.groups || [], periods);
  const flowGroups = raw?.flowGroups?.length
    ? migrateGroups(raw.flowGroups, periods)
    : groups.filter(g => g.type === 'income' || g.type === 'expense');
  const balanceGroups = raw?.balanceGroups?.length
    ? migrateGroups(raw.balanceGroups, periods)
    : groups.filter(g => g.type === 'asset' || g.type === 'liability');

  return {
    years,
    months,
    days,
    notes: raw?.notes ?? [],
    snapshots: raw?.snapshots ?? [],
    groups,
    flowGroups,
    balanceGroups,
  };
};
