'use client';

import { useState, useEffect, useCallback } from 'react';
import { useParams } from 'next/navigation';
import { Loader2 } from 'lucide-react';
import Header from '@/components/layout/Header';
import CryptoHeader from '@/components/crypto/CryptoHeader';
import CryptoSnapshot from '@/components/crypto/CryptoSnapshot';
import PriceChart from '@/components/crypto/PriceChart';
import FinancialHighlight from '@/components/crypto/FinancialHighlight';
import ProjectInfo from '@/components/crypto/ProjectInfo';
import RelatedNews from '@/components/crypto/RelatedNews';
import { cryptoApi, type Cryptocurrency } from '@/lib/api';

export default function CryptoDetailPage() {
  const params = useParams();
  const coinId = params.coinId as string;

  const [crypto, setCrypto] = useState<Cryptocurrency | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchCrypto = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await cryptoApi.getById(coinId);
      setCrypto(response.data);
    } catch (err) {
      console.error('Failed to fetch crypto:', err);
      setError('암호화폐 데이터를 불러올 수 없습니다.');
    } finally {
      setLoading(false);
    }
  }, [coinId]);

  useEffect(() => {
    if (coinId) fetchCrypto();
    const interval = setInterval(fetchCrypto, 30000);
    return () => clearInterval(interval);
  }, [coinId, fetchCrypto]);

  if (loading && !crypto) {
    return (
      <div className="min-h-screen bg-bg-body">
        <Header />
        <main className="max-w-page mx-auto px-4 py-8">
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <Loader2 className="w-8 h-8 text-[#F26649] animate-spin" />
            <p className="text-tx-muted text-sm">데이터를 불러오는 중...</p>
          </div>
        </main>
      </div>
    );
  }

  if (error && !crypto) {
    return (
      <div className="min-h-screen bg-bg-body">
        <Header />
        <main className="max-w-page mx-auto px-4 py-8">
          <div className="flex flex-col items-center justify-center py-20 gap-4">
            <p className="text-tx-muted">{error}</p>
            <button onClick={fetchCrypto} className="btn-primary">다시 시도</button>
          </div>
        </main>
      </div>
    );
  }

  if (!crypto) return null;

  return (
    <div className="min-h-screen bg-bg-body">
      <Header />
      <main className="max-w-page mx-auto px-4 py-5">
        {/* Crypto Header / Snapshot top bar */}
        <CryptoHeader crypto={crypto} />

        {/* FnGuide 2-column layout */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-5 mt-5">
          {/* Left Column - 2/3 */}
          <div className="lg:col-span-2 space-y-5">
            <CryptoSnapshot crypto={crypto} />
            <PriceChart coinId={coinId} />
            <FinancialHighlight crypto={crypto} />
          </div>

          {/* Right Column - 1/3 */}
          <div className="space-y-5">
            <ProjectInfo crypto={crypto} />
            <RelatedNews coinId={coinId} />
          </div>
        </div>
      </main>
    </div>
  );
}
