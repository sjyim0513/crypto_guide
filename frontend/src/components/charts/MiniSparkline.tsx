'use client';

import { useMemo } from 'react';
import { LineChart, Line, ResponsiveContainer } from 'recharts';

interface MiniSparklineProps {
  data?: number[];
  color?: string;
  isPositive?: boolean;
}

export default function MiniSparkline({ 
  data, 
  color,
  isPositive = true 
}: MiniSparklineProps) {
  // 임시 데이터 생성
  const chartData = useMemo(() => {
    if (data) {
      return data.map((value, index) => ({ value, index }));
    }
    
    // 기본 임시 데이터
    const mockData = [];
    let value = 100;
    for (let i = 0; i < 24; i++) {
      value += (Math.random() - 0.5) * 10;
      mockData.push({ value, index: i });
    }
    return mockData;
  }, [data]);

  const strokeColor = color || (isPositive ? '#10B981' : '#EF4444');

  return (
    <div className="w-24 h-8">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart data={chartData}>
          <Line
            type="monotone"
            dataKey="value"
            stroke={strokeColor}
            strokeWidth={1.5}
            dot={false}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
