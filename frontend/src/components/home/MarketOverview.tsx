'use client';

import { useState, useEffect } from 'react';
import { TrendingUp, TrendingDown, Loader2 } from 'lucide-react';
import { cryptoApi, type MarketOverview as MarketOverviewType } from '@/lib/api';
import { formatNumber } from '@/lib/utils';

export default function MarketOverview() {
  const [data, setData] = useState<MarketOverviewType | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;
    async function fetchData() {
      try {
        setLoading(true);
        setError(null);
        const response = await cryptoApi.getMarketOverview();
        if (!cancelled) setData(response.data);
      } catch (err) {
        if (!cancelled) { console.error('Failed to fetch market overview:', err); setError('마켓 데이터를 불러올 수 없습니다.'); }
      } finally { if (!cancelled) setLoading(false); }
    }
    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => { cancelled = true; clearInterval(interval); };
  }, []);

  if (loading && !data) {
    return (
      <div className="card">
        <div className="card-header"><h2>시장 현황</h2></div>
        <div className="card-body">
          <div className="grid grid-cols-3 md:grid-cols-6 gap-4 animate-pulse">
            {Array.from({ length: 6 }).map((_, i) => (
              <div key={i}><div className="h-3 bg-bg-hover rounded w-16 mb-2" /><div className="h-5 bg-bg-hover rounded w-20" /></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (error && !data) {
    return (
      <div className="card">
        <div className="card-header"><h2>시장 현황</h2></div>
        <div className="card-body text-center text-tx-muted text-sm">
          <p>{error}</p>
          <button onClick={() => window.location.reload()} className="mt-2 text-[#F26649] hover:underline text-xs">다시 시도</button>
        </div>
      </div>
    );
  }

  if (!data) return null;

  const changePercent = data.marketCapChangePercentage24h;
  const isUp = changePercent >= 0;

  return (
    <div className="card">
      <div className="card-header">
        <h2>시장 현황</h2>
        <div className="flex items-center gap-1.5">
          <span className="live-dot" />
          <span className="text-2xs font-semibold text-success">실시간</span>
        </div>
      </div>

      {/* FnGuide Snapshot 스타일 - 2열 정보 테이블 */}
      <div className="card-body p-0">
        <table className="table-info">
          <tbody>
            <tr>
              <th>총 시가총액</th>
              <td className="mono-number font-semibold">{formatNumber(data.totalMarketCap)}</td>
              <th>24시간 거래량</th>
              <td className="mono-number font-semibold">{formatNumber(data.totalVolume24h)}</td>
            </tr>
            <tr>
              <th>BTC 도미넌스</th>
              <td className="mono-number font-semibold">{data.btcDominance.toFixed(1)}%</td>
              <th>ETH 도미넌스</th>
              <td className="mono-number font-semibold">{data.ethDominance.toFixed(1)}%</td>
            </tr>
            <tr>
              <th>시가총액 변동(24h)</th>
              <td className={`mono-number font-semibold ${isUp ? 'text-up' : 'text-down'}`}>
                {isUp ? '+' : ''}{changePercent.toFixed(2)}%
                {isUp ? <TrendingUp className="w-3.5 h-3.5 inline ml-1" /> : <TrendingDown className="w-3.5 h-3.5 inline ml-1" />}
              </td>
              <th>총 코인 수</th>
              <td className="mono-number font-semibold">{data.totalCoins.toLocaleString()}</td>
            </tr>
            <tr>
              <th>상승</th>
              <td className="text-up font-semibold">
                <TrendingUp className="w-3.5 h-3.5 inline mr-1" />
                {data.upCount.toLocaleString()}
              </td>
              <th>하락</th>
              <td className="text-down font-semibold">
                <TrendingDown className="w-3.5 h-3.5 inline mr-1" />
                {data.downCount.toLocaleString()}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
