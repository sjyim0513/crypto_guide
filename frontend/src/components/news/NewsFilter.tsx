'use client';

import { Search, X } from 'lucide-react';

interface NewsFilterProps {
  searchQuery: string;
  onSearchChange: (query: string) => void;
  onSearchSubmit: () => void;
  onClearSearch: () => void;
  activeSearch: string;
}

export default function NewsFilter({ searchQuery, onSearchChange, onSearchSubmit, onClearSearch, activeSearch }: NewsFilterProps) {
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') onSearchSubmit();
  };

  return (
    <div>
      <div className="flex gap-2">
        <div className="flex-1 flex items-center border border-border rounded px-3 h-8 focus-within:border-[#F26649] transition-colors">
          <Search className="w-3.5 h-3.5 text-tx-muted flex-shrink-0" />
          <input
            type="text"
            placeholder="뉴스 검색... (Enter로 검색)"
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            onKeyDown={handleKeyDown}
            className="bg-transparent border-none outline-none ml-2 text-xs text-tx-primary placeholder:text-tx-muted w-full"
          />
          {searchQuery && (
            <button onClick={onClearSearch}>
              <X className="w-3.5 h-3.5 text-tx-muted hover:text-tx-primary" />
            </button>
          )}
        </div>
        <button onClick={onSearchSubmit} className="btn-primary text-xs px-4 h-8">검색</button>
      </div>

      {activeSearch && (
        <div className="mt-2 flex items-center gap-2 text-2xs">
          <span className="text-tx-muted">검색어:</span>
          <span className="badge badge-primary">{activeSearch}</span>
          <button onClick={onClearSearch} className="text-tx-muted hover:text-danger">초기화</button>
        </div>
      )}
    </div>
  );
}
