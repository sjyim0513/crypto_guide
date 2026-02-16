'use client';

import { clsx } from 'clsx';
import { type Cryptocurrency } from '@/lib/api';
import { formatNumber } from '@/lib/utils';

interface CryptoSnapshotProps {
  crypto: Cryptocurrency;
}

function formatSupply(num: number): string {
  if (num >= 1e9) return `${(num / 1e9).toFixed(2)}B`;
  if (num >= 1e6) return `${(num / 1e6).toFixed(2)}M`;
  if (num >= 1e3) return `${(num / 1e3).toFixed(2)}K`;
  return num.toLocaleString();
}

export default function CryptoSnapshot({ crypto }: CryptoSnapshotProps) {
  const circulatingPercent = crypto.maxSupply
    ? (crypto.circulatingSupply / crypto.maxSupply * 100).toFixed(1)
    : null;

  const changes = [
    { label: '24시간', value: crypto.priceChangePercentage24h },
    { label: '7일', value: crypto.priceChangePercentage7d },
    { label: '30일', value: crypto.priceChangePercentage30d },
  ];

  return (
    <div className="card">
      <div className="card-header">
        <h2>시세현황</h2>
      </div>

      <div className="card-body p-0">
        {/* Price Changes - compact inline */}
        <div className="flex border-b border-border-light">
          {changes.map((item) => (
            <div key={item.label} className="flex-1 px-4 py-3 text-center border-r border-border-light last:border-r-0">
              <div className="text-2xs text-tx-muted mb-0.5">{item.label}</div>
              <div className={clsx(
                'mono-number text-sm font-semibold',
                item.value >= 0 ? 'text-up' : 'text-down'
              )}>
                {item.value >= 0 ? '+' : ''}{item.value.toFixed(2)}%
              </div>
            </div>
          ))}
        </div>

        {/* FnGuide-style 2-column info table */}
        <table className="table-info">
          <tbody>
            <tr>
              <th>시가총액</th>
              <td className="mono-number font-semibold">{formatNumber(crypto.marketCap)}</td>
              <th>24시간 거래량</th>
              <td className="mono-number font-semibold">{formatNumber(crypto.totalVolume)}</td>
            </tr>
            <tr>
              <th>완전희석가치(FDV)</th>
              <td className="mono-number">{formatNumber(crypto.fullyDilutedValuation)}</td>
              <th>거래량/시총 비율</th>
              <td className="mono-number">
                {crypto.marketCap > 0 ? ((crypto.totalVolume / crypto.marketCap) * 100).toFixed(2) : '0'}%
              </td>
            </tr>
            <tr>
              <th>24시간 최고가</th>
              <td className="mono-number text-up font-semibold">${crypto.high24h.toLocaleString()}</td>
              <th>24시간 최저가</th>
              <td className="mono-number text-down font-semibold">${crypto.low24h.toLocaleString()}</td>
            </tr>
            <tr>
              <th>유통량</th>
              <td className="mono-number">{formatSupply(crypto.circulatingSupply)}</td>
              <th>총 발행량</th>
              <td className="mono-number">{formatSupply(crypto.totalSupply)}</td>
            </tr>
            <tr>
              <th>최대 발행량</th>
              <td className="mono-number">{crypto.maxSupply ? formatSupply(crypto.maxSupply) : '∞'}</td>
              <th>유통률</th>
              <td className="mono-number">{circulatingPercent ? `${circulatingPercent}%` : '-'}</td>
            </tr>
          </tbody>
        </table>

        {/* Supply Progress Bar */}
        {circulatingPercent && (
          <div className="px-4 py-3 border-t border-border-light">
            <div className="flex justify-between text-2xs mb-1.5">
              <span className="text-tx-muted">유통량 진행률</span>
              <span className="mono-number text-tx-primary font-semibold">{circulatingPercent}%</span>
            </div>
            <div className="h-2 bg-bg-hover rounded-full overflow-hidden">
              <div className="h-full bg-[#F26649] rounded-full transition-all" style={{ width: `${circulatingPercent}%` }} />
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
