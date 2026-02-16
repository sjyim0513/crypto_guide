'use client';

import { useState, useEffect, useMemo, useCallback } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { Star, ChevronUp, ChevronDown, ArrowUpDown, Loader2, ChevronLeft, ChevronRight } from 'lucide-react';
import { clsx } from 'clsx';
import { cryptoApi, type Cryptocurrency, type PageResponse } from '@/lib/api';
import { formatNumber, formatPrice } from '@/lib/utils';
import MiniSparkline from '@/components/charts/MiniSparkline';

type SortField = 'rank' | 'price' | 'change24h' | 'change7d' | 'marketCap' | 'volume24h';
type SortDirection = 'asc' | 'desc';

const PAGE_SIZE = 50;

export default function CryptoTable() {
  const [cryptos, setCryptos] = useState<Cryptocurrency[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [sortField, setSortField] = useState<SortField>('rank');
  const [sortDirection, setSortDirection] = useState<SortDirection>('asc');
  const [favorites, setFavorites] = useState<Set<string>>(new Set());

  useEffect(() => {
    try {
      const saved = localStorage.getItem('crypto_favorites');
      if (saved) setFavorites(new Set(JSON.parse(saved)));
    } catch {}
  }, []);

  const fetchCryptos = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await cryptoApi.getAll(page, PAGE_SIZE);
      const data: PageResponse<Cryptocurrency> = response.data;
      setCryptos(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error('Failed to fetch cryptocurrencies:', err);
      setError('암호화폐 데이터를 불러올 수 없습니다.');
    } finally { setLoading(false); }
  }, [page]);

  useEffect(() => {
    fetchCryptos();
    const interval = setInterval(fetchCryptos, 60000);
    return () => clearInterval(interval);
  }, [fetchCryptos]);

  const sortedCryptos = useMemo(() => {
    const sorted = [...cryptos];
    sorted.sort((a, b) => {
      let aVal: number, bVal: number;
      switch (sortField) {
        case 'rank': aVal = a.marketCapRank; bVal = b.marketCapRank; break;
        case 'price': aVal = a.currentPrice; bVal = b.currentPrice; break;
        case 'change24h': aVal = a.priceChangePercentage24h; bVal = b.priceChangePercentage24h; break;
        case 'change7d': aVal = a.priceChangePercentage7d; bVal = b.priceChangePercentage7d; break;
        case 'marketCap': aVal = a.marketCap; bVal = b.marketCap; break;
        case 'volume24h': aVal = a.totalVolume; bVal = b.totalVolume; break;
        default: aVal = a.marketCapRank; bVal = b.marketCapRank;
      }
      return sortDirection === 'asc' ? aVal - bVal : bVal - aVal;
    });
    return sorted;
  }, [cryptos, sortField, sortDirection]);

  const handleSort = (field: SortField) => {
    if (sortField === field) setSortDirection(prev => prev === 'asc' ? 'desc' : 'asc');
    else { setSortField(field); setSortDirection('asc'); }
  };

  const toggleFavorite = (coinId: string) => {
    setFavorites(prev => {
      const next = new Set(prev);
      if (next.has(coinId)) next.delete(coinId); else next.add(coinId);
      try { localStorage.setItem('crypto_favorites', JSON.stringify(Array.from(next))); } catch {}
      return next;
    });
  };

  const SortIcon = ({ field }: { field: SortField }) => {
    if (sortField !== field) return <ArrowUpDown className="w-3 h-3 text-tx-muted" />;
    return sortDirection === 'asc'
      ? <ChevronUp className="w-3 h-3 text-[#F26649]" />
      : <ChevronDown className="w-3 h-3 text-[#F26649]" />;
  };

  if (loading && cryptos.length === 0) {
    return (
      <div className="card">
        <div className="card-header"><h2>암호화폐 시가총액 순위</h2></div>
        <div className="p-12 flex flex-col items-center gap-3">
          <Loader2 className="w-6 h-6 text-[#F26649] animate-spin" />
          <p className="text-tx-muted text-xs">데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error && cryptos.length === 0) {
    return (
      <div className="card">
        <div className="card-header"><h2>암호화폐 시가총액 순위</h2></div>
        <div className="p-12 text-center">
          <p className="text-tx-muted mb-3 text-sm">{error}</p>
          <button onClick={fetchCryptos} className="btn-primary text-xs">다시 시도</button>
        </div>
      </div>
    );
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>
          암호화폐 시가총액 순위
          <span className="text-tx-muted font-normal text-xs ml-2">({totalElements.toLocaleString()})</span>
        </h2>
        {loading && <Loader2 className="w-3.5 h-3.5 text-[#F26649] animate-spin" />}
      </div>

      <div className="overflow-x-auto">
        <table className="table-fn">
          <thead>
            <tr>
              <th className="w-8 text-center px-2">&nbsp;</th>
              <th className="cursor-pointer select-none w-12 text-center" onClick={() => handleSort('rank')}>
                <div className="flex items-center justify-center gap-0.5"># <SortIcon field="rank" /></div>
              </th>
              <th className="min-w-[140px]">이름</th>
              <th className="cursor-pointer select-none text-right" onClick={() => handleSort('price')}>
                <div className="flex items-center gap-0.5 justify-end">가격 <SortIcon field="price" /></div>
              </th>
              <th className="cursor-pointer select-none text-right" onClick={() => handleSort('change24h')}>
                <div className="flex items-center gap-0.5 justify-end">24h <SortIcon field="change24h" /></div>
              </th>
              <th className="cursor-pointer select-none text-right" onClick={() => handleSort('change7d')}>
                <div className="flex items-center gap-0.5 justify-end">7d <SortIcon field="change7d" /></div>
              </th>
              <th className="cursor-pointer select-none text-right" onClick={() => handleSort('marketCap')}>
                <div className="flex items-center gap-0.5 justify-end">시가총액 <SortIcon field="marketCap" /></div>
              </th>
              <th className="cursor-pointer select-none text-right" onClick={() => handleSort('volume24h')}>
                <div className="flex items-center gap-0.5 justify-end">거래량(24h) <SortIcon field="volume24h" /></div>
              </th>
              <th className="text-center w-24">7일 차트</th>
            </tr>
          </thead>
          <tbody>
            {sortedCryptos.map((crypto) => (
              <tr key={crypto.coinId} className="group">
                <td className="text-center px-2">
                  <button
                    onClick={(e) => { e.stopPropagation(); toggleFavorite(crypto.coinId); }}
                    className="p-0.5 hover:text-warning transition-colors"
                  >
                    <Star className={clsx('w-3.5 h-3.5', favorites.has(crypto.coinId) ? 'fill-warning text-warning' : 'text-tx-light')} />
                  </button>
                </td>
                <td className="text-center text-tx-muted text-xs font-medium">{crypto.marketCapRank}</td>
                <td>
                  <Link href={`/crypto/${crypto.coinId}`} className="flex items-center gap-2 hover:text-[#F26649] transition-colors">
                    {crypto.imageUrl && (
                      <div className="relative w-5 h-5 flex-shrink-0">
                        <Image src={crypto.imageUrl} alt={crypto.name} fill className="rounded-full" sizes="20px" />
                      </div>
                    )}
                    <span className="font-semibold text-[13px]">{crypto.name}</span>
                    <span className="text-2xs text-tx-muted">{crypto.symbol.toUpperCase()}</span>
                  </Link>
                </td>
                <td className="text-right mono-number font-medium text-[13px]">{formatPrice(crypto.currentPrice)}</td>
                <td className={clsx('text-right mono-number font-medium text-[13px]', crypto.priceChangePercentage24h >= 0 ? 'text-up' : 'text-down')}>
                  {crypto.priceChangePercentage24h >= 0 ? '+' : ''}{crypto.priceChangePercentage24h.toFixed(2)}%
                </td>
                <td className={clsx('text-right mono-number text-[13px]', crypto.priceChangePercentage7d >= 0 ? 'text-up' : 'text-down')}>
                  {crypto.priceChangePercentage7d >= 0 ? '+' : ''}{crypto.priceChangePercentage7d.toFixed(2)}%
                </td>
                <td className="text-right mono-number text-[13px]">{formatNumber(crypto.marketCap)}</td>
                <td className="text-right mono-number text-[13px]">{formatNumber(crypto.totalVolume)}</td>
                <td className="text-center">
                  <MiniSparkline isPositive={crypto.priceChangePercentage7d >= 0} />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="px-4 py-3 border-t border-border flex items-center justify-between">
          <span className="text-2xs text-tx-muted">
            {page * PAGE_SIZE + 1}-{Math.min((page + 1) * PAGE_SIZE, totalElements)} / {totalElements.toLocaleString()}
          </span>
          <div className="flex items-center gap-1.5">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-1.5 rounded border border-border hover:bg-bg-hover disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronLeft className="w-3.5 h-3.5" />
            </button>
            <span className="text-xs font-medium px-2 text-tx-secondary">{page + 1} / {totalPages}</span>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page >= totalPages - 1}
              className="p-1.5 rounded border border-border hover:bg-bg-hover disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
            >
              <ChevronRight className="w-3.5 h-3.5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
