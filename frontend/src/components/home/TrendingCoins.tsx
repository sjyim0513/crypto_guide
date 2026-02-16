'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Flame, TrendingUp, TrendingDown, Loader2 } from 'lucide-react';
import { clsx } from 'clsx';
import { cryptoApi, type Cryptocurrency } from '@/lib/api';
import { formatPrice } from '@/lib/utils';

export default function TrendingCoins() {
  const [coins, setCoins] = useState<Cryptocurrency[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function fetchData() {
      try {
        setLoading(true);
        setError(null);
        const response = await cryptoApi.getTopGainers(5);
        if (!cancelled) setCoins(response.data);
      } catch (err) {
        if (!cancelled) { console.error('Failed to fetch trending coins:', err); setError('데이터 로드 실패'); }
      } finally { if (!cancelled) setLoading(false); }
    }
    fetchData();
    const interval = setInterval(fetchData, 60000);
    return () => { cancelled = true; clearInterval(interval); };
  }, []);

  return (
    <div className="card">
      <div className="card-header">
        <h3 className="flex items-center gap-1.5">
          <Flame className="w-3.5 h-3.5 text-[#F26649]" />
          급상승 코인
        </h3>
      </div>

      {loading && coins.length === 0 ? (
        <div className="p-6 flex justify-center"><Loader2 className="w-4 h-4 text-[#F26649] animate-spin" /></div>
      ) : error && coins.length === 0 ? (
        <div className="p-4 text-center text-tx-muted text-2xs">{error}</div>
      ) : (
        <div>
          {coins.map((coin, index) => (
            <Link
              key={coin.coinId}
              href={`/crypto/${coin.coinId}`}
              className="flex items-center gap-2 px-3 py-2 hover:bg-bg-hover transition-colors border-b border-border-light last:border-b-0"
            >
              <span className="text-2xs text-tx-muted w-4 text-center font-medium">{index + 1}</span>
              {coin.imageUrl && (
                <div className="relative w-4 h-4 flex-shrink-0">
                  <Image src={coin.imageUrl} alt={coin.name} fill className="rounded-full" sizes="16px" />
                </div>
              )}
              <div className="flex-1 min-w-0">
                <span className="text-[12px] font-semibold text-tx-primary truncate block">{coin.name}</span>
              </div>
              <div className="text-right flex-shrink-0">
                <div className={clsx(
                  'text-[12px] mono-number font-semibold',
                  coin.priceChangePercentage24h >= 0 ? 'text-up' : 'text-down'
                )}>
                  {coin.priceChangePercentage24h >= 0 ? '+' : ''}{coin.priceChangePercentage24h.toFixed(2)}%
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
    </div>
  );
}
