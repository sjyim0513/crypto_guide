'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Clock, Sparkles, Loader2 } from 'lucide-react';
import { newsApi, type CryptoNews } from '@/lib/api';
import { timeAgo } from '@/lib/utils';

interface RelatedNewsProps {
  coinId: string;
}

export default function RelatedNews({ coinId }: RelatedNewsProps) {
  const [news, setNews] = useState<CryptoNews[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function fetchNews() {
      try {
        setLoading(true);
        setError(null);
        const response = await newsApi.getByCrypto(coinId, 0, 5);
        if (!cancelled) setNews(response.data.content);
      } catch (err) {
        if (!cancelled) { console.error('Failed to fetch related news:', err); setError('관련 뉴스를 불러올 수 없습니다.'); }
      } finally { if (!cancelled) setLoading(false); }
    }
    fetchNews();
    return () => { cancelled = true; };
  }, [coinId]);

  return (
    <div className="card">
      <div className="card-header">
        <h2>관련 뉴스</h2>
        <Link href={`/news?coin=${coinId}`} className="text-2xs text-[#F26649] font-semibold hover:underline">
          더보기 →
        </Link>
      </div>

      {loading ? (
        <div className="p-6 flex justify-center"><Loader2 className="w-4 h-4 text-[#F26649] animate-spin" /></div>
      ) : error ? (
        <div className="p-4 text-center text-tx-muted text-2xs">{error}</div>
      ) : news.length === 0 ? (
        <div className="p-4 text-center text-tx-muted text-2xs">관련 뉴스가 없습니다.</div>
      ) : (
        <div>
          {news.map((item) => (
            <Link
              key={item.id}
              href={`/news/${item.id}`}
              className="block px-3 py-2.5 hover:bg-bg-hover transition-colors border-b border-border-light last:border-b-0"
            >
              <h3 className="text-[12px] font-medium text-tx-primary line-clamp-2 mb-1">{item.title}</h3>
              {item.summary && (
                <div className="flex items-start gap-1.5 mb-1">
                  <Sparkles className="w-3 h-3 text-[#F26649] flex-shrink-0 mt-0.5" />
                  <p className="text-2xs text-tx-muted line-clamp-1">{item.summary}</p>
                </div>
              )}
              <div className="flex items-center gap-1.5 text-2xs text-tx-muted">
                <span className="font-medium">{item.source}</span>
                <span>·</span>
                <span className="flex items-center gap-0.5">
                  <Clock className="w-2.5 h-2.5" />
                  {timeAgo(item.publishedAt)}
                </span>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
