'use client';

import { useState, useEffect, useCallback } from 'react';
import Header from '@/components/layout/Header';
import NewsList from '@/components/news/NewsList';
import NewsFilter from '@/components/news/NewsFilter';
import { newsApi, type CryptoNews, type PageResponse } from '@/lib/api';
import { Loader2 } from 'lucide-react';

const PAGE_SIZE = 20;

export default function NewsPage() {
  const [news, setNews] = useState<CryptoNews[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [activeSearch, setActiveSearch] = useState('');

  const fetchNews = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      let response;
      if (activeSearch.trim()) {
        response = await newsApi.search(activeSearch, page, PAGE_SIZE);
      } else {
        response = await newsApi.getAll(page, PAGE_SIZE);
      }
      const data: PageResponse<CryptoNews> = response.data;
      setNews(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
    } catch (err) {
      console.error('Failed to fetch news:', err);
      setError('뉴스를 불러올 수 없습니다.');
    } finally { setLoading(false); }
  }, [page, activeSearch]);

  useEffect(() => { fetchNews(); }, [fetchNews]);

  const handleSearch = (keyword: string) => setSearchKeyword(keyword);
  const handleSearchSubmit = () => { setActiveSearch(searchKeyword); setPage(0); };
  const handleClearSearch = () => { setSearchKeyword(''); setActiveSearch(''); setPage(0); };

  return (
    <div className="min-h-screen bg-bg-body">
      <Header />
      <div className="max-w-page mx-auto px-4 py-5">
        {/* Page header */}
        <div className="card mb-5">
          <div className="card-header">
            <h2>
              암호화폐 뉴스
              {totalElements > 0 && <span className="text-tx-muted font-normal text-xs ml-2">({totalElements.toLocaleString()}건)</span>}
            </h2>
          </div>
          <div className="card-body">
            <p className="text-tx-muted text-xs mb-3">AI가 요약한 최신 암호화폐 뉴스를 확인하세요</p>
            <NewsFilter
              searchQuery={searchKeyword}
              onSearchChange={handleSearch}
              onSearchSubmit={handleSearchSubmit}
              onClearSearch={handleClearSearch}
              activeSearch={activeSearch}
            />
          </div>
        </div>

        {/* List */}
        {loading && news.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-16 gap-3">
            <Loader2 className="w-6 h-6 text-[#F26649] animate-spin" />
            <p className="text-tx-muted text-xs">뉴스를 불러오는 중...</p>
          </div>
        ) : error && news.length === 0 ? (
          <div className="text-center py-16">
            <p className="text-tx-muted mb-3 text-sm">{error}</p>
            <button onClick={fetchNews} className="btn-primary text-xs">다시 시도</button>
          </div>
        ) : news.length === 0 ? (
          <div className="text-center py-16 text-tx-muted text-sm">
            {activeSearch ? `"${activeSearch}"에 대한 검색 결과가 없습니다.` : '뉴스가 없습니다.'}
          </div>
        ) : (
          <NewsList news={news} page={page} totalPages={totalPages} onPageChange={setPage} loading={loading} />
        )}
      </div>
    </div>
  );
}
