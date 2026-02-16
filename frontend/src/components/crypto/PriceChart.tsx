"use client";

import { useState } from "react";
import { clsx } from "clsx";
import PriceLineChart from "@/components/charts/PriceLineChart";

interface PriceChartProps {
  coinId: string;
}

const timeframes = [
  { label: "24시간", value: "1d" },
  { label: "7일", value: "7d" },
  { label: "30일", value: "30d" },
  { label: "90일", value: "90d" },
  { label: "1년", value: "1y" },
  { label: "전체", value: "all" },
];

export default function PriceChart({ coinId }: PriceChartProps) {
  const [selectedTimeframe, setSelectedTimeframe] = useState("7d");

  return (
    <div className="card">
      <div className="card-header">
        <h2>가격 차트</h2>
        <div className="flex gap-0.5">
          {timeframes.map((tf) => (
            <button
              key={tf.value}
              onClick={() => setSelectedTimeframe(tf.value)}
              className={clsx(
                "px-2.5 py-1 rounded text-2xs font-semibold transition-colors",
                selectedTimeframe === tf.value
                  ? "bg-[#F26649] text-white"
                  : "text-tx-muted hover:text-tx-primary hover:bg-bg-hover",
              )}
            >
              {tf.label}
            </button>
          ))}
        </div>
      </div>

      <div className="p-4">
        <PriceLineChart coinId={coinId} timeframe={selectedTimeframe} />
        <div className="flex items-center justify-center gap-6 mt-3 text-2xs">
          <div className="flex items-center gap-1.5">
            <div className="w-2.5 h-2.5 rounded-full bg-[#F26649]" />
            <span className="text-tx-muted">가격</span>
          </div>
          <div className="flex items-center gap-1.5">
            <div className="w-2.5 h-2.5 rounded-full bg-secondary" />
            <span className="text-tx-muted">거래량</span>
          </div>
        </div>
      </div>
    </div>
  );
}
