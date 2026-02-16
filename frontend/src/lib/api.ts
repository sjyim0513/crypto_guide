import axios from "axios";

const API_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add auth token if available
    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error("API Error:", error.response?.data || error.message);
    return Promise.reject(error);
  },
);

// Types
export interface Cryptocurrency {
  id: number;
  coinId: string;
  symbol: string;
  name: string;
  imageUrl: string;
  currentPrice: number;
  marketCap: number;
  marketCapRank: number;
  fullyDilutedValuation: number;
  totalVolume: number;
  high24h: number;
  low24h: number;
  priceChange24h: number;
  priceChangePercentage24h: number;
  priceChangePercentage7d: number;
  priceChangePercentage30d: number;
  circulatingSupply: number;
  totalSupply: number;
  maxSupply: number;
  ath: number;
  athDate: string;
  athChangePercentage: number;
  atl: number;
  atlDate: string;
  atlChangePercentage: number;
  description: string;
  homepage: string;
  whitepaper: string;
  github: string;
  twitter: string;
  telegram: string;
  themeLarge?: Theme | null;
  themeMedium?: Theme | null;
  themeSmall?: Theme | null;
  lastUpdated: string;
}

export interface Theme {
  id: number;
  slug: string;
  name: string;
  description: string;
  color: string;
  iconUrl: string;
  cryptoCount?: number;
}

export interface CryptoNews {
  id: number;
  title: string;
  content: string;
  summary: string;
  sourceUrl: string;
  source: string;
  author: string;
  imageUrl: string;
  status: string;
  publishedAt: string;
  relatedCryptoSymbols: string[];
  relatedThemes: string[];
  createdAt: string;
}

export interface MarketOverview {
  totalMarketCap: number;
  totalVolume24h: number;
  marketCapChangePercentage24h: number;
  btcDominance: number;
  ethDominance: number;
  upCount: number;
  downCount: number;
  totalCoins: number;
}

export interface PriceHistory {
  timestamp: string;
  price: number;
  marketCap: number;
  volume: number;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

// API Functions
export const cryptoApi = {
  // Cryptocurrencies
  getAll: (page = 0, size = 100) =>
    api.get<PageResponse<Cryptocurrency>>("/v1/cryptocurrencies", {
      params: { page, size },
    }),

  getById: (coinId: string) =>
    api.get<Cryptocurrency>(`/v1/cryptocurrencies/${coinId}`),

  search: (query: string) =>
    api.get<Cryptocurrency[]>("/v1/cryptocurrencies/search", {
      params: { query },
    }),

  getByTheme: (themeSlug: string, page = 0, size = 50) =>
    api.get<PageResponse<Cryptocurrency>>(
      `/v1/cryptocurrencies/theme/${themeSlug}`,
      { params: { page, size } },
    ),

  getTopGainers: (limit = 10) =>
    api.get<Cryptocurrency[]>("/v1/cryptocurrencies/top-gainers", {
      params: { limit },
    }),

  getTopLosers: (limit = 10) =>
    api.get<Cryptocurrency[]>("/v1/cryptocurrencies/top-losers", {
      params: { limit },
    }),

  getTopVolume: (limit = 10) =>
    api.get<Cryptocurrency[]>("/v1/cryptocurrencies/top-volume", {
      params: { limit },
    }),

  getMarketOverview: () =>
    api.get<MarketOverview>("/v1/cryptocurrencies/market-overview"),

  getPriceHistory: (coinId: string, interval = "HOUR_1", hours = 24) =>
    api.get<PriceHistory[]>(`/v1/cryptocurrencies/${coinId}/price-history`, {
      params: { interval, hours },
    }),
};

export const themeApi = {
  getAll: () => api.get<Theme[]>("/v1/themes"),

  getAllWithCount: () => api.get<Theme[]>("/v1/themes/with-count"),

  getBySlug: (slug: string) => api.get<Theme>(`/v1/themes/${slug}`),
};

export const newsApi = {
  getAll: (page = 0, size = 20) =>
    api.get<PageResponse<CryptoNews>>("/v1/news", { params: { page, size } }),

  getById: (id: number) => api.get<CryptoNews>(`/v1/news/${id}`),

  getByCrypto: (coinId: string, page = 0, size = 20) =>
    api.get<PageResponse<CryptoNews>>(`/v1/news/crypto/${coinId}`, {
      params: { page, size },
    }),

  getByTheme: (themeSlug: string, page = 0, size = 20) =>
    api.get<PageResponse<CryptoNews>>(`/v1/news/theme/${themeSlug}`, {
      params: { page, size },
    }),

  getRecent: (hours = 24) =>
    api.get<CryptoNews[]>("/v1/news/recent", { params: { hours } }),

  search: (keyword: string, page = 0, size = 20) =>
    api.get<PageResponse<CryptoNews>>("/v1/news/search", {
      params: { keyword, page, size },
    }),
};

export default api;
