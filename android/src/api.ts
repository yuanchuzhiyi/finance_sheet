import type { ReportData } from './types';

const API_BASE = process.env.EXPO_PUBLIC_API_URL ?? 'http://10.0.2.2:8000';

export const fetchReport = async (): Promise<ReportData | null> => {
  try {
    const res = await fetch(`${API_BASE}/report`);
    if (!res.ok) return null;
    const json = await res.json();
    return json.data ?? null;
  } catch (e) {
    console.warn('fetchReport failed', e);
    return null;
  }
};

export const saveReport = async (data: ReportData) => {
  try {
    await fetch(`${API_BASE}/report`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ data }),
    });
  } catch (e) {
    console.warn('saveReport failed', e);
  }
};

export const deleteReport = async () => {
  try {
    await fetch(`${API_BASE}/report`, { method: 'DELETE' });
  } catch (e) {
    console.warn('deleteReport failed', e);
  }
};
