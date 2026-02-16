'use client';

import { useState, useEffect, useRef, useCallback } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { useRouter, usePathname } from 'next/navigation';
import { Search, X, Loader2, ChevronDown, User } from 'lucide-react';
import { clsx } from 'clsx';
import { cryptoApi, type Cryptocurrency } from '@/lib/api';
import { formatPrice } from '@/lib/utils';

const navItems = [
  { href: '/', label: '암호화폐' },
  { href: '/themes', label: '테마분류' },
  { href: '/news', label: '뉴스' },
];

export default function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const [searchQuery, setSearchQuery] = useState('');
  const [results, setResults] = useState<Cryptocurrency[]>([]);
  const [loading, setLoading] = useState(false);
  const [showResults, setShowResults] = useState(false);
  const [selectedIndex, setSelectedIndex] = useState(-1);
  const searchRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);
  const debounceRef = useRef<NodeJS.Timeout | null>(null);

  const doSearch = useCallback(async (query: string) => {
    if (!query.trim()) { setResults([]); setShowResults(false); return; }
    try {
      setLoading(true);
      const response = await cryptoApi.search(query.trim());
      setResults(response.data.slice(0, 7));
      setShowResults(true);
      setSelectedIndex(-1);
    } catch { setResults([]); }
    finally { setLoading(false); }
  }, []);

  useEffect(() => {
    if (debounceRef.current) clearTimeout(debounceRef.current);
    if (!searchQuery.trim()) { setResults([]); setShowResults(false); return; }
    debounceRef.current = setTimeout(() => doSearch(searchQuery), 300);
    return () => { if (debounceRef.current) clearTimeout(debounceRef.current); };
  }, [searchQuery, doSearch]);

  useEffect(() => {
    function handleClick(e: MouseEvent) {
      if (searchRef.current && !searchRef.current.contains(e.target as Node)) setShowResults(false);
    }
    document.addEventListener('mousedown', handleClick);
    return () => document.removeEventListener('mousedown', handleClick);
  }, []);

  const handleSelect = (coinId: string) => {
    setShowResults(false); setSearchQuery('');
    router.push(`/crypto/${coinId}`);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!showResults || results.length === 0) {
      if (e.key === 'Enter' && searchQuery.trim()) doSearch(searchQuery);
      return;
    }
    if (e.key === 'ArrowDown') { e.preventDefault(); setSelectedIndex(p => (p < results.length - 1 ? p + 1 : 0)); }
    else if (e.key === 'ArrowUp') { e.preventDefault(); setSelectedIndex(p => (p > 0 ? p - 1 : results.length - 1)); }
    else if (e.key === 'Enter') { e.preventDefault(); if (selectedIndex >= 0) handleSelect(results[selectedIndex].coinId); }
    else if (e.key === 'Escape') { setShowResults(false); inputRef.current?.blur(); }
  };

  return (
    <header className="sticky top-0 z-50">
      {/* Top Brand Bar */}
      <div className="h-1 bg-[#F26649]" />

      {/* Main Header */}
      <div className="bg-white border-b border-border">
        <div className="max-w-page mx-auto px-4 flex items-center justify-between h-14">
          {/* Logo */}
          <Link href="/" className="flex items-center gap-2 mr-8">
            <div className="w-7 h-7 rounded flex items-center justify-center bg-[#F26649]">
              <span className="text-white font-bold text-sm">C</span>
            </div>
            <span className="font-bold text-lg text-tx-primary tracking-tight">
              Crypto<span className="text-[#F26649]">Guide</span>
            </span>
          </Link>

          {/* Navigation */}
          <nav className="hidden md:flex items-center h-full">
            {navItems.map((item) => {
              const isActive = item.href === '/' ? pathname === '/' : pathname.startsWith(item.href);
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={clsx(
                    'relative h-14 flex items-center px-5 text-[13px] font-semibold transition-colors',
                    isActive
                      ? 'text-[#F26649]'
                      : 'text-tx-secondary hover:text-tx-primary'
                  )}
                >
                  {item.label}
                  {isActive && (
                    <span className="absolute bottom-0 left-0 right-0 h-[2px] bg-[#F26649]" />
                  )}
                </Link>
              );
            })}
          </nav>

          <div className="flex-1" />

          {/* Search */}
          <div ref={searchRef} className="relative hidden sm:block mr-3">
            <div className="flex items-center border border-border rounded h-8 px-2.5 w-56 focus-within:border-[#F26649] transition-colors">
              <Search className="w-3.5 h-3.5 text-tx-muted flex-shrink-0" />
              <input
                ref={inputRef}
                type="text"
                placeholder="코인 검색"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                onFocus={() => { if (results.length > 0) setShowResults(true); }}
                onKeyDown={handleKeyDown}
                className="bg-transparent border-none outline-none ml-2 text-xs text-tx-primary placeholder:text-tx-muted w-full"
              />
              {loading && <Loader2 className="w-3.5 h-3.5 text-tx-muted animate-spin" />}
              {!loading && searchQuery && (
                <button onClick={() => { setSearchQuery(''); setResults([]); setShowResults(false); inputRef.current?.focus(); }}>
                  <X className="w-3.5 h-3.5 text-tx-muted hover:text-tx-primary" />
                </button>
              )}
            </div>

            {/* Dropdown */}
            {showResults && (
              <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-border rounded shadow-lg overflow-hidden z-50 w-72">
                {results.length === 0 && !loading ? (
                  <div className="p-3 text-center text-tx-muted text-xs">검색 결과가 없습니다.</div>
                ) : (
                  <ul>
                    {results.map((coin, i) => (
                      <li key={coin.coinId}>
                        <button
                          onClick={() => handleSelect(coin.coinId)}
                          onMouseEnter={() => setSelectedIndex(i)}
                          className={clsx(
                            'w-full flex items-center gap-2.5 px-3 py-2 text-left transition-colors',
                            i === selectedIndex ? 'bg-bg-hover' : 'hover:bg-bg-hover'
                          )}
                        >
                          {coin.imageUrl && (
                            <div className="relative w-5 h-5 flex-shrink-0">
                              <Image src={coin.imageUrl} alt={coin.name} fill className="rounded-full" sizes="20px" />
                            </div>
                          )}
                          <div className="flex-1 min-w-0">
                            <span className="text-xs font-semibold text-tx-primary">{coin.name}</span>
                            <span className="text-2xs text-tx-muted ml-1.5">{coin.symbol.toUpperCase()}</span>
                            {coin.marketCapRank && <span className="text-2xs text-tx-light ml-1">#{coin.marketCapRank}</span>}
                          </div>
                          <div className="text-right flex-shrink-0">
                            <div className="mono-number text-xs font-medium">{formatPrice(coin.currentPrice)}</div>
                            <div className={clsx('text-2xs mono-number', coin.priceChangePercentage24h >= 0 ? 'text-up' : 'text-down')}>
                              {coin.priceChangePercentage24h >= 0 ? '+' : ''}{coin.priceChangePercentage24h.toFixed(2)}%
                            </div>
                          </div>
                        </button>
                      </li>
                    ))}
                  </ul>
                )}
                {results.length > 0 && (
                  <div className="px-3 py-1.5 border-t border-border-light text-2xs text-tx-muted flex gap-3">
                    <span>↑↓ 이동</span><span>Enter 선택</span><span>Esc 닫기</span>
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Live + User */}
          <div className="flex items-center gap-3">
            <div className="flex items-center gap-1.5">
              <span className="live-dot" />
              <span className="text-2xs font-semibold text-success">LIVE</span>
            </div>
            <button className="h-8 px-3 flex items-center gap-1.5 border border-border rounded text-xs text-tx-secondary hover:border-[#F26649] hover:text-[#F26649] transition-colors">
              <User className="w-3.5 h-3.5" />
              <span className="hidden sm:inline font-medium">로그인</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
