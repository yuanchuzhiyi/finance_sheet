import React from 'react';
import type { CategoryGroup, ReportItem } from '../types';

interface FinancialReportTemplateProps {
  viewMode: 'year' | 'month' | 'day';
  selectedPeriod: string;
  displayGroups: CategoryGroup[];
  summary: {
    income: number;
    expense: number;
    asset: number;
    liability: number;
  };
}

export const FinancialReportTemplate: React.FC<FinancialReportTemplateProps> = ({
  viewMode,
  selectedPeriod,
  displayGroups,
  summary,
}) => {
  const reportTitle = viewMode === 'day' ? '资产负债表' : '利润表';
  const reportSubtitle = viewMode === 'day' 
    ? 'BALANCE SHEET' 
    : 'INCOME STATEMENT';
  
  const cashflow = summary.income - summary.expense;
  const netWorth = summary.asset - summary.liability;

  const sumChildren = (items: ReportItem[], period: string): number =>
    items.reduce<number>((s, child) => {
      if (child.children?.length) return s + sumChildren(child.children, period);
      return s + (child.values[period] || 0);
    }, 0);

  const renderItems = (items: ReportItem[], period: string, depth = 0): React.ReactNode => {
    return items.map((item, index) => {
      const value = item.children?.length
        ? sumChildren(item.children, period)
        : (item.values[period] || 0);
      
      const hasChildren = item.children && item.children.length > 0;
      const isLastItem = index === items.length - 1;
      
      return (
        <React.Fragment key={item.id}>
          <tr style={{ 
            backgroundColor: depth === 0 ? 'white' : depth === 1 ? '#f9fafb' : '#f3f4f6',
            borderLeft: depth > 0 ? `3px solid ${depth === 1 ? '#e5e7eb' : '#d1d5db'}` : 'none'
          }}>
            <td style={{
              padding: '8px 12px',
              borderBottom: '1px solid #e5e7eb',
              fontSize: depth === 0 ? '12px' : '11px',
              paddingLeft: `${12 + depth * 20}px`,
              fontWeight: depth === 0 ? 600 : 400,
              color: depth === 0 ? '#1f2937' : depth === 1 ? '#4b5563' : '#6b7280',
            }}>
              {depth > 0 && (
                <span style={{ 
                  color: '#cbd5e1', 
                  marginRight: '6px',
                  fontSize: '14px'
                }}>
                  {isLastItem ? '└' : '├'}
                </span>
              )}
              <span>
                {item.name}
              </span>
            </td>
            <td style={{
              padding: '8px 12px',
              borderBottom: '1px solid #e5e7eb',
              textAlign: 'right',
              fontSize: depth === 0 ? '12px' : '11px',
              fontWeight: depth === 0 ? 600 : 400,
              color: depth === 0 ? '#1f2937' : '#4b5563'
            }}>
              {value.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
            </td>
          </tr>
          {hasChildren && renderItems(item.children!, period, depth + 1)}
        </React.Fragment>
      );
    });
  };

  return (
    <div style={{
      width: '210mm',
      minHeight: '297mm',
      padding: '20mm',
      backgroundColor: 'white',
      fontFamily: '-apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif',
      color: '#1f2937',
      boxSizing: 'border-box',
    }}>
      {/* 报表头部 */}
      <div style={{ textAlign: 'center', marginBottom: '30px', borderBottom: '2px solid #1f2937', paddingBottom: '20px' }}>
        <h1 style={{ 
          fontSize: '24px', 
          fontWeight: 700, 
          margin: '0 0 8px 0',
          color: '#1f2937'
        }}>
          家庭财务报表
        </h1>
        <h2 style={{ 
          fontSize: '20px', 
          fontWeight: 600, 
          margin: '0 0 8px 0',
          color: '#4b5563'
        }}>
          {reportTitle}
        </h2>
        <p style={{ 
          fontSize: '12px', 
          color: '#6b7280', 
          margin: '0 0 8px 0',
          textTransform: 'uppercase',
          letterSpacing: '1px'
        }}>
          {reportSubtitle}
        </p>
        <p style={{ 
          fontSize: '14px', 
          fontWeight: 600,
          color: '#374151',
          margin: '8px 0 0 0'
        }}>
          报表期间：{selectedPeriod}
        </p>
      </div>

      {/* 报表主体 */}
      <div style={{ marginBottom: '30px' }}>
        {displayGroups.map((group, groupIndex) => {
          const groupTotal = group.items.reduce((sum, item) => {
            if (item.children?.length) return sum + sumChildren(item.children, selectedPeriod);
            return sum + (item.values[selectedPeriod] || 0);
          }, 0);

          const groupLabel = group.type === 'income' ? '收入' 
            : group.type === 'expense' ? '支出'
            : group.type === 'asset' ? '资产'
            : '负债';

          return (
            <div key={group.id} style={{ marginBottom: groupIndex < displayGroups.length - 1 ? '30px' : '0' }}>
              <div style={{
                backgroundColor: '#f3f4f6',
                padding: '10px 12px',
                borderLeft: `4px solid ${
                  group.type === 'income' ? '#10b981' 
                  : group.type === 'expense' ? '#ef4444'
                  : group.type === 'asset' ? '#6366f1'
                  : '#f59e0b'
                }`,
                marginBottom: '8px'
              }}>
                <h3 style={{ 
                  fontSize: '14px', 
                  fontWeight: 700, 
                  margin: 0,
                  color: '#1f2937'
                }}>
                  {groupLabel}
                </h3>
              </div>
              
              <table style={{ 
                width: '100%', 
                borderCollapse: 'collapse',
                border: '1px solid #e5e7eb'
              }}>
                <thead>
                  <tr style={{ backgroundColor: '#f9fafb' }}>
                    <th style={{
                      padding: '10px 12px',
                      borderBottom: '2px solid #d1d5db',
                      textAlign: 'left',
                      fontSize: '12px',
                      fontWeight: 600,
                      color: '#374151'
                    }}>
                      科目名称
                    </th>
                    <th style={{
                      padding: '10px 12px',
                      borderBottom: '2px solid #d1d5db',
                      textAlign: 'right',
                      fontSize: '12px',
                      fontWeight: 600,
                      color: '#374151',
                      width: '150px'
                    }}>
                      金额 (元)
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {renderItems(group.items, selectedPeriod)}
                </tbody>
                <tfoot>
                  <tr style={{ backgroundColor: '#f3f4f6' }}>
                    <td style={{
                      padding: '12px',
                      borderTop: '2px solid #9ca3af',
                      fontSize: '13px',
                      fontWeight: 700,
                      color: '#1f2937'
                    }}>
                      {groupLabel}合计
                    </td>
                    <td style={{
                      padding: '12px',
                      borderTop: '2px solid #9ca3af',
                      textAlign: 'right',
                      fontSize: '13px',
                      fontWeight: 700,
                      color: '#1f2937'
                    }}>
                      {groupTotal.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>
          );
        })}
      </div>

      {/* 汇总信息 */}
      <div style={{
        marginTop: '40px',
        padding: '20px',
        backgroundColor: '#f9fafb',
        border: '2px solid #e5e7eb',
        borderRadius: '8px'
      }}>
        <h3 style={{ 
          fontSize: '16px', 
          fontWeight: 700, 
          marginBottom: '16px',
          color: '#1f2937',
          borderBottom: '2px solid #d1d5db',
          paddingBottom: '8px'
        }}>
          财务汇总
        </h3>
        
        {viewMode === 'day' ? (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <tbody>
              <tr>
                <td style={{ padding: '8px 0', fontSize: '13px', color: '#374151' }}>资产总额：</td>
                <td style={{ padding: '8px 0', textAlign: 'right', fontSize: '13px', fontWeight: 600, color: '#6366f1' }}>
                  ¥ {summary.asset.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
              <tr>
                <td style={{ padding: '8px 0', fontSize: '13px', color: '#374151' }}>负债总额：</td>
                <td style={{ padding: '8px 0', textAlign: 'right', fontSize: '13px', fontWeight: 600, color: '#f59e0b' }}>
                  ¥ {summary.liability.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
              <tr style={{ borderTop: '2px solid #d1d5db' }}>
                <td style={{ padding: '12px 0 0 0', fontSize: '14px', fontWeight: 700, color: '#1f2937' }}>所有者权益（净资产）：</td>
                <td style={{ 
                  padding: '12px 0 0 0', 
                  textAlign: 'right', 
                  fontSize: '14px', 
                  fontWeight: 700,
                  color: netWorth >= 0 ? '#10b981' : '#ef4444'
                }}>
                  ¥ {netWorth.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
            </tbody>
          </table>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <tbody>
              <tr>
                <td style={{ padding: '8px 0', fontSize: '13px', color: '#374151' }}>本期收入：</td>
                <td style={{ padding: '8px 0', textAlign: 'right', fontSize: '13px', fontWeight: 600, color: '#10b981' }}>
                  ¥ {summary.income.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
              <tr>
                <td style={{ padding: '8px 0', fontSize: '13px', color: '#374151' }}>本期支出：</td>
                <td style={{ padding: '8px 0', textAlign: 'right', fontSize: '13px', fontWeight: 600, color: '#ef4444' }}>
                  ¥ {summary.expense.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
              <tr style={{ borderTop: '2px solid #d1d5db' }}>
                <td style={{ padding: '12px 0 0 0', fontSize: '14px', fontWeight: 700, color: '#1f2937' }}>本期利润（结余）：</td>
                <td style={{ 
                  padding: '12px 0 0 0', 
                  textAlign: 'right', 
                  fontSize: '14px', 
                  fontWeight: 700,
                  color: cashflow >= 0 ? '#10b981' : '#ef4444'
                }}>
                  ¥ {cashflow.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </td>
              </tr>
            </tbody>
          </table>
        )}
      </div>

      {/* 页脚 */}
      <div style={{
        marginTop: '40px',
        paddingTop: '20px',
        borderTop: '1px solid #e5e7eb',
        fontSize: '10px',
        color: '#9ca3af',
        textAlign: 'center'
      }}>
        <p style={{ margin: '4px 0' }}>生成时间：{new Date().toLocaleString('zh-CN')}</p>
        <p style={{ margin: '4px 0' }}>家庭财务管理系统 | Family Finance Management System</p>
      </div>
    </div>
  );
};
