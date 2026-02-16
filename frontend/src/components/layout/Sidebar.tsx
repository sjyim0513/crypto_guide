'use client';

import { useState, useEffect } from 'react';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { ChevronRight, Loader2, TrendingUp } from 'lucide-react';
import { clsx } from 'clsx';
import { themeApi, type Theme } from '@/lib/api';

export default function Sidebar() {
  const pathname = usePathname();
  const [themes, setThemes] = useState<Theme[]>([]);
  const [loading, setLoading] = useState(true);
  const [expanded, setExpanded] = useState(true);

  useEffect(() => {
    let cancelled = false;
    async function fetchThemes() {
      try {
        const response = await themeApi.getAll();
        if (!cancelled) setThemes(response.data);
      } catch (err) {
        console.error('Failed to fetch sidebar themes:', err);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    fetchThemes();
    return () => { cancelled = true; };
  }, []);

  return (
    <aside className="hidden xl:block w-56 flex-shrink-0">
      {/* 테마별 분류 */}
      <div className="card">
        <div className="card-header">
          <h3 className="flex items-center gap-1.5">
            <TrendingUp className="w-3.5 h-3.5 text-[#F26649]" />
            테마별 분류
          </h3>
          <button
            onClick={() => setExpanded(!expanded)}
            className="text-tx-muted hover:text-tx-primary transition-colors"
          >
            <ChevronRight className={clsx('w-4 h-4 transition-transform', expanded && 'rotate-90')} />
          </button>
        </div>

        {expanded && (
          <div className="py-1">
            {loading ? (
              <div className="flex justify-center py-4">
                <Loader2 className="w-4 h-4 text-tx-muted animate-spin" />
              </div>
            ) : themes.length === 0 ? (
              <p className="text-xs text-tx-muted px-3 py-3 text-center">테마 없음</p>
            ) : (
              themes.map((theme) => {
                const isActive = pathname === `/themes/${theme.slug}`;
                return (
                  <Link
                    key={theme.slug}
                    href={`/themes/${theme.slug}`}
                    className={clsx(
                      'flex items-center gap-2 px-4 py-[7px] text-[12px] transition-colors border-l-2',
                      isActive
                        ? 'bg-primary-50 text-[#F26649] border-[#F26649] font-semibold'
                        : 'text-tx-secondary hover:bg-bg-hover border-transparent hover:text-tx-primary'
                    )}
                  >
                    <span
                      className="w-2 h-2 rounded-full flex-shrink-0"
                      style={{ backgroundColor: theme.color || '#6B7280' }}
                    />
                    <span className="truncate">{theme.name}</span>
                    {theme.cryptoCount !== undefined && (
                      <span className="text-2xs text-tx-muted ml-auto">{theme.cryptoCount}</span>
                    )}
                  </Link>
                );
              })
            )}
            <div className="border-t border-border-light px-4 py-2">
              <Link href="/themes" className="text-2xs text-[#F26649] font-semibold hover:underline">
                전체보기 →
              </Link>
            </div>
          </div>
        )}
      </div>
    </aside>
  );
}
