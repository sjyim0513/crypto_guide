'use client';

import Image from 'next/image';
import Link from 'next/link';
import { ArrowLeft, Clock, ExternalLink, Sparkles, Share2 } from 'lucide-react';
import { type CryptoNews } from '@/lib/api';
import { formatDateTime } from '@/lib/utils';

interface NewsDetailProps {
  news: CryptoNews;
}

export default function NewsDetail({ news }: NewsDetailProps) {
  const handleShare = async () => {
    try {
      if (navigator.share) {
        await navigator.share({ title: news.title, url: window.location.href });
      } else {
        await navigator.clipboard.writeText(window.location.href);
        alert('링크가 복사되었습니다.');
      }
    } catch {}
  };

  return (
    <article className="card">
      {/* Back nav */}
      <div className="px-4 py-2 border-b border-border-light">
        <Link href="/news" className="inline-flex items-center gap-1 text-tx-muted hover:text-[#F26649] text-xs transition-colors">
          <ArrowLeft className="w-3 h-3" />
          뉴스 목록
        </Link>
      </div>

      {/* Header */}
      <div className="p-4 border-b border-border">
        <h1 className="text-lg font-bold text-tx-primary mb-3">{news.title}</h1>
        <div className="flex flex-wrap items-center gap-3 text-2xs text-tx-muted">
          <span className="font-semibold text-tx-primary">{news.source}</span>
          {news.author && (<><span>·</span><span>{news.author}</span></>)}
          <span>·</span>
          <span className="flex items-center gap-0.5">
            <Clock className="w-3 h-3" />
            {formatDateTime(news.publishedAt)}
          </span>
        </div>
        <div className="flex flex-wrap gap-1.5 mt-3">
          {news.relatedCryptoSymbols?.map((coin) => (
            <Link key={coin} href={`/crypto/${coin.toLowerCase()}`} className="badge badge-primary text-2xs hover:opacity-80 transition-opacity">
              {coin}
            </Link>
          ))}
          {news.relatedThemes?.map((theme) => (
            <span key={theme} className="badge badge-secondary text-2xs">{theme}</span>
          ))}
        </div>
      </div>

      {/* Featured Image */}
      {news.imageUrl && (
        <div className="relative w-full h-64">
          <Image src={news.imageUrl} alt={news.title} fill className="object-cover" sizes="(max-width: 768px) 100vw, 860px" />
        </div>
      )}

      {/* AI Summary */}
      {news.summary && (
        <div className="px-4 py-3 bg-primary-50 border-b border-border">
          <div className="flex items-start gap-2.5">
            <div className="p-1.5 bg-primary-100 rounded flex-shrink-0">
              <Sparkles className="w-4 h-4 text-[#F26649]" />
            </div>
            <div>
              <h3 className="text-xs font-bold text-tx-primary mb-1">AI 요약</h3>
              <p className="text-[12px] text-tx-secondary leading-relaxed">{news.summary}</p>
            </div>
          </div>
        </div>
      )}

      {/* Content */}
      {news.content && (
        <div className="p-4">
          <div className="text-[13px] text-tx-secondary leading-[1.7]">
            {news.content.split('\n').map((paragraph, index) => {
              if (paragraph.startsWith('## ')) return <h2 key={index} className="text-base font-bold text-tx-primary mt-5 mb-2">{paragraph.replace('## ', '')}</h2>;
              if (paragraph.startsWith('### ')) return <h3 key={index} className="text-sm font-semibold text-tx-primary mt-4 mb-1.5">{paragraph.replace('### ', '')}</h3>;
              if (paragraph.startsWith('> ')) return <blockquote key={index} className="border-l-2 border-[#F26649] pl-3 italic my-3 text-tx-muted text-[12px]">{paragraph.replace('> ', '')}</blockquote>;
              if (paragraph.startsWith('- ')) return <li key={index} className="ml-4 mb-0.5">{paragraph.replace('- ', '')}</li>;
              if (paragraph.trim()) return <p key={index} className="my-2">{paragraph}</p>;
              return null;
            })}
          </div>
        </div>
      )}

      {/* Actions */}
      <div className="px-4 py-3 border-t border-border flex items-center justify-between">
        {news.sourceUrl && (
          <a href={news.sourceUrl} target="_blank" rel="noopener noreferrer" className="flex items-center gap-1.5 text-[#F26649] hover:underline text-xs font-medium">
            <ExternalLink className="w-3.5 h-3.5" />원문 보기
          </a>
        )}
        <button onClick={handleShare} className="p-1.5 hover:bg-bg-hover rounded transition-colors" title="공유하기">
          <Share2 className="w-4 h-4 text-tx-muted" />
        </button>
      </div>
    </article>
  );
}
