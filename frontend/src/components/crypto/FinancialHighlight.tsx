'use client';

import { TrendingUp, TrendingDown } from 'lucide-react';
import { clsx } from 'clsx';
import { type Cryptocurrency } from '@/lib/api';
import { formatNumber, formatDate } from '@/lib/utils';

interface FinancialHighlightProps {
  crypto: Cryptocurrency;
}

export default function FinancialHighlight({ crypto }: FinancialHighlightProps) {
  return (
    <div className="card">
      <div className="card-header">
        <h2>Financial Highlight</h2>
      </div>

      <div className="card-body p-0">
        {/* ATH / ATL — FnGuide 2열 테이블 스타일 */}
        <table className="table-info">
          <tbody>
            <tr>
              <th className="!bg-up/5">
                <span className="flex items-center gap-1 text-up">
                  <TrendingUp className="w-3 h-3" />역대 최고가 (ATH)
                </span>
              </th>
              <td className="mono-number font-bold text-up">${crypto.ath.toLocaleString()}</td>
              <th className="!bg-down/5">
                <span className="flex items-center gap-1 text-down">
                  <TrendingDown className="w-3 h-3" />역대 최저가 (ATL)
                </span>
              </th>
              <td className="mono-number font-bold text-down">${crypto.atl.toLocaleString()}</td>
            </tr>
            <tr>
              <th>ATH 일자</th>
              <td className="text-tx-secondary text-[12px]">{formatDate(crypto.athDate)}</td>
              <th>ATL 일자</th>
              <td className="text-tx-secondary text-[12px]">{formatDate(crypto.atlDate)}</td>
            </tr>
            <tr>
              <th>ATH 대비 변동</th>
              <td className={clsx('mono-number font-semibold', crypto.athChangePercentage >= 0 ? 'text-up' : 'text-down')}>
                {crypto.athChangePercentage >= 0 ? '+' : ''}{crypto.athChangePercentage.toFixed(2)}%
              </td>
              <th>ATL 대비 변동</th>
              <td className="mono-number font-semibold text-up">
                +{crypto.atlChangePercentage.toLocaleString()}%
              </td>
            </tr>
          </tbody>
        </table>

        {/* Key Metrics */}
        <div className="px-4 py-3 border-t border-border-light">
          <h3 className="section-title text-[13px] mb-3">주요 지표</h3>
        </div>
        <table className="table-info">
          <tbody>
            <tr>
              <th>시가총액</th>
              <td className="mono-number font-semibold">{formatNumber(crypto.marketCap)}</td>
              <th>거래량 (24h)</th>
              <td className="mono-number font-semibold">{formatNumber(crypto.totalVolume)}</td>
            </tr>
            <tr>
              <th>시총/거래량 비율</th>
              <td className="mono-number">
                {crypto.totalVolume > 0 ? (crypto.marketCap / crypto.totalVolume).toFixed(2) : '-'}
              </td>
              <th>24h 가격 변동</th>
              <td className={clsx('mono-number font-semibold', crypto.priceChange24h >= 0 ? 'text-up' : 'text-down')}>
                {crypto.priceChange24h >= 0 ? '+' : ''}${Math.abs(crypto.priceChange24h).toLocaleString()}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  );
}
