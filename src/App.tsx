import { useEffect, useMemo, useState, useRef } from 'react';
import type { JSX } from 'react';
import { Plus, RotateCcw, Trash2, CalendarPlus, Calculator, ListPlus, Info, FileDown, Eye, X, Edit2 } from 'lucide-react';
import type { ReportData, CategoryGroup, CategoryType, ReportItem } from './types';
import { defaultReport, loadLocal, loadRemote, saveLocal, saveRemote, deleteLocal, deleteRemote } from './utils/storage';
import html2pdf from 'html2pdf.js';
import { FinancialReportTemplate } from './components/FinancialReportTemplate';

const GROUP_META: Record<CategoryType, { label: string; accent: string; bg: string }> = {
  income: { label: '收入 (Income)', accent: 'text-emerald-600', bg: 'bg-emerald-50' },
  expense: { label: '支出 (Expenses)', accent: 'text-rose-600', bg: 'bg-rose-50' },
  asset: { label: '资产 (Assets)', accent: 'text-indigo-600', bg: 'bg-indigo-50' },
  liability: { label: '负债 (Liabilities)', accent: 'text-amber-600', bg: 'bg-amber-50' },
};

const dateFormat = /^\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$/;

function App() {
  const [data, setData] = useState<ReportData | null>(null);
  const [selectedYear, setSelectedYear] = useState<string>('');
  const [selectedMonth, setSelectedMonth] = useState<string>('');
  const [selectedDay, setSelectedDay] = useState<string>('');
  const [viewMode, setViewMode] = useState<'year' | 'month' | 'day'>('day');
  const [showNotes, setShowNotes] = useState(false);
  const [showPreview, setShowPreview] = useState(false);
  const reportRef = useRef<HTMLDivElement>(null);
  const pdfTemplateRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const local = loadLocal();
    const mergedLocal = {
      ...local,
      flowGroups: local.flowGroups ?? local.groups?.filter(g => g.type === 'income' || g.type === 'expense') ?? [],
      balanceGroups: local.balanceGroups ?? local.groups?.filter(g => g.type === 'asset' || g.type === 'liability') ?? [],
      groups: local.groups ?? [...(local.flowGroups || []), ...(local.balanceGroups || [])],
    };
    setData(mergedLocal);
    setSelectedYear(mergedLocal.years[mergedLocal.years.length - 1] ?? '');
    setSelectedMonth(mergedLocal.months[mergedLocal.months.length - 1] ?? '');
    setSelectedDay(mergedLocal.days[mergedLocal.days.length - 1] ?? '');

    loadRemote().then(remote => {
      if (!remote) return;
      const mergedRemote = {
        ...remote,
        flowGroups: remote.flowGroups ?? remote.groups?.filter(g => g.type === 'income' || g.type === 'expense') ?? [],
        balanceGroups: remote.balanceGroups ?? remote.groups?.filter(g => g.type === 'asset' || g.type === 'liability') ?? [],
        groups: remote.groups ?? [...(remote.flowGroups || []), ...(remote.balanceGroups || [])],
      };
      setData(mergedRemote);
      setSelectedYear(mergedRemote.years[mergedRemote.years.length - 1] ?? '');
      setSelectedMonth(mergedRemote.months[mergedRemote.months.length - 1] ?? '');
      setSelectedDay(mergedRemote.days[mergedRemote.days.length - 1] ?? '');
      saveLocal(mergedRemote);
    });
  }, []);

  useEffect(() => {
    if (data) {
      saveLocal(data);
      void saveRemote(data);
    }
  }, [data]);

  const updateGroup = (groupId: string, updater: (group: CategoryGroup) => CategoryGroup) => {
    if (!data) return;
    const apply = (list: CategoryGroup[]) => list.map(group => (group.id === groupId ? updater(group) : group));
    setData({
      ...data,
      flowGroups: apply(data.flowGroups || []),
      balanceGroups: apply(data.balanceGroups || []),
      groups: apply(data.groups || []),
    });
  };

  const addDay = () => {
    if (!data) return;
    const day = prompt('输入新日期 (格式: 2025-12-01)', '');
    if (!day) return;
    if (!dateFormat.test(day)) {
      alert('请输入正确的日期格式，如 2025-12-01');
      return;
    }
    if (data.days.includes(day)) {
      alert('该日期已存在');
      return;
    }

    const appendDay = (items: ReportItem[]): ReportItem[] =>
      items.map(item => ({
        ...item,
        values: { ...item.values, [day]: 0 },
        children: item.children ? appendDay(item.children) : undefined,
      }));

    setData({
      ...data,
      days: [...data.days, day],
      groups: data.groups.map(group => ({
        ...group,
        items: appendDay(group.items),
      })),
    });
    setSelectedDay(day);
    setViewMode('day');
  };

  const addItem = (groupId: string) => {
    if (!data) return;
    const name = prompt('新科目名称：');
    if (!name) return;
    const newId = `${groupId}_${Date.now()}`;

    updateGroup(groupId, group => ({
      ...group,
      items: [
        ...group.items,
        {
          id: newId,
          name,
          values: Object.fromEntries(
            [...data.years, ...data.months, ...data.days].map(p => [p, 0])
          ),
        },
      ],
    }));
  };

  const addYear = () => {
    if (!data) return;
    const year = prompt('输入新年份 (格式: 2025)', '');
    if (!year) return;
    if (!/^\d{4}$/.test(year)) {
      alert('请输入正确的年份格式，如 2025');
      return;
    }
    if (data.years.includes(year)) {
      alert('该年份已存在');
      return;
    }
    const appendYear = (items: ReportItem[]): ReportItem[] =>
      items.map(item => ({
        ...item,
        values: { ...item.values, [year]: 0 },
        children: item.children ? appendYear(item.children) : undefined,
      }));
    setData({
      ...data,
      years: [...data.years, year],
      groups: data.groups.map(g => ({ ...g, items: appendYear(g.items) })),
    });
    setSelectedYear(year);
    setViewMode('year');
  };

  const addMonth = () => {
    if (!data) return;
    const month = prompt('输入新月份 (格式: 2025-03)', '');
    if (!month) return;
    if (!/^\d{4}-(0[1-9]|1[0-2])$/.test(month)) {
      alert('请输入正确的月份格式，如 2025-03');
      return;
    }
    if (data.months.includes(month)) {
      alert('该月份已存在');
      return;
    }
    const appendMonth = (items: ReportItem[]): ReportItem[] =>
      items.map(item => ({
        ...item,
        values: { ...item.values, [month]: 0 },
        children: item.children ? appendMonth(item.children) : undefined,
      }));
    setData({
      ...data,
      months: [...data.months, month],
      groups: data.groups.map(g => ({ ...g, items: appendMonth(g.items) })),
    });
    setSelectedMonth(month);
    setViewMode('month');
  };

  const addSubItem = (groupId: string, parentId: string) => {
    if (!data) return;
    const name = prompt('新子科目名称：');
    if (!name) return;
    const newId = `${parentId}_${Date.now()}`;

    const appendChild = (items: ReportItem[]): ReportItem[] =>
      items.map(item => {
        if (item.id === parentId) {
          return {
            ...item,
            children: [
              ...(item.children ?? []),
              {
                id: newId,
                name,
                values: Object.fromEntries([...data.years, ...data.months, ...data.days].map(p => [p, 0])),
              },
            ],
          };
        }
        return { ...item, children: item.children ? appendChild(item.children) : item.children };
      });

    updateGroup(groupId, group => ({
      ...group,
      items: appendChild(group.items),
    }));
  };

  const renameItem = (groupId: string, itemId: string) => {
    if (!data) return;
    const group = data.groups.find(g => g.id === groupId);
    const findItem = (items: ReportItem[]): ReportItem | undefined => {
      for (const i of items) {
        if (i.id === itemId) return i;
        const child = i.children && findItem(i.children);
        if (child) return child;
      }
      return undefined;
    };
    const item = group ? findItem(group.items) : undefined;
    if (!item) return;
    const newName = prompt('修改科目名称：', item.name);
    if (!newName || newName === item.name) return;

    const rename = (items: ReportItem[]): ReportItem[] =>
      items.map(it => {
        if (it.id === itemId) return { ...it, name: newName };
        return { ...it, children: it.children ? rename(it.children) : it.children };
      });

    updateGroup(groupId, g => ({
      ...g,
      items: rename(g.items),
    }));
  };

  const deleteItem = (groupId: string, itemId: string) => {
    console.log('deleteItem called', { groupId, itemId });
    if (!data) {
      console.log('No data');
      return;
    }
    
    // Search in all group arrays
    const allGroups = [...(data.groups || []), ...(data.flowGroups || []), ...(data.balanceGroups || [])];
    console.log('All groups:', allGroups.map(g => ({ id: g.id, type: g.type, itemCount: g.items.length })));
    
    const group = allGroups.find(g => g.id === groupId);
    console.log('Found group:', group ? { id: group.id, type: group.type, itemCount: group.items.length } : null);
    
    const findItem = (items: ReportItem[]): ReportItem | undefined => {
      for (const i of items) {
        if (i.id === itemId) return i;
        const child = i.children && findItem(i.children);
        if (child) return child;
      }
      return undefined;
    };
    
    const item = group ? findItem(group.items) : undefined;
    console.log('Found item:', item ? { id: item.id, name: item.name } : null);
    
    if (!item) {
      console.log('Item not found, available items:', group?.items.map(it => ({ id: it.id, name: it.name })));
      alert('无法找到该科目，请刷新页面后重试');
      return;
    }
    
    const confirmResult = confirm(`确定删除科目"${item.name}"吗？`);
    console.log('Confirm result:', confirmResult);
    if (!confirmResult) return;

    const remove = (items: ReportItem[]): ReportItem[] =>
      items
        .filter(it => it.id !== itemId)
        .map(it => ({ ...it, children: it.children ? remove(it.children) : it.children }));

    updateGroup(groupId, g => ({
      ...g,
      items: remove(g.items),
    }));
    console.log('Item deleted');
  };

  const updateValue = (groupId: string, itemId: string, period: string, value: number) => {
    if (!data) return;
    const update = (items: ReportItem[]): ReportItem[] =>
      items.map(it => {
        if (it.id === itemId) {
          const qty = it.quantities?.[period] ?? 1;
          const unitPrices = { ...(it.unitPrices || {}) };
          unitPrices[period] = Number.isNaN(value) ? 0 : value;
          const values = { ...it.values, [period]: (Number.isNaN(value) ? 0 : value) * qty };
          return { ...it, values, unitPrices };
        }
        return { ...it, children: it.children ? update(it.children) : it.children };
      });

    updateGroup(groupId, g => ({
      ...g,
      items: update(g.items),
    }));
  };

  const updateQuantity = (groupId: string, itemId: string, period: string, quantity: number) => {
    if (!data) return;
    const update = (items: ReportItem[]): ReportItem[] =>
      items.map(it => {
        if (it.id === itemId) {
          const unitPrice = it.unitPrices?.[period] ?? it.values[period] ?? 0;
          const q = Number.isNaN(quantity) ? 0 : quantity;
          const quantities = { ...(it.quantities || {}), [period]: q };
          const values = { ...it.values, [period]: unitPrice * q };
          return { ...it, quantities, values };
        }
        return { ...it, children: it.children ? update(it.children) : it.children };
      });

    updateGroup(groupId, g => ({
      ...g,
      items: update(g.items),
    }));
  };

  const resetData = () => {
    if (!confirm('重置后将回到默认模板，确定吗？')) return;
    const freshLocal = defaultReport();
    const mergedLocal = {
      ...freshLocal,
      flowGroups: freshLocal.flowGroups ?? freshLocal.groups?.filter(g => g.type === 'income' || g.type === 'expense') ?? [],
      balanceGroups: freshLocal.balanceGroups ?? freshLocal.groups?.filter(g => g.type === 'asset' || g.type === 'liability') ?? [],
      groups: freshLocal.groups ?? [...(freshLocal.flowGroups || []), ...(freshLocal.balanceGroups || [])],
    };
    setData(mergedLocal);
    setSelectedYear(mergedLocal.years[mergedLocal.years.length - 1] ?? '');
    setSelectedMonth(mergedLocal.months[mergedLocal.months.length - 1] ?? '');
    setSelectedDay(mergedLocal.days[mergedLocal.days.length - 1] ?? '');
    saveLocal(mergedLocal);
    void saveRemote(mergedLocal);
  };

  const saveReportManually = () => {
    if (!data) return;
    saveLocal(data);
    void saveRemote(data);
    alert('报表已保存到本地与服务器');
  };

  const deleteReport = () => {
    if (!confirm('删除服务器与本地的报表数据？此操作不可恢复。')) return;
    deleteLocal();
    void deleteRemote();
    const fresh = defaultReport();
    const merged = {
      ...fresh,
      flowGroups: fresh.flowGroups ?? [],
      balanceGroups: fresh.balanceGroups ?? [],
      groups: fresh.groups ?? [],
    };
    setData(merged);
    setSelectedYear(merged.years[merged.years.length - 1] ?? '');
    setSelectedMonth(merged.months[merged.months.length - 1] ?? '');
    setSelectedDay(merged.days[merged.days.length - 1] ?? '');
    saveLocal(merged);
  };

  const exportToPDF = async () => {
    if (!pdfTemplateRef.current) return;
    
    try {
      const reportTitle = viewMode === 'day' ? '资产负债表' : '利润表';
      const period = viewMode === 'year' ? selectedYear : viewMode === 'month' ? selectedMonth : selectedDay;
      const filename = `${reportTitle}_${period}.pdf`;

      const opt = {
        margin: 0,
        filename,
        image: { type: 'jpeg' as const, quality: 0.98 },
        html2canvas: { scale: 2, useCORS: true },
        jsPDF: { unit: 'mm', format: 'a4', orientation: 'portrait' as const }
      };

      await html2pdf().set(opt).from(pdfTemplateRef.current).save();
      setShowPreview(false);
      alert('PDF导出成功！');
    } catch (error) {
      console.error('PDF导出失败:', error);
      alert('PDF导出失败，请重试');
    }
  };

  const summary = useMemo(() => {
    if (!data) return { income: 0, expense: 0, asset: 0, liability: 0 };
    const period = viewMode === 'year' ? selectedYear : viewMode === 'month' ? selectedMonth : selectedDay;
    if (!period) return { income: 0, expense: 0, asset: 0, liability: 0 };

    const targetGroups =
      viewMode === 'day'
        ? (data.balanceGroups?.length ? data.balanceGroups : data.groups.filter(g => g.type === 'asset' || g.type === 'liability'))
        : (data.flowGroups?.length ? data.flowGroups : data.groups.filter(g => g.type === 'income' || g.type === 'expense'));

    const sumItems = (items: ReportItem[]): number =>
      items.reduce<number>((sum, item) => {
        const childrenTotal: number = item.children ? sumItems(item.children) : 0;
        const selfValue: number = item.values[period] || 0;
        return sum + (item.children ? childrenTotal : selfValue);
      }, 0);

    return targetGroups.reduce(
      (acc, group) => {
        const total = sumItems(group.items);
        acc[group.type] += total;
        return acc;
      },
      { income: 0, expense: 0, asset: 0, liability: 0 }
    );
  }, [data, selectedYear, selectedMonth, selectedDay, viewMode]);

  if (!data) return null;

  const cashflow = summary.income - summary.expense;
  const netWorth = summary.asset - summary.liability;
  const currentPeriods =
    viewMode === 'year'
      ? (selectedYear ? [selectedYear] : data.years.slice(0, 1))
      : viewMode === 'month'
        ? (selectedMonth ? [selectedMonth] : data.months.slice(0, 1))
        : (selectedDay ? [selectedDay] : data.days.slice(0, 1));
  const displayGroups =
    viewMode === 'day'
      ? (data.balanceGroups?.length ? data.balanceGroups : data.groups.filter(g => g.type === 'asset' || g.type === 'liability'))
      : (data.flowGroups?.length ? data.flowGroups : data.groups.filter(g => g.type === 'income' || g.type === 'expense'));

  const getPeriodValue = (item: ReportItem, period: string): number => {
    if (item.children?.length) {
      return item.children.reduce((s, c) => s + getPeriodValue(c, period), 0);
    }
    return item.values[period] || 0;
  };

  return (
    <div className="min-h-screen bg-slate-50">
        <div className="max-w-6xl mx-auto px-4 py-8 md:py-10">
        <header className="flex flex-col md:flex-row md:items-center md:justify-between gap-4 mb-8">
          <div>
            <p className="text-sm uppercase tracking-[0.2em] text-slate-400 mb-1">FAMILY FINANCE</p>
            <h1 className="text-3xl md:text-4xl font-semibold text-slate-900">
              {viewMode === 'day' ? '资产负债表' : '利润表'}
            </h1>
            <p className="text-slate-500 mt-2">
              {viewMode === 'day' 
                ? '按日查看资产负债情况，支持科目分层、附注与日期快照。'
                : '按年/月查看收支利润情况，支持科目分层、附注与日期快照。'
              }
            </p>
          </div>
          <div className="flex flex-wrap gap-2">
            <button
              onClick={addDay}
              className="inline-flex items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 py-2 text-slate-700 shadow-sm hover:border-slate-300"
            >
              <CalendarPlus size={18} />
              <span>新增日期</span>
            </button>
            <button
              onClick={addMonth}
              className="inline-flex items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 py-2 text-slate-700 shadow-sm hover:border-slate-300"
            >
              <CalendarPlus size={18} />
              <span>新增月份</span>
            </button>
            <button
              onClick={addYear}
              className="inline-flex items-center gap-2 rounded-lg bg-white border border-slate-200 px-4 py-2 text-slate-700 shadow-sm hover:border-slate-300"
            >
              <CalendarPlus size={18} />
              <span>新增年份</span>
            </button>
            <button
              onClick={resetData}
              className="inline-flex items-center gap-2 rounded-lg bg-white border border-red-200 px-4 py-2 text-red-600 shadow-sm hover:border-red-300"
            >
              <RotateCcw size={18} />
              <span>重置模板</span>
            </button>
            <button
              onClick={saveReportManually}
              className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-white shadow-sm hover:bg-indigo-500"
            >
              <span>保存报表</span>
            </button>
            <button
              onClick={() => setShowPreview(true)}
              className="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-4 py-2 text-white shadow-sm hover:bg-blue-500"
            >
              <Eye size={18} />
              <span>预览PDF</span>
            </button>
            <button
              onClick={deleteReport}
              className="inline-flex items-center gap-2 rounded-lg bg-white border border-red-200 px-4 py-2 text-red-600 shadow-sm hover:border-red-300"
            >
              <span>删除报表</span>
            </button>
          </div>
        </header>

        <section className="grid gap-4 md:grid-cols-2 xl:grid-cols-4 mb-8">
          {viewMode === 'day' ? (
            <>
              <div className="rounded-2xl bg-white border border-slate-200 p-4 shadow-sm">
                <p className="text-sm text-slate-500 mb-1">资产总额</p>
                <div className="flex items-end justify-between">
                  <h3 className="text-2xl font-semibold text-indigo-600">¥ {summary.asset.toLocaleString()}</h3>
                  <Calculator className="text-indigo-500" size={18} />
                </div>
              </div>
              <div className="rounded-2xl bg-white border border-slate-200 p-4 shadow-sm">
                <p className="text-sm text-slate-500 mb-1">负债总额</p>
                <h3 className="text-2xl font-semibold text-amber-600">¥ {summary.liability.toLocaleString()}</h3>
              </div>
              <div className="rounded-2xl bg-white border border-slate-200 p-4 shadow-sm md:col-span-2">
                <p className="text-sm text-slate-500 mb-1">所有者权益（净资产）</p>
                <h3 className={`text-2xl font-semibold ${netWorth >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                  ¥ {netWorth.toLocaleString()}
                </h3>
              </div>
            </>
          ) : (
            <>
              <div className="rounded-2xl bg-white border border-slate-200 p-4 shadow-sm">
                <p className="text-sm text-slate-500 mb-1">本期收入</p>
                <div className="flex items-end justify-between">
                  <h3 className="text-2xl font-semibold text-emerald-600">¥ {summary.income.toLocaleString()}</h3>
                  <Calculator className="text-emerald-500" size={18} />
                </div>
              </div>
              <div className="rounded-2xl bg-white border border-slate-200 p-4 shadow-sm">
                <p className="text-sm text-slate-500 mb-1">本期支出</p>
                <h3 className="text-2xl font-semibold text-rose-600">¥ {summary.expense.toLocaleString()}</h3>
              </div>
              <div className="rounded-2xl bg-white border border-slate-200 p-4 shadow-sm md:col-span-2">
                <p className="text-sm text-slate-500 mb-1">本期利润（结余）</p>
                <h3 className={`text-2xl font-semibold ${cashflow >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>
                  ¥ {cashflow.toLocaleString()}
                </h3>
              </div>
            </>
          )}
        </section>

        <section className="mb-6">
          <div className="flex flex-col gap-3 md:flex-row md:items-center md:gap-4">
            <div className="flex items-center gap-2">
              <label className="text-sm text-slate-500">查看维度：</label>
              <div className="flex gap-2">
                <button
                  onClick={() => setViewMode('year')}
                  className={`px-3 py-2 rounded-lg border ${viewMode === 'year' ? 'border-indigo-500 bg-indigo-50 text-indigo-700' : 'border-slate-200 bg-white text-slate-700'}`}
                >
                  按年
                </button>
                <button
                  onClick={() => setViewMode('month')}
                  className={`px-3 py-2 rounded-lg border ${viewMode === 'month' ? 'border-indigo-500 bg-indigo-50 text-indigo-700' : 'border-slate-200 bg-white text-slate-700'}`}
                >
                  按月
                </button>
                <button
                  onClick={() => setViewMode('day')}
                  className={`px-3 py-2 rounded-lg border ${viewMode === 'day' ? 'border-indigo-500 bg-indigo-50 text-indigo-700' : 'border-slate-200 bg-white text-slate-700'}`}
                >
                  按日
                </button>
              </div>
            </div>
            {viewMode === 'year' && (
              <select
                value={selectedYear}
                onChange={(e) => setSelectedYear(e.target.value)}
                className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-700 shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              >
                {data.years.map(year => (
                  <option key={year} value={year}>{year}</option>
                ))}
              </select>
            )}
            {viewMode === 'month' && (
              <select
                value={selectedMonth}
                onChange={(e) => setSelectedMonth(e.target.value)}
                className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-700 shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              >
                {data.months.map(month => (
                  <option key={month} value={month}>{month}</option>
                ))}
              </select>
            )}
            {viewMode === 'day' && (
              <select
                value={selectedDay}
                onChange={(e) => setSelectedDay(e.target.value)}
                className="rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-700 shadow-sm focus:border-indigo-500 focus:ring-2 focus:ring-indigo-100"
              >
                {data.days.map(day => (
                  <option key={day} value={day}>{day}</option>
                ))}
              </select>
            )}
            <button
              onClick={() => setShowNotes(!showNotes)}
              className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-slate-700 shadow-sm hover:border-slate-300"
            >
              {showNotes ? '折叠备注' : '展开备注'}
            </button>
          </div>
        </section>

        {/* 快照功能已移除 */}

        <div ref={reportRef} className="space-y-6">
          {displayGroups.map(group => {
            const meta = GROUP_META[group.type];
            return (
              <section key={group.id} className="rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
                <div className={`flex items-center justify-between px-4 py-3 ${meta.bg}`}>
                  <div>
                    <p className={`text-sm font-semibold ${meta.accent}`}>{meta.label}</p>
                    <p className="text-xs text-slate-500">双击科目名称可重命名</p>
                  </div>
                  <button
                    onClick={() => addItem(group.id)}
                    className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-white px-3 py-1 text-sm text-slate-700 hover:border-slate-300 shadow-xs"
                  >
                    <Plus size={14} />
                    新增科目
                  </button>
                </div>

                <div className="overflow-x-auto">
                  <table className="w-full min-w-[720px] border-collapse">
                    <thead>
                      <tr className="bg-slate-50 text-left text-sm text-slate-600">
                        <th className="px-4 py-3 w-56">科目</th>
                        {showNotes && <th className="px-3 py-3 min-w-[200px]">备注</th>}
                        {currentPeriods.map(day => (
                          <th key={day} className="px-3 py-3 text-right min-w-[120px]">{day}</th>
                        ))}
                        <th className="px-4 py-3 text-right">操作</th>
                      </tr>
                    </thead>
                    <tbody>
                      {group.items.map(item => {
                        const sumChildren = (items: ReportItem[], period: string): number =>
                          items.reduce<number>((s, child) => {
                            if (child.children?.length) return s + sumChildren(child.children, period);
                            return s + (child.values[period] || 0);
                          }, 0);

                        const renderRow = (it: ReportItem, depth = 0): JSX.Element[] => {
                          const rows: JSX.Element[] = [];
                          rows.push(
                            <tr key={`${it.id}-${depth}`} className="border-t border-slate-100 hover:bg-slate-50/60">
                              <td
                                className="px-4 py-3 font-medium text-slate-800 cursor-pointer"
                                onDoubleClick={() => renameItem(group.id, it.id)}
                                title="双击重命名"
                              >
                                <span className="inline-flex items-center gap-2" style={{ paddingLeft: depth * 14 }}>
                                  {depth > 0 && <span className="w-1.5 h-1.5 rounded-full bg-slate-300" />}
                                  {it.name}
                                </span>
                              </td>
                              {showNotes && (
                                <td className="px-3 py-2">
                                  <input
                                    type="text"
                                    className="w-full rounded-lg border border-transparent bg-slate-50 px-3 py-2 text-slate-800 focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
                                    value={it.note || ''}
                                    onChange={(e) => {
                                      const update = (items: ReportItem[]): ReportItem[] =>
                                        items.map(item => {
                                          if (item.id === it.id) {
                                            return { ...item, note: e.target.value };
                                          }
                                          return { ...item, children: item.children ? update(item.children) : item.children };
                                        });
                                      updateGroup(group.id, g => ({
                                        ...g,
                                        items: update(g.items),
                                      }));
                                    }}
                                    placeholder="备注信息..."
                                  />
                                </td>
                              )}
                              {currentPeriods.map(period => {
                                const displayValue = it.children?.length
                                  ? sumChildren(it.children, period)
                                  : (it.values[period] || 0);
                                return (
                                  <td key={period} className="px-3 py-2">
                                    <input
                                      type="number"
                                      inputMode="decimal"
                                      className="w-full rounded-lg border border-transparent bg-slate-50 px-3 py-2 text-right text-slate-800 focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
                                      value={displayValue === 0 ? '' : displayValue}
                                      onChange={(e) => {
                                        const newVal = Number(e.target.value);
                                        // interpret为总价手动改动，同时更新单价
                                        const qty = it.quantities?.[period] ?? 1;
                                        const update = (items: ReportItem[]): ReportItem[] =>
                                          items.map(item => {
                                            if (item.id === it.id) {
                                              const unitPrices = { ...(item.unitPrices || {}) };
                                              unitPrices[period] = Number.isNaN(newVal) ? 0 : newVal / (qty || 1);
                                              const values = { ...item.values, [period]: Number.isNaN(newVal) ? 0 : newVal };
                                              return { ...item, values, unitPrices };
                                            }
                                            return { ...item, children: item.children ? update(item.children) : item.children };
                                          });
                                        updateGroup(group.id, g => ({
                                          ...g,
                                          items: update(g.items),
                                        }));
                                      }}
                                      disabled={!!it.children?.length}
                                      title={it.children?.length ? '含子科目的汇总行，金额由子科目累加' : ''}
                                    />
                                  </td>
                                );
                              })}
                              <td className="px-4 py-2 text-right">
                                <div className="flex items-center justify-end gap-2">
                                  <button
                                    onClick={(e) => {
                                      e.preventDefault();
                                      e.stopPropagation();
                                      addSubItem(group.id, it.id);
                                    }}
                                    className="text-slate-400 hover:text-indigo-600 transition-colors"
                                    title="新增子科目"
                                  >
                                    <ListPlus size={16} />
                                  </button>
                                  <button
                                    onClick={(e) => {
                                      e.preventDefault();
                                      e.stopPropagation();
                                      deleteItem(group.id, it.id);
                                    }}
                                    className="text-slate-400 hover:text-rose-600 transition-colors"
                                    title="删除科目"
                                  >
                                    <Trash2 size={16} />
                                  </button>
                                </div>
                              </td>
                            </tr>
                          );
                          if (it.children?.length) {
                            it.children.forEach(child => rows.push(...renderRow(child, depth + 1)));
                          }
                          return rows;
                        };
                        return renderRow(item);
                      })}
                    </tbody>
                    <tfoot>
                      <tr className="bg-slate-50 border-t border-slate-100 text-sm font-semibold text-slate-700">
                        <td className="px-4 py-3">小计</td>
                        {currentPeriods.map(period => {
                          const sumItems = (items: ReportItem[]): number =>
                            items.reduce((sum, item) => {
                              if (item.children?.length) return sum + sumItems(item.children);
                              return sum + (item.values[period] || 0);
                            }, 0);
                          const total = sumItems(group.items);
                          return (
                            <td key={period} className="px-3 py-3 text-right">
                              ¥ {total.toLocaleString()}
                            </td>
                          );
                        })}
                        <td />
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </section>
            );
          })}
        </div>

        <section className="mt-8 rounded-2xl border border-slate-200 bg-white shadow-sm overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 bg-slate-50">
            <div className="flex items-center gap-2 text-slate-700">
              <Info size={16} />
              <span className="font-semibold">其他信息 / 附注（不计入汇总）</span>
            </div>
            <button
              onClick={() => {
                if (!data) return;
                setData({
                  ...data,
                  notes: [...data.notes, { id: `note_${Date.now()}`, label: '', value: '' }],
                });
              }}
              className="inline-flex items-center gap-1 rounded-full border border-slate-200 bg-white px-3 py-1 text-sm text-slate-700 hover:border-slate-300 shadow-xs"
            >
              <Plus size={14} />
              新增附注
            </button>
          </div>
          <div className="divide-y divide-slate-100">
            {data.notes.length === 0 ? (
              <p className="px-4 py-3 text-slate-400 text-sm">暂无附注，可用来记录养老保险缴纳额等信息。</p>
            ) : (
              data.notes.map(note => (
                <div key={note.id} className="flex items-center gap-4 px-4 py-3 hover:bg-slate-50/60 group">
                  <div className="flex-1 grid grid-cols-2 gap-4">
                    <input
                      type="text"
                      className="w-full rounded-lg border border-transparent bg-slate-50 px-3 py-2 text-slate-800 font-medium focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
                      value={note.label}
                      onChange={(e) => {
                        setData({
                          ...data,
                          notes: data.notes.map(n => 
                            n.id === note.id ? { ...n, label: e.target.value } : n
                          ),
                        });
                      }}
                      placeholder="标题..."
                    />
                    <input
                      type="text"
                      className="w-full rounded-lg border border-transparent bg-slate-50 px-3 py-2 text-slate-800 focus:border-indigo-400 focus:bg-white focus:ring-2 focus:ring-indigo-100"
                      value={note.value}
                      onChange={(e) => {
                        setData({
                          ...data,
                          notes: data.notes.map(n => 
                            n.id === note.id ? { ...n, value: e.target.value } : n
                          ),
                        });
                      }}
                      placeholder="内容..."
                    />
                  </div>
                  <button
                    onClick={() => {
                      if (!confirm(`确定删除附注"${note.label || '此项'}"吗？`)) return;
                      setData({
                        ...data,
                        notes: data.notes.filter(n => n.id !== note.id),
                      });
                    }}
                    className="text-slate-400 hover:text-rose-600 transition-colors opacity-0 group-hover:opacity-100"
                    title="删除附注"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              ))
            )}
          </div>
        </section>

        <footer className="mt-10 text-center text-sm text-slate-400">
          数据仅保存在本地浏览器 · 支持自定义年份、科目层级与附注 · 双击科目可重命名
        </footer>
      </div>

      {/* PDF预览模态框 */}
        {showPreview && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4">
            <div className="relative w-full max-w-4xl h-[90vh] bg-white rounded-lg shadow-2xl flex flex-col">
              {/* 模态框头部 */}
              <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200">
                <h2 className="text-xl font-semibold text-slate-900">PDF预览</h2>
                <div className="flex items-center gap-2">
                  <button
                    onClick={exportToPDF}
                    className="inline-flex items-center gap-2 rounded-lg bg-emerald-600 px-4 py-2 text-white shadow-sm hover:bg-emerald-500"
                  >
                    <FileDown size={18} />
                    <span>导出PDF</span>
                  </button>
                  <button
                    onClick={() => setShowPreview(false)}
                    className="inline-flex items-center justify-center w-10 h-10 rounded-lg bg-slate-100 text-slate-600 hover:bg-slate-200 transition-colors"
                    title="关闭"
                  >
                    <X size={20} />
                  </button>
                </div>
              </div>

              {/* 预览内容区域 */}
              <div className="flex-1 overflow-auto p-6 bg-slate-100">
                <div className="mx-auto" style={{ width: '210mm' }}>
                  <div ref={pdfTemplateRef}>
                    <FinancialReportTemplate
                      data={data}
                      viewMode={viewMode}
                      selectedPeriod={
                        viewMode === 'year' ? selectedYear 
                        : viewMode === 'month' ? selectedMonth 
                        : selectedDay
                      }
                      displayGroups={displayGroups}
                      summary={summary}
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
    </div>
  );
}

export default App;
