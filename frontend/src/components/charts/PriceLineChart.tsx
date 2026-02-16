'use client';

import { useState, useEffect, useMemo } from 'react';
import {
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Area,
  AreaChart,
} from 'recharts';
import { Loader2 } from 'lucide-react';
import { clsx } from 'clsx';
import { cryptoApi, type PriceHistory } from '@/lib/api';

interface PriceLineChartProps {
  coinId: string;
  timeframe: string;
}

// timeframe → API 파라미터 매핑
const timeframeConfig: Record<string, { interval: string; hours: number }> = {
  '1d': { interval: 'HOUR_1', hours: 24 },
  '7d': { interval: 'HOUR_1', hours: 168 },
  '30d': { interval: 'DAY_1', hours: 720 },
  '90d': { interval: 'DAY_1', hours: 2160 },
  '1y': { interval: 'DAY_1', hours: 8760 },
  'all': { interval: 'DAY_1', hours: 17520 },
};

export default function PriceLineChart({ coinId, timeframe }: PriceLineChartProps) {
  const [data, setData] = useState<PriceHistory[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchPriceHistory() {
      try {
        setLoading(true);
        setError(null);
        const config = timeframeConfig[timeframe] || timeframeConfig['7d'];
        const response = await cryptoApi.getPriceHistory(coinId, config.interval, config.hours);
        if (!cancelled) {
          setData(response.data);
        }
      } catch (err) {
        if (!cancelled) {
          console.error('Failed to fetch price history:', err);
          setError('가격 데이터를 불러올 수 없습니다.');
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    fetchPriceHistory();

    return () => {
      cancelled = true;
    };
  }, [coinId, timeframe]);

  const priceChange = useMemo(() => {
    if (data.length < 2) return 0;
    return ((data[data.length - 1].price - data[0].price) / data[0].price) * 100;
  }, [data]);

  const isPositive = priceChange >= 0;
  const chartColor = isPositive ? '#16A34A' : '#EF4444';

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    if (timeframe === '1d') {
      return date.toLocaleTimeString('ko-KR', { hour: '2-digit', minute: '2-digit' });
    }
    return date.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' });
  };

  const formatPrice = (price: number) => `$${price.toLocaleString()}`;

  const formatVolume = (volume: number) => {
    if (volume >= 1e9) return `$${(volume / 1e9).toFixed(1)}B`;
    if (volume >= 1e6) return `$${(volume / 1e6).toFixed(1)}M`;
    return `$${volume.toLocaleString()}`;
  };

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-background border border-border rounded-lg p-3 shadow-lg">
          <p className="text-sm text-text-muted mb-1">
            {new Date(label).toLocaleDateString('ko-KR', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
              hour: '2-digit',
              minute: '2-digit',
            })}
          </p>
          <p className="text-lg font-bold text-text mono-number">
            {formatPrice(payload[0].value)}
          </p>
        </div>
      );
    }
    return null;
  };

  if (loading) {
    return (
      <div className="w-full flex justify-center items-center py-16">
        <Loader2 className="w-8 h-8 text-primary animate-spin" />
      </div>
    );
  }

  if (error || data.length === 0) {
    return (
      <div className="w-full text-center py-16 text-text-muted text-sm">
        {error || '가격 데이터가 없습니다.'}
      </div>
    );
  }

  return (
    <div className="w-full">
      {/* Price Stats */}
      <div className="flex items-center gap-4 mb-4">
        <div>
          <span className="text-sm text-text-muted">현재가</span>
          <div className="text-2xl font-bold text-text mono-number">
            {formatPrice(data[data.length - 1]?.price || 0)}
          </div>
        </div>
        <div className={clsx(
          'px-3 py-1 rounded-lg text-sm font-semibold mono-number',
          isPositive ? 'bg-success/10 text-success' : 'bg-danger/10 text-danger'
        )}>
          {isPositive ? '+' : ''}{priceChange.toFixed(2)}%
        </div>
      </div>

      {/* Price Chart */}
      <div className="h-64 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data} margin={{ top: 5, right: 5, bottom: 5, left: 5 }}>
            <defs>
              <linearGradient id={`gradient-${coinId}-${timeframe}`} x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={chartColor} stopOpacity={0.3} />
                <stop offset="95%" stopColor={chartColor} stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="var(--border)" vertical={false} />
            <XAxis
              dataKey="timestamp"
              tickFormatter={formatDate}
              stroke="var(--text-muted)"
              fontSize={12}
              tickLine={false}
              axisLine={false}
            />
            <YAxis
              domain={['auto', 'auto']}
              tickFormatter={(value) => {
                if (value >= 1000) return `$${(value / 1000).toFixed(0)}k`;
                if (value >= 1) return `$${value.toFixed(0)}`;
                return `$${value.toFixed(4)}`;
              }}
              stroke="var(--text-muted)"
              fontSize={12}
              tickLine={false}
              axisLine={false}
              width={65}
            />
            <Tooltip content={<CustomTooltip />} />
            <Area
              type="monotone"
              dataKey="price"
              stroke={chartColor}
              strokeWidth={2}
              fill={`url(#gradient-${coinId}-${timeframe})`}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {/* Volume Chart */}
      {data.some(d => d.volume > 0) && (
        <div className="h-20 w-full mt-4">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart data={data} margin={{ top: 5, right: 5, bottom: 5, left: 5 }}>
              <XAxis dataKey="timestamp" hide />
              <YAxis hide />
              <Tooltip
                content={({ active, payload }) => {
                  if (active && payload && payload.length) {
                    return (
                      <div className="bg-background border border-border rounded-lg p-2 shadow-lg">
                        <p className="text-sm text-text-muted">
                          거래량: {formatVolume(payload[0].value as number)}
                        </p>
                      </div>
                    );
                  }
                  return null;
                }}
              />
              <Area
                type="monotone"
                dataKey="volume"
                stroke="var(--secondary)"
                strokeWidth={1}
                fill="var(--secondary)"
                fillOpacity={0.3}
              />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
}
