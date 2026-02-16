'use client';

import Link from 'next/link';
import { Layers } from 'lucide-react';
import { type Theme } from '@/lib/api';

interface ThemeGridProps {
  themes: Theme[];
}

export default function ThemeGrid({ themes }: ThemeGridProps) {
  if (themes.length === 0) {
    return <div className="text-center py-16 text-tx-muted text-sm">등록된 테마가 없습니다.</div>;
  }

  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-3">
      {themes.map((theme) => {
        const color = theme.color || '#6B7280';
        return (
          <Link
            key={theme.slug}
            href={`/themes/${theme.slug}`}
            className="card p-4 hover:shadow-md transition-all group"
          >
            <div className="flex items-start gap-3">
              <div className="p-2 rounded-lg flex-shrink-0" style={{ backgroundColor: `${color}15` }}>
                {theme.iconUrl ? (
                  <img src={theme.iconUrl} alt={theme.name} className="w-5 h-5" />
                ) : (
                  <Layers className="w-5 h-5" style={{ color }} />
                )}
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-[13px] text-tx-primary group-hover:text-[#F26649] transition-colors">
                  {theme.name}
                </h3>
                {theme.description && (
                  <p className="text-2xs text-tx-muted mt-0.5 line-clamp-2">{theme.description}</p>
                )}
              </div>
            </div>
            <div className="mt-3 pt-3 border-t border-border-light flex items-center justify-between">
              <span className="text-2xs text-tx-muted">포함된 코인</span>
              <span className="text-xs font-bold text-tx-primary">
                {theme.cryptoCount !== undefined ? `${theme.cryptoCount}개` : '-'}
              </span>
            </div>
          </Link>
        );
      })}
    </div>
  );
}
