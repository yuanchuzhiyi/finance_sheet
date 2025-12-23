import React, { useEffect, useMemo, useState } from 'react';
import { SafeAreaView, View, Text, StyleSheet, ScrollView, Pressable, TextInput, Alert } from 'react-native';
import { Picker } from '@react-native-picker/picker';
import { fetchReport, saveReport, deleteReport } from './src/api';
import { defaultData, loadLocal, saveLocal, deleteLocal, migrate } from './src/storage';
import type { CategoryGroup, ReportData, ReportItem, CategoryType } from './src/types';

const GROUP_META: Record<CategoryType, { color: string }> = {
  income: { color: '#0ea5e9' },
  expense: { color: '#ef4444' },
  asset: { color: '#6366f1' },
  liability: { color: '#f59e0b' },
};

type ViewMode = 'year' | 'month' | 'day';

export default function App() {
  const [data, setData] = useState<ReportData | null>(null);
  const [viewMode, setViewMode] = useState<ViewMode>('day');
  const [selectedYear, setSelectedYear] = useState<string>('');
  const [selectedMonth, setSelectedMonth] = useState<string>('');
  const [selectedDay, setSelectedDay] = useState<string>('');
  const [showExtras, setShowExtras] = useState(false);

  useEffect(() => {
    (async () => {
      const local = await loadLocal();
      applyAndSelect(local);
      const remote = await fetchReport();
      if (remote) {
        const merged = migrate(remote);
        applyAndSelect(merged);
        await saveLocal(merged);
      }
    })();
  }, []);

  const applyAndSelect = (payload: ReportData) => {
    setData(payload);
    setSelectedYear(payload.years.at(-1) ?? '');
    setSelectedMonth(payload.months.at(-1) ?? '');
    setSelectedDay(payload.days.at(-1) ?? '');
  };

  const periods = useMemo(() => {
    if (!data) return [];
    if (viewMode === 'year') return selectedYear ? [selectedYear] : data.years.slice(0, 1);
    if (viewMode === 'month') return selectedMonth ? [selectedMonth] : data.months.slice(0, 1);
    return selectedDay ? [selectedDay] : data.days.slice(0, 1);
  }, [data, viewMode, selectedYear, selectedMonth, selectedDay]);

  const displayGroups = useMemo(() => {
    if (!data) return [];
    return viewMode === 'day'
      ? (data.balanceGroups?.length ? data.balanceGroups : data.groups.filter(g => g.type === 'asset' || g.type === 'liability'))
      : (data.flowGroups?.length ? data.flowGroups : data.groups.filter(g => g.type === 'income' || g.type === 'expense'));
  }, [data, viewMode]);

  const summary = useMemo(() => {
    if (!data || periods.length === 0) return { income: 0, expense: 0, asset: 0, liability: 0 };
    const period = periods[0];
    const sumItems = (items: ReportItem[]): number =>
      items.reduce((sum, item) => {
        const child = item.children ? sumItems(item.children) : 0;
        return sum + (child || item.values[period] || 0);
      }, 0);
    const target = displayGroups;
    return target.reduce(
      (acc, g) => {
        acc[g.type] += sumItems(g.items);
        return acc;
      },
      { income: 0, expense: 0, asset: 0, liability: 0 } as Record<CategoryType, number>
    );
  }, [data, periods, displayGroups]);

  const updateGroup = (groupId: string, updater: (g: CategoryGroup) => CategoryGroup) => {
    if (!data) return;
    const apply = (list: CategoryGroup[]) => list.map(g => (g.id === groupId ? updater(g) : g));
    const next: ReportData = {
      ...data,
      flowGroups: apply(data.flowGroups),
      balanceGroups: apply(data.balanceGroups),
      groups: apply(data.groups),
    };
    setData(next);
    void persist(next);
  };

  const persist = async (payload: ReportData) => {
    await saveLocal(payload);
    void saveReport(payload);
  };

  const updateValue = (groupId: string, itemId: string, period: string, value: number) => {
    if (!data) return;
    const apply = (items: ReportItem[]): ReportItem[] =>
      items.map(it => {
        if (it.id === itemId) {
          const qty = it.quantities?.[period] ?? 1;
          const unitPrices = { ...(it.unitPrices || {}), [period]: Number.isNaN(value) ? 0 : value };
          const values = { ...it.values, [period]: (Number.isNaN(value) ? 0 : value) * (qty || 1) };
          return { ...it, values, unitPrices };
        }
        return { ...it, children: it.children ? apply(it.children) : it.children };
      });
    updateGroup(groupId, g => ({ ...g, items: apply(g.items) }));
  };

  const updateQuantity = (groupId: string, itemId: string, period: string, qty: number) => {
    if (!data) return;
    const apply = (items: ReportItem[]): ReportItem[] =>
      items.map(it => {
        if (it.id === itemId) {
          const q = Number.isNaN(qty) ? 0 : qty;
          const price = it.unitPrices?.[period] ?? it.values[period] ?? 0;
          const quantities = { ...(it.quantities || {}), [period]: q };
          const values = { ...it.values, [period]: price * (q || 0) };
          return { ...it, quantities, values };
        }
        return { ...it, children: it.children ? apply(it.children) : it.children };
      });
    updateGroup(groupId, g => ({ ...g, items: apply(g.items) }));
  };

  const addPeriod = (mode: ViewMode) => {
    if (!data) return;
    let input = '';
    if (mode === 'year') input = prompt('新增年份 (YYYY)', '') || '';
    if (mode === 'month') input = prompt('新增月份 (YYYY-MM)', '') || '';
    if (mode === 'day') input = prompt('新增日期 (YYYY-MM-DD)', '') || '';
    if (!input) return;
    const arr = mode === 'year' ? data.years : mode === 'month' ? data.months : data.days;
    if (arr.includes(input)) return Alert.alert('已存在');
    const append = (items: ReportItem[]): ReportItem[] =>
      items.map(it => ({
        ...it,
        values: { ...it.values, [input]: 0 },
        quantities: { ...(it.quantities || {}), [input]: 1 },
        unitPrices: { ...(it.unitPrices || {}), [input]: 0 },
        children: it.children ? append(it.children) : undefined,
      }));
    const next: ReportData = {
      ...data,
      years: mode === 'year' ? [...data.years, input] : data.years,
      months: mode === 'month' ? [...data.months, input] : data.months,
      days: mode === 'day' ? [...data.days, input] : data.days,
      flowGroups: mode === 'day' ? data.flowGroups : data.flowGroups.map(g => ({ ...g, items: append(g.items) })),
      balanceGroups: mode === 'day' ? data.balanceGroups.map(g => ({ ...g, items: append(g.items) })) : data.balanceGroups,
      groups: data.groups.map(g => ({ ...g, items: append(g.items) })),
    };
    setData(next);
    if (mode === 'year') setSelectedYear(input);
    if (mode === 'month') setSelectedMonth(input);
    if (mode === 'day') setSelectedDay(input);
    void persist(next);
  };

  const addItem = (groupId: string) => {
    if (!data) return;
    const name = prompt('新科目名称', '');
    if (!name) return;
    const periodsAll = [...data.years, ...data.months, ...data.days];
    const newItem: ReportItem = {
      id: `${groupId}_${Date.now()}`,
      name,
      values: Object.fromEntries(periodsAll.map(p => [p, 0])),
      quantities: Object.fromEntries(periodsAll.map(p => [p, 1])),
      unitPrices: Object.fromEntries(periodsAll.map(p => [p, 0])),
    };
    updateGroup(groupId, g => ({ ...g, items: [...g.items, newItem] }));
  };

  const deleteItem = (groupId: string, itemId: string) => {
    if (!data) return;
    updateGroup(groupId, g => ({
      ...g,
      items: g.items.filter(it => it.id !== itemId).map(it => ({
        ...it,
        children: it.children?.filter(c => c.id !== itemId),
      })),
    }));
  };

  const renameItem = (groupId: string, itemId: string) => {
    const name = prompt('重命名', '');
    if (!name) return;
    updateGroup(groupId, g => ({
      ...g,
      items: g.items.map(it => (it.id === itemId ? { ...it, name } : it)),
    }));
  };

  const onSave = () => {
    if (!data) return;
    void saveReport(data);
    Alert.alert('已保存', '数据已同步服务器和本地');
  };

  const onDeleteReport = () => {
    Alert.alert('删除确认', '删除服务器与本地数据？', [
      { text: '取消', style: 'cancel' },
      {
        text: '删除',
        style: 'destructive',
        onPress: async () => {
          await deleteLocal();
          await deleteReport();
          const fresh = migrate(defaultData());
          applyAndSelect(fresh);
          await saveLocal(fresh);
        },
      },
    ]);
  };

  if (!data) return null;

  const cards = [
    { label: '本期收入', value: summary.income, color: '#0ea5e9' },
    { label: '本期支出', value: summary.expense, color: '#ef4444' },
    { label: '本期结余', value: summary.income - summary.expense, color: '#10b981' },
    { label: '净资产', value: summary.asset - summary.liability, color: '#6366f1' },
  ];

  return (
    <SafeAreaView style={styles.safe}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text style={styles.title}>家庭财务报表 · 移动版</Text>
        <View style={styles.cardRow}>
          {cards.map(card => (
            <View key={card.label} style={[styles.card, { borderColor: card.color }]}>
              <Text style={styles.cardLabel}>{card.label}</Text>
              <Text style={[styles.cardValue, { color: card.color }]}>¥ {card.value.toLocaleString()}</Text>
            </View>
          ))}
        </View>

        <View style={styles.toolbar}>
          <Text style={styles.label}>查看维度</Text>
          <View style={styles.modeRow}>
            {(['year', 'month', 'day'] as ViewMode[]).map(m => (
              <Pressable key={m} onPress={() => setViewMode(m)} style={[styles.modeBtn, viewMode === m && styles.modeBtnActive]}>
                <Text style={viewMode === m ? styles.modeTxtActive : styles.modeTxt}>
                  {m === 'year' ? '按年' : m === 'month' ? '按月' : '按日'}
                </Text>
              </Pressable>
            ))}
          </View>
          <Picker
            selectedValue={viewMode === 'year' ? selectedYear : viewMode === 'month' ? selectedMonth : selectedDay}
            style={styles.picker}
            onValueChange={(v) => {
              if (viewMode === 'year') setSelectedYear(v);
              else if (viewMode === 'month') setSelectedMonth(v);
              else setSelectedDay(v);
            }}
          >
            {(viewMode === 'year' ? data.years : viewMode === 'month' ? data.months : data.days).map(p => (
              <Picker.Item key={p} label={p} value={p} />
            ))}
          </Picker>
          <View style={styles.actionRow}>
            <Pressable style={styles.smallBtn} onPress={() => addPeriod(viewMode)}>
              <Text style={styles.smallBtnTxt}>新增{viewMode === 'year' ? '年' : viewMode === 'month' ? '月' : '日'}</Text>
            </Pressable>
            <Pressable style={styles.smallBtn} onPress={() => setShowExtras(!showExtras)}>
              <Text style={styles.smallBtnTxt}>{showExtras ? '折叠数量单价' : '展开数量单价'}</Text>
            </Pressable>
            <Pressable style={[styles.smallBtn, { backgroundColor: '#2563eb' }]} onPress={onSave}>
              <Text style={[styles.smallBtnTxt, { color: '#fff' }]}>保存报表</Text>
            </Pressable>
            <Pressable style={[styles.smallBtn, { borderColor: '#ef4444' }]} onPress={onDeleteReport}>
              <Text style={[styles.smallBtnTxt, { color: '#ef4444' }]}>删除报表</Text>
            </Pressable>
          </View>
        </View>

        {displayGroups.map(group => (
          <View key={group.id} style={styles.groupBox}>
            <View style={[styles.groupHeader, { borderLeftColor: GROUP_META[group.type].color }]}>
              <Text style={[styles.groupTitle, { color: GROUP_META[group.type].color }]}>{group.name}</Text>
              <Pressable style={styles.smallBtn} onPress={() => addItem(group.id)}>
                <Text style={styles.smallBtnTxt}>新增科目</Text>
              </Pressable>
            </View>
            <View style={styles.tableHeader}>
              <Text style={[styles.th, { flex: 1.2 }]}>科目</Text>
              {showExtras && <Text style={[styles.th, { width: 70 }]}>数量</Text>}
              {showExtras && <Text style={[styles.th, { width: 90 }]}>单价</Text>}
              {periods.map(p => (
                <Text key={p} style={[styles.th, { width: 120, textAlign: 'right' }]}>{p}</Text>
              ))}
              <Text style={[styles.th, { width: 60, textAlign: 'right' }]}>操作</Text>
            </View>
            {group.items.map(item => (
              <View key={item.id} style={styles.row}>
                <Pressable style={[styles.cell, { flex: 1.2 }]} onLongPress={() => renameItem(group.id, item.id)}>
                  <Text style={styles.cellText}>{item.name}</Text>
                </Pressable>
                {showExtras && (
                  <View style={[styles.cell, { width: 70 }]}>
                    <TextInput
                      style={styles.input}
                      keyboardType="numeric"
                      value={(item.quantities?.[periods[0]] || 0) === 0 ? '' : String(item.quantities?.[periods[0]] ?? 1)}
                      onChangeText={(t) => updateQuantity(group.id, item.id, periods[0], Number(t))}
                    />
                  </View>
                )}
                {showExtras && (
                  <View style={[styles.cell, { width: 90 }]}>
                    <TextInput
                      style={styles.input}
                      keyboardType="numeric"
                      value={(item.unitPrices?.[periods[0]] || 0) === 0 ? '' : String(item.unitPrices?.[periods[0]] ?? 0)}
                      onChangeText={(t) => updateValue(group.id, item.id, periods[0], Number(t))}
                    />
                  </View>
                )}
                {periods.map(p => (
                  <View key={p} style={[styles.cell, { width: 120 }]}>
                    <TextInput
                      style={styles.input}
                      keyboardType="numeric"
                      value={(item.values[p] || 0) === 0 ? '' : String(item.values[p] ?? 0)}
                      onChangeText={(t) => updateValue(group.id, item.id, p, Number(t))}
                    />
                  </View>
                ))}
                <Pressable style={[styles.cell, { width: 60 }]} onPress={() => deleteItem(group.id, item.id)}>
                  <Text style={[styles.cellText, { color: '#ef4444', textAlign: 'right' }]}>删除</Text>
                </Pressable>
              </View>
            ))}
          </View>
        ))}
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safe: { flex: 1, backgroundColor: '#f8fafc' },
  container: { padding: 16, gap: 12 },
  title: { fontSize: 20, fontWeight: '700', color: '#0f172a' },
  cardRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  card: { flexBasis: '48%', borderWidth: 1, borderRadius: 12, padding: 12, backgroundColor: '#fff' },
  cardLabel: { color: '#64748b', fontSize: 13 },
  cardValue: { fontSize: 18, fontWeight: '700', marginTop: 4 },
  toolbar: { backgroundColor: '#fff', borderRadius: 12, padding: 12, gap: 8, borderWidth: 1, borderColor: '#e2e8f0' },
  label: { color: '#475569', fontSize: 13 },
  modeRow: { flexDirection: 'row', gap: 8 },
  modeBtn: { paddingHorizontal: 10, paddingVertical: 8, borderRadius: 8, borderWidth: 1, borderColor: '#cbd5e1' },
  modeBtnActive: { borderColor: '#2563eb', backgroundColor: '#e0ecff' },
  modeTxt: { color: '#475569' },
  modeTxtActive: { color: '#1d4ed8', fontWeight: '600' },
  picker: { backgroundColor: '#fff', borderWidth: 1, borderColor: '#e2e8f0' },
  actionRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8, marginTop: 4 },
  smallBtn: { paddingHorizontal: 10, paddingVertical: 8, borderRadius: 8, borderWidth: 1, borderColor: '#cbd5e1' },
  smallBtnTxt: { color: '#475569', fontSize: 13 },
  groupBox: { backgroundColor: '#fff', borderRadius: 12, borderWidth: 1, borderColor: '#e2e8f0', marginTop: 10 },
  groupHeader: { flexDirection: 'row', alignItems: 'center', justifyContent: 'space-between', padding: 12, borderLeftWidth: 4 },
  groupTitle: { fontSize: 16, fontWeight: '700' },
  tableHeader: { flexDirection: 'row', alignItems: 'center', backgroundColor: '#f8fafc', paddingHorizontal: 12, paddingVertical: 8 },
  th: { color: '#475569', fontSize: 12 },
  row: { flexDirection: 'row', alignItems: 'center', paddingHorizontal: 12, paddingVertical: 6, borderTopWidth: 1, borderTopColor: '#f1f5f9' },
  cell: { paddingVertical: 4 },
  cellText: { color: '#0f172a', fontSize: 13 },
  input: { backgroundColor: '#f8fafc', borderRadius: 8, paddingVertical: 6, paddingHorizontal: 8, borderWidth: 1, borderColor: '#e2e8f0', textAlign: 'right' },
});
