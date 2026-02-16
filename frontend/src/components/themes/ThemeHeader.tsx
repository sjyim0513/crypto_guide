'use client';

import { ArrowLeft, Layers } from 'lucide-react';
import Link from 'next/link';
import { type Theme } from '@/lib/api';

interface ThemeHeaderProps {
  theme: Theme;
}

export default function ThemeHeader({ theme }: ThemeHeaderProps) {
  const color = theme.color || '#6B7280';

  return (
    <div className="card">
      <div className="px-4 py-2 border-b border-border-light">
        <Link href="/themes" className="inline-flex items-center gap-1 text-tx-muted hover:text-[#F26649] text-xs transition-colors">
          <ArrowLeft className="w-3 h-3" />
          테마 목록
        </Link>
      </div>

      <div className="p-4 flex items-center gap-3">
        <div className="w-10 h-10 rounded-lg flex items-center justify-center flex-shrink-0" style={{ backgroundColor: `${color}15` }}>
          {theme.iconUrl ? (
            <img src={theme.iconUrl} alt={theme.name} className="w-5 h-5" />
          ) : (
            <Layers className="w-5 h-5" style={{ color }} />
          )}
        </div>
        <div>
          <h1 className="text-lg font-bold text-tx-primary">{theme.name}</h1>
          {theme.description && <p className="text-2xs text-tx-muted mt-0.5">{theme.description}</p>}
        </div>
        <div className="ml-auto text-right">
          <div className="text-2xs text-tx-muted">포함된 코인</div>
          <div className="text-lg font-bold text-tx-primary">{theme.cryptoCount !== undefined ? `${theme.cryptoCount}개` : '-'}</div>
        </div>
      </div>
    </div>
  );
}
