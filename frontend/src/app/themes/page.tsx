'use client';

import { useState, useEffect } from 'react';
import { Loader2 } from 'lucide-react';
import Header from '@/components/layout/Header';
import ThemeGrid from '@/components/themes/ThemeGrid';
import { themeApi, type Theme } from '@/lib/api';

export default function ThemesPage() {
  const [themes, setThemes] = useState<Theme[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function fetchThemes() {
      try {
        setLoading(true);
        setError(null);
        const response = await themeApi.getAllWithCount();
        if (!cancelled) setThemes(response.data);
      } catch (err) {
        if (!cancelled) { console.error('Failed to fetch themes:', err); setError('테마 데이터를 불러올 수 없습니다.'); }
      } finally { if (!cancelled) setLoading(false); }
    }
    fetchThemes();
    return () => { cancelled = true; };
  }, []);

  return (
    <div className="min-h-screen bg-bg-body">
      <Header />
      <div className="max-w-page mx-auto px-4 py-5">
        <div className="card mb-5">
          <div className="card-header"><h2>테마별 분류</h2></div>
          <div className="card-body">
            <p className="text-tx-muted text-xs">암호화폐를 테마/카테고리별로 분류하여 확인하세요</p>
          </div>
        </div>

        {loading ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3">
            <Loader2 className="w-6 h-6 text-[#F26649] animate-spin" />
            <p className="text-tx-muted text-xs">테마를 불러오는 중...</p>
          </div>
        ) : error ? (
          <div className="text-center py-16">
            <p className="text-tx-muted mb-3 text-sm">{error}</p>
            <button onClick={() => window.location.reload()} className="btn-primary text-xs">다시 시도</button>
          </div>
        ) : (
          <ThemeGrid themes={themes} />
        )}
      </div>
    </div>
  );
}
