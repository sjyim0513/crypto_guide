'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { Clock, Sparkles, Loader2 } from 'lucide-react';
import { newsApi, type CryptoNews } from '@/lib/api';
import { timeAgo } from '@/lib/utils';

interface RelatedNewsSectionProps {
  excludeId: number;
}

export default function RelatedNewsSection({ excludeId }: RelatedNewsSectionProps) {
  const [news, setNews] = useState<CryptoNews[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function fetchRelated() {
      try {
        setLoading(true);
        const response = await newsApi.getRecent(48);
        if (!cancelled) {
          const filtered = response.data.filter((item: CryptoNews) => item.id !== excludeId).slice(0, 5);
          setNews(filtered);
        }
      } catch (err) {
        console.error('Failed to fetch related news:', err);
      } finally { if (!cancelled) setLoading(false); }
    }
    fetchRelated();
    return () => { cancelled = true; };
  }, [excludeId]);

  if (loading) {
    return <div className="card p-6 flex justify-center"><Loader2 className="w-4 h-4 text-[#F26649] animate-spin" /></div>;
  }
  if (news.length === 0) return null;

  return (
    <div className="card">
      <div className="card-header"><h2>관련 뉴스</h2></div>
      <div>
        {news.map((item) => (
          <Link
            key={item.id}
            href={`/news/${item.id}`}
            className="block px-3 py-2.5 hover:bg-bg-hover transition-colors border-b border-border-light last:border-b-0"
          >
            <h3 className="text-[12px] font-medium text-tx-primary hover:text-[#F26649] transition-colors line-clamp-2">
              {item.title}
            </h3>
            {item.summary && (
              <div className="flex items-start gap-1.5 mt-1">
                <Sparkles className="w-3 h-3 text-[#F26649] flex-shrink-0 mt-0.5" />
                <p className="text-2xs text-tx-muted line-clamp-1">{item.summary}</p>
              </div>
            )}
            <div className="flex items-center gap-1.5 mt-1 text-2xs text-tx-muted">
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
    </div>
  );
}
