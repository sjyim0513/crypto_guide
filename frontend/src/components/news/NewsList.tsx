'use client';

import Link from 'next/link';
import Image from 'next/image';
import { Clock, Sparkles, ChevronLeft, ChevronRight, Loader2 } from 'lucide-react';
import { type CryptoNews } from '@/lib/api';
import { timeAgo } from '@/lib/utils';

interface NewsListProps {
  news: CryptoNews[];
  page: number;
  totalPages: number;
  onPageChange: (page: number) => void;
  loading: boolean;
}

export default function NewsList({ news, page, totalPages, onPageChange, loading }: NewsListProps) {
  return (
    <div>
      {loading && news.length > 0 && (
        <div className="flex justify-center mb-3">
          <Loader2 className="w-4 h-4 text-[#F26649] animate-spin" />
        </div>
      )}

      <div className="space-y-3">
        {news.map((item) => (
          <Link
            key={item.id}
            href={`/news/${item.id}`}
            className="card flex gap-3 p-3 hover:shadow-md transition-all group"
          >
            {item.imageUrl && (
              <div className="relative w-36 h-24 flex-shrink-0 rounded overflow-hidden bg-bg-muted hidden sm:block">
                <Image
                  src={item.imageUrl}
                  alt={item.title}
                  fill
                  className="object-cover group-hover:scale-105 transition-transform"
                  sizes="144px"
                />
              </div>
            )}
            <div className="flex-1 min-w-0">
              <h3 className="font-semibold text-[13px] text-tx-primary group-hover:text-[#F26649] transition-colors line-clamp-2">
                {item.title}
              </h3>
              {item.summary && (
                <div className="flex items-start gap-1.5 mt-1.5">
                  <Sparkles className="w-3 h-3 text-[#F26649] flex-shrink-0 mt-0.5" />
                  <p className="text-2xs text-tx-muted line-clamp-2">{item.summary}</p>
                </div>
              )}
              <div className="flex items-center justify-between mt-2">
                <div className="flex items-center gap-2 text-2xs text-tx-muted">
                  <span className="font-medium">{item.source}</span>
                  <span>·</span>
                  <span className="flex items-center gap-0.5">
                    <Clock className="w-2.5 h-2.5" />
                    {timeAgo(item.publishedAt)}
                  </span>
                </div>
                <div className="flex gap-1 flex-shrink-0">
                  {item.relatedCryptoSymbols?.slice(0, 3).map((coin) => (
                    <span key={coin} className="badge badge-primary text-2xs">{coin}</span>
                  ))}
                  {item.relatedThemes?.slice(0, 1).map((theme) => (
                    <span key={theme} className="badge badge-secondary text-2xs">{theme}</span>
                  ))}
                </div>
              </div>
            </div>
          </Link>
        ))}
      </div>

      {totalPages > 1 && (
        <div className="mt-5 flex items-center justify-center gap-3">
          <button
            onClick={() => onPageChange(Math.max(0, page - 1))}
            disabled={page === 0}
            className="p-1.5 rounded border border-border hover:bg-bg-hover disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronLeft className="w-3.5 h-3.5" />
          </button>
          <span className="text-xs font-medium text-tx-secondary">{page + 1} / {totalPages}</span>
          <button
            onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
            disabled={page >= totalPages - 1}
            className="p-1.5 rounded border border-border hover:bg-bg-hover disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
          >
            <ChevronRight className="w-3.5 h-3.5" />
          </button>
        </div>
      )}
    </div>
  );
}
