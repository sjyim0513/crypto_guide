'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import Header from '@/components/layout/Header';
import ThemeHeader from '@/components/themes/ThemeHeader';
import ThemeCryptoList from '@/components/themes/ThemeCryptoList';
import { themeApi, type Theme } from '@/lib/api';

export default function ThemeDetailPage() {
  const params = useParams();
  const themeSlug = params.themeSlug as string;

  const [theme, setTheme] = useState<Theme | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchTheme = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await themeApi.getBySlug(themeSlug);
      setTheme(response.data);
    } catch (err) {
      console.error('Failed to fetch theme:', err);
      setError('테마 정보를 불러올 수 없습니다.');
    } finally { setLoading(false); }
  }, [themeSlug]);

  useEffect(() => { if (themeSlug) fetchTheme(); }, [themeSlug, fetchTheme]);

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-body">
        <Header />
        <div className="max-w-page mx-auto px-4 py-8">
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <Loader2 className="w-6 h-6 text-[#F26649] animate-spin" />
            <p className="text-tx-muted text-sm">테마 정보를 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (error || !theme) {
    return (
      <div className="min-h-screen bg-bg-body">
        <Header />
        <div className="max-w-page mx-auto px-4 py-8">
          <div className="flex flex-col items-center justify-center py-20 gap-4">
            <p className="text-tx-muted">{error || '테마를 찾을 수 없습니다.'}</p>
            <button onClick={fetchTheme} className="btn-primary">다시 시도</button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-bg-body">
      <Header />
      <div className="max-w-page mx-auto px-4 py-5">
        <ThemeHeader theme={theme} />
        <div className="mt-5">
          <ThemeCryptoList themeSlug={themeSlug} />
        </div>
      </div>
    </div>
  );
}
