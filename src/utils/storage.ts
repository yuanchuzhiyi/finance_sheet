import type { ReportData } from '../types';

const STORAGE_KEY = 'family_financial_report_v2';
const API_BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8000';

export const defaultReport = (): ReportData => defaultData();

export const loadLocal = (): ReportData => {
  const saved = localStorage.getItem(STORAGE_KEY);
  if (saved) {
    try {
      const parsed = JSON.parse(saved);
      return migrateToMultiPeriod(parsed);
    } catch (e) {
      console.error('Failed to parse saved data', e);
    }
  }
  return defaultData();
};

export const saveLocal = (data: ReportData) => {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(data));
};

export const deleteLocal = () => {
  localStorage.removeItem(STORAGE_KEY);
};

export const loadRemote = async (): Promise<ReportData | null> => {
  // Skip remote loading if API URL is not configured
  if (!import.meta.env.VITE_API_URL) {
    console.info('Remote storage disabled: VITE_API_URL not configured');
    return null;
  }
  
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000); // 3 second timeout
    
    const res = await fetch(`${API_BASE}/report`, {
      signal: controller.signal
    });
    clearTimeout(timeoutId);
    
    if (!res.ok) return null;
    const payload = await res.json();
    if (!payload?.data) return null;
    return migrateToMultiPeriod(payload.data);
  } catch (err) {
    console.warn('Fetch remote failed', err);
    return null;
  }
};

export const saveRemote = async (data: ReportData) => {
  // Skip remote saving if API URL is not configured
  if (!import.meta.env.VITE_API_URL) {
    return;
  }
  
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000); // 3 second timeout
    
    await fetch(`${API_BASE}/report`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ data }),
      signal: controller.signal
    });
    clearTimeout(timeoutId);
  } catch (err) {
    console.warn('Save remote failed', err);
  }
};

export const deleteRemote = async () => {
  // Skip remote deletion if API URL is not configured
  if (!import.meta.env.VITE_API_URL) {
    return;
  }
  
  try {
    const controller = new AbortController();
    const timeoutId = setTimeout(() => controller.abort(), 3000); // 3 second timeout
    
    await fetch(`${API_BASE}/report`, { 
      method: 'DELETE',
      signal: controller.signal
    });
    clearTimeout(timeoutId);
  } catch (err) {
    console.warn('Delete remote failed', err);
  }
};

// ---- helpers ----

const defaultData = (): ReportData => ({
  years: ['2025'],
  months: ['2025-01', '2025-02'],
  days: ['2025-01-01'],
  snapshots: [],
  notes: [
    { id: 'note_1', label: '养老保险总缴纳额', value: '200000' },
  ],
  flowGroups: [
    {
      id: 'income',
      type: 'income',
      name: '收入 (Income)',
      items: [
        { id: 'inc_1', name: '工资收入', values: { '2025': 240000, '2025-01': 20000, '2025-02': 20000 }, quantities: { '2025': 1, '2025-01': 1, '2025-02': 1 }, unitPrices: { '2025': 240000, '2025-01': 20000, '2025-02': 20000 } },
        { id: 'inc_2', name: '公积金收入', values: { '2025': 6000, '2025-02': 500 }, quantities: { '2025': 1, '2025-02': 1 }, unitPrices: { '2025': 6000, '2025-02': 500 } },
        { id: 'inc_3', name: '股票投资收入', values: {}, quantities: {}, unitPrices: {} },
        { id: 'inc_4', name: '期货投资收入', values: {}, quantities: {}, unitPrices: {} },
        { id: 'inc_5', name: '黄金投资收入', values: {}, quantities: {}, unitPrices: {} },
        { id: 'inc_6', name: '白银投资收入', values: {}, quantities: {}, unitPrices: {} },
        { id: 'inc_7', name: '铑投资收入', values: {}, quantities: {}, unitPrices: {} },
        { id: 'inc_8', name: '加密货币投资收入', values: {}, quantities: {}, unitPrices: {} },
        { id: 'inc_9', name: '其他投资收入', values: {}, quantities: {}, unitPrices: {} },
      ]
    },
    {
      id: 'expense',
      type: 'expense',
      name: '支出 (Expenses)',
      items: [
        { id: 'exp_1', name: '房贷/房租', values: { '2025': 60000, '2025-01': 5000, '2025-02': 5000 }, quantities: { '2025': 1, '2025-01': 1, '2025-02': 1 }, unitPrices: { '2025': 60000, '2025-01': 5000, '2025-02': 5000 } },
        { id: 'exp_2', name: '餐饮美食', values: { '2025': 36000, '2025-01': 3000, '2025-02': 2800 }, quantities: { '2025': 1, '2025-01': 1, '2025-02': 1 }, unitPrices: { '2025': 36000, '2025-01': 3000, '2025-02': 2800 } },
        { id: 'exp_3', name: '交通出行', values: { '2025': 9600, '2025-02': 800 }, quantities: { '2025': 1, '2025-02': 1 }, unitPrices: { '2025': 9600, '2025-02': 800 } },
      ]
    }
  ],
  balanceGroups: [
    {
      id: 'asset',
      type: 'asset',
      name: '资产 (Assets)',
      items: [
        { id: 'ast_cash', name: '现金', values: { '2025-01-01': 20000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 20000 } },
        { id: 'ast_recv', name: '应收借款', values: { '2025-01-01': 50000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 50000 } },
        { id: 'ast_int', name: '累计利息', values: { '2025-01-01': 500 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 500 } },
        { id: 'ast_gold', name: '黄金资产', values: { '2025-01-01': 50000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 50000 } },
        { id: 'ast_stock', name: '股票资产', values: { '2025-01-01': 110000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 110000 } },
        { id: 'ast_rhod', name: '铑实物', values: { '2025-01-01': 30000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 30000 } },
        { id: 'ast_crypto', name: '加密货币', values: { '2025-01-01': 38000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 38000 } },
        { id: 'ast_fund', name: '公积金', values: { '2025-01-01': 78000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 78000 } },
        { id: 'ast_savegold', name: '积存金', values: { '2025-01-01': 15000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 15000 } },
        { id: 'ast_futures', name: '期货资产', values: { '2025-01-01': 24000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 24000 } },
      ]
    },
    {
      id: 'liability',
      type: 'liability',
      name: '负债 (Liabilities)',
      items: [
        { id: 'lia_card', name: '信用卡未还', values: { '2025-01-01': 2500 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 2500 } },
        { id: 'lia_debt', name: '欠款', values: { '2025-01-01': 50000 }, quantities: { '2025-01-01': 1 }, unitPrices: { '2025-01-01': 50000 } },
      ]
    }
  ],
  // 兼容旧字段，默认合并
  groups: []
});

