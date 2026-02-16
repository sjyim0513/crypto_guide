'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import Header from '@/components/layout/Header';
import NewsDetail from '@/components/news/NewsDetail';
import RelatedNewsSection from '@/components/news/RelatedNewsSection';
import { newsApi, type CryptoNews } from '@/lib/api';

export default function NewsDetailPage() {
  const params = useParams();
  const id = Number(params.id);

  const [news, setNews] = useState<CryptoNews | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchNews = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await newsApi.getById(id);
      setNews(response.data);
    } catch (err) {
      console.error('Failed to fetch news:', err);
      setError('뉴스를 불러올 수 없습니다.');
    } finally { setLoading(false); }
  }, [id]);

  useEffect(() => { if (id) fetchNews(); }, [id, fetchNews]);

  if (loading) {
    return (
      <div className="min-h-screen bg-bg-body">
        <Header />
        <main className="max-w-[860px] mx-auto px-4 py-8">
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <Loader2 className="w-6 h-6 text-[#F26649] animate-spin" />
            <p className="text-tx-muted text-sm">뉴스를 불러오는 중...</p>
          </div>
        </main>
      </div>
    );
  }

  if (error || !news) {
    return (
      <div className="min-h-screen bg-bg-body">
        <Header />
        <main className="max-w-[860px] mx-auto px-4 py-8">
          <div className="flex flex-col items-center justify-center py-20 gap-4">
            <p className="text-tx-muted">{error || '뉴스를 찾을 수 없습니다.'}</p>
            <button onClick={fetchNews} className="btn-primary">다시 시도</button>
          </div>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-bg-body">
      <Header />
      <main className="max-w-[860px] mx-auto px-4 py-5">
        <NewsDetail news={news} />
        <div className="mt-5">
          <RelatedNewsSection excludeId={id} />
        </div>
      </main>
    </div>
  );
}
