'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import Link from 'next/link';
import { Star, TrendingUp, TrendingDown, ArrowLeft } from 'lucide-react';
import { clsx } from 'clsx';
import { type Cryptocurrency } from '@/lib/api';
import { formatPrice } from '@/lib/utils';

interface CryptoHeaderProps {
  crypto: Cryptocurrency;
}

export default function CryptoHeader({ crypto }: CryptoHeaderProps) {
  const [isFavorite, setIsFavorite] = useState(false);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('crypto_favorites');
      if (saved) {
        const favs: string[] = JSON.parse(saved);
        setIsFavorite(favs.includes(crypto.coinId));
      }
    } catch {}
  }, [crypto.coinId]);

  const toggleFavorite = () => {
    setIsFavorite(prev => {
      const next = !prev;
      try {
        const saved = localStorage.getItem('crypto_favorites');
        const favs: string[] = saved ? JSON.parse(saved) : [];
        if (next) favs.push(crypto.coinId);
        else { const idx = favs.indexOf(crypto.coinId); if (idx > -1) favs.splice(idx, 1); }
        localStorage.setItem('crypto_favorites', JSON.stringify(favs));
      } catch {}
      return next;
    });
  };

  const change24h = crypto.priceChangePercentage24h;
  const isUp = change24h >= 0;

  return (
    <div className="card">
      {/* Back nav */}
      <div className="px-4 py-2 border-b border-border-light">
        <Link href="/" className="inline-flex items-center gap-1 text-tx-muted hover:text-[#F26649] text-xs transition-colors">
          <ArrowLeft className="w-3 h-3" />
          목록으로
        </Link>
      </div>

      <div className="p-4 flex flex-col md:flex-row md:items-center justify-between gap-4">
        {/* Left: Coin identity */}
        <div className="flex items-center gap-3">
          {crypto.imageUrl && (
            <div className="relative w-10 h-10 flex-shrink-0">
              <Image src={crypto.imageUrl} alt={crypto.name} fill className="rounded-full" sizes="40px" />
            </div>
          )}
          <div>
            <div className="flex items-center gap-2 flex-wrap">
              <h1 className="text-lg font-bold text-tx-primary">{crypto.name}</h1>
              <span className="text-tx-muted text-sm">{crypto.symbol.toUpperCase()}</span>
              <span className="badge badge-primary">#{crypto.marketCapRank}</span>
              <button onClick={toggleFavorite} className="p-1 hover:bg-bg-hover rounded transition-colors">
                <Star className={clsx('w-4 h-4', isFavorite ? 'fill-warning text-warning' : 'text-tx-muted hover:text-warning')} />
              </button>
            </div>
            {(() => {
              const themeList = [crypto.themeLarge, crypto.themeMedium, crypto.themeSmall].filter(Boolean);
              return themeList.length > 0 && (
                <div className="flex flex-wrap gap-1.5 mt-1.5">
                  {themeList.map((theme) => (
                    <Link key={theme!.slug} href={`/themes/${theme!.slug}`} className="badge badge-secondary text-2xs hover:opacity-80 transition-opacity">
                      {theme!.name}
                    </Link>
                  ))}
                </div>
              );
            })()}
          </div>
        </div>

        {/* Right: Price + change */}
        <div className="text-right">
          <div className="mono-number text-2xl font-bold text-tx-primary">{formatPrice(crypto.currentPrice)}</div>
          <div className={clsx('flex items-center justify-end gap-1.5 mt-0.5', isUp ? 'text-up' : 'text-down')}>
            {isUp ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
            <span className="text-base font-semibold mono-number">
              {isUp ? '+' : ''}{change24h.toFixed(2)}%
            </span>
            <span className="text-tx-muted text-2xs">(24h)</span>
          </div>
        </div>
      </div>
    </div>
  );
}