// 将旧版数据迁移为包含年/月/日三种周期
const migrateToMultiPeriod = (raw: any): ReportData => {
  if (raw && Array.isArray(raw.days) && Array.isArray(raw.months) && Array.isArray(raw.years)) {
    return {
      ...raw,
      notes: raw.notes ?? [],
      snapshots: raw.snapshots ?? [],
    };
  }

  const daysRaw: string[] = Array.isArray(raw?.days) ? raw.days : [];
  const monthsRaw: string[] = Array.isArray(raw?.months) ? raw.months : [];
  const yearsRaw: string[] = Array.isArray(raw?.years) ? raw.years : [];

  const derivedMonths = monthsRaw.length ? monthsRaw : daysRaw.map(d => d.slice(0, 7)).filter(Boolean);
  const derivedYears = yearsRaw.length
    ? yearsRaw
    : (monthsRaw.length ? monthsRaw.map(m => m.slice(0, 4)) : daysRaw.map(d => d.slice(0, 4))).filter(Boolean);

  const safeYears = (derivedYears.length ? derivedYears : ['2025']).map(String);
  const safeMonths = (derivedMonths.length ? derivedMonths : ['2025-01']).map(String);
  const safeDays = (daysRaw.length
    ? daysRaw
    : (monthsRaw.length ? monthsRaw.map(m => `${m}-01`) : (yearsRaw.length ? yearsRaw.map(y => `${y}-01-01`) : ['2025-01-01']))
  ).map(String);

  const ensurePeriods = (values: Record<string, number | undefined>) => {
    const acc: Record<string, number> = {};
    Object.entries(values || {}).forEach(([k, v]) => {
      if (!k) return;
      acc[k] = v || 0;
    });
    [...safeYears, ...safeMonths, ...safeDays].forEach(p => {
      if (!(p in acc)) acc[p] = 0;
    });
    return acc;
  };

  const migrateItems = (items: any[]): any[] =>
    (items || []).map(it => {
      const values = ensurePeriods(it.values || {});
      const baseQty = it.quantities || {};
      const basePrice = it.unitPrices || {};
      const quantities: Record<string, number> = { ...baseQty };
      const unitPrices: Record<string, number> = { ...basePrice };
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
        children: it.children ? migrateItems(it.children) : undefined,
      };
    });

  const migratedGroups = (raw?.groups || []).map((g: any) => ({
    id: g.id,
    type: g.type,
    name: g.name,
    items: migrateItems(g.items || []),
  }));

  const flowGroups = migratedGroups.filter((g: any) => g.type === 'income' || g.type === 'expense');
  const balanceGroups = migratedGroups.filter((g: any) => g.type === 'asset' || g.type === 'liability');

  return {
    years: Array.from(new Set(safeYears)),
    months: Array.from(new Set(safeMonths)),
    days: Array.from(new Set(safeDays)),
    notes: raw?.notes ?? [],
    snapshots: raw?.snapshots ?? [],
    flowGroups,
    balanceGroups,
    groups: migratedGroups,
  };
};
