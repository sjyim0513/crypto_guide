'use client';

import Link from 'next/link';
import { Globe, FileText, Github, Twitter, MessageCircle, ExternalLink } from 'lucide-react';
import { type Cryptocurrency } from '@/lib/api';

interface ProjectInfoProps {
  crypto: Cryptocurrency;
}

export default function ProjectInfo({ crypto }: ProjectInfoProps) {
  const links = [
    { label: '홈페이지', url: crypto.homepage, icon: Globe },
    { label: '백서', url: crypto.whitepaper, icon: FileText },
    { label: 'GitHub', url: crypto.github, icon: Github },
    { label: 'Twitter', url: crypto.twitter, icon: Twitter },
    { label: 'Telegram', url: crypto.telegram, icon: MessageCircle },
  ].filter(link => link.url);

  return (
    <div className="card">
      <div className="card-header">
        <h2>프로젝트 정보</h2>
      </div>

      <div className="card-body">
        {/* Description */}
        {crypto.description && (
          <div className="mb-4">
            <h3 className="text-2xs font-semibold text-tx-muted uppercase tracking-wider mb-1.5">소개</h3>
            <p className="text-[12px] text-tx-secondary leading-relaxed">{crypto.description}</p>
          </div>
        )}

        {/* Themes (대·중·소) */}
        {(() => {
          const themeList = [crypto.themeLarge, crypto.themeMedium, crypto.themeSmall].filter(Boolean);
          return themeList.length > 0 && (
            <div className="mb-4">
              <h3 className="text-2xs font-semibold text-tx-muted uppercase tracking-wider mb-1.5">테마</h3>
              <div className="flex flex-wrap gap-1.5">
                {themeList.map((theme) => (
                  <Link key={theme!.slug} href={`/themes/${theme!.slug}`} className="badge badge-secondary text-2xs hover:opacity-80 transition-opacity">
                    {theme!.name}
                  </Link>
                ))}
              </div>
            </div>
          );
        })()}

        {/* Links */}
        {links.length > 0 && (
          <div>
            <h3 className="text-2xs font-semibold text-tx-muted uppercase tracking-wider mb-2">공식 링크</h3>
            <div className="space-y-1">
              {links.map((link) => (
                <a
                  key={link.label}
                  href={link.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex items-center justify-between px-3 py-2 bg-bg-muted rounded hover:bg-bg-hover transition-colors"
                >
                  <div className="flex items-center gap-2">
                    <link.icon className="w-3.5 h-3.5 text-tx-muted" />
                    <span className="text-[12px] font-medium text-tx-primary">{link.label}</span>
                  </div>
                  <ExternalLink className="w-3 h-3 text-tx-light" />
                </a>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
