'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Newspaper, Clock, Loader2 } from 'lucide-react';
import { newsApi, type CryptoNews } from '@/lib/api';
import { timeAgo } from '@/lib/utils';

export default function LatestNews() {
  const [news, setNews] = useState<CryptoNews[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function fetchData() {
      try {
        setLoading(true);
        setError(null);
        const response = await newsApi.getRecent(24);
        if (!cancelled) setNews(response.data.slice(0, 5));
      } catch (err) {
        if (!cancelled) { console.error('Failed to fetch news:', err); setError('뉴스 로드 실패'); }
      } finally { if (!cancelled) setLoading(false); }
    }
    fetchData();
    const interval = setInterval(fetchData, 300000);
    return () => { cancelled = true; clearInterval(interval); };
  }, []);

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="flex items-center gap-1.5">
          <Newspaper className="w-3.5 h-3.5 text-secondary" />
          최신 뉴스
        </h3>
        <Link href="/news" className="text-2xs text-[#F26649] font-semibold hover:underline">
          더보기 →
        </Link>
      </div>

      {loading && news.length === 0 ? (
        <div className="p-6 flex justify-center"><Loader2 className="w-4 h-4 text-[#F26649] animate-spin" /></div>
      ) : error && news.length === 0 ? (
        <div className="p-4 text-center text-tx-muted text-2xs">{error}</div>
      ) : news.length === 0 ? (
        <div className="p-4 text-center text-tx-muted text-2xs">최근 뉴스가 없습니다.</div>
      ) : (
        <div>
          {news.map((item) => (
            <Link
              key={item.id}
              href={`/news/${item.id}`}
              className="block px-3 py-2.5 hover:bg-bg-hover transition-colors border-b border-border-light last:border-b-0"
            >
              <h4 className="text-[12px] font-medium text-tx-primary line-clamp-2 leading-[1.4] mb-1">
                {item.title}
              </h4>
              <div className="flex items-center gap-1.5 text-2xs text-tx-muted">
                <span className="font-medium">{item.source}</span>
                <span>·</span>
                <span className="flex items-center gap-0.5">
                  <Clock className="w-2.5 h-2.5" />
                  {timeAgo(item.publishedAt)}
                </span>
                {item.relatedCryptoSymbols && item.relatedCryptoSymbols.length > 0 && (
                  <>
                    <span className="ml-auto" />
                    {item.relatedCryptoSymbols.slice(0, 2).map((symbol) => (
                      <span key={symbol} className="badge badge-primary text-2xs px-1.5 py-0">{symbol}</span>
                    ))}
                  </>
                )}
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
