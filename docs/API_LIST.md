# CryptoGuide API 구현 리스트

프론트엔드가 사용하는 API 기준으로, 백엔드에서 구현·연동해야 할 엔드포인트 목록입니다.  
**Base URL**: `http://localhost:8080/api` (context-path: `/api`)

---

## 1. 암호화폐 (Cryptocurrencies)

| # | Method | Path | Query/Path 파라미터 | 응답 타입 | 사용처 |
|---|--------|------|---------------------|-----------|--------|
| 1 | GET | `/v1/cryptocurrencies` | `page` (0), `size` (100) | `Page<CryptocurrencyDto>` | 홈 CryptoTable (목록, 페이지네이션) |
| 2 | GET | `/v1/cryptocurrencies/{coinId}` | path: `coinId` | `CryptocurrencyDto` | 코인 상세 페이지 |
| 3 | GET | `/v1/cryptocurrencies/search` | `query` (string) | `List<CryptocurrencyDto>` (배열) | Header 검색 |
| 4 | GET | `/v1/cryptocurrencies/theme/{themeSlug}` | path: `themeSlug`, query: `page`, `size` | `Page<CryptocurrencyDto>` | 테마 상세 코인 목록 |
| 5 | GET | `/v1/cryptocurrencies/top-gainers` | `limit` (기본 10) | `List<CryptocurrencyDto>` | 홈 TrendingCoins (5개) |
| 6 | GET | `/v1/cryptocurrencies/top-losers` | `limit` (기본 10) | `List<CryptocurrencyDto>` | (프론트 미사용, 선택 구현) |
| 7 | GET | `/v1/cryptocurrencies/top-volume` | `limit` (기본 10) | `List<CryptocurrencyDto>` | (프론트 미사용, 선택 구현) |
| 8 | GET | `/v1/cryptocurrencies/market-overview` | 없음 | `MarketOverviewDto` | 홈 MarketOverview |
| 9 | GET | `/v1/cryptocurrencies/{coinId}/price-history` | `interval` (HOUR_1, DAY_1 등), `hours` (숫자) | `List<PriceHistoryDto>` | 코인 상세 가격 차트 |

### 1-1. Page 응답 형식 (Spring Page 직렬화)

프론트엔드 `PageResponse<T>` 기대 필드:

- `content`: T[]
- `totalElements`: number
- `totalPages`: number
- `size`: number
- `number`: number (현재 페이지, 0-based)

Spring `Page` 기본 직렬화와 동일하므로 별도 래퍼 불필요.

### 1-2. 가격 히스토리 interval

프론트엔드 차트에서 사용하는 조합:

| timeframe (UI) | interval | hours |
|----------------|----------|--------|
| 1d | HOUR_1 | 24 |
| 7d | HOUR_1 | 168 |
| 30d | DAY_1 | 720 |
| 90d | DAY_1 | 2160 |
| 1y | DAY_1 | 8760 |
| all | DAY_1 | 17520 |

백엔드 `PriceHistory.TimeInterval` enum에 `HOUR_1`, `DAY_1` 포함되어 있으면 됨.

---

## 2. 테마 (Themes)

| # | Method | Path | Query/Path 파라미터 | 응답 타입 | 사용처 |
|---|--------|------|---------------------|-----------|--------|
| 1 | GET | `/v1/themes` | 없음 | `List<ThemeDto>` | Sidebar 테마 목록 |
| 2 | GET | `/v1/themes/with-count` | 없음 | `List<ThemeDto>` (각 항목에 `cryptoCount` 포함) | 테마 목록 페이지 |
| 3 | GET | `/v1/themes/{slug}` | path: `slug` (themeSlug) | `ThemeDto` | 테마 상세 페이지 헤더 |

---

## 3. 뉴스 (News)

| # | Method | Path | Query/Path 파라미터 | 응답 타입 | 사용처 |
|---|--------|------|---------------------|-----------|--------|
| 1 | GET | `/v1/news` | `page`, `size` (기본 20) | `Page<CryptoNewsDto>` | 뉴스 목록 페이지 |
| 2 | GET | `/v1/news/{id}` | path: `id` (Long) | `CryptoNewsDto` | 뉴스 상세 페이지 |
| 3 | GET | `/v1/news/crypto/{coinId}` | path: `coinId`, query: `page`, `size` | `Page<CryptoNewsDto>` | 코인 상세 관련 뉴스 (5개) |
| 4 | GET | `/v1/news/theme/{themeSlug}` | path: `themeSlug`, query: `page`, `size` | `Page<CryptoNewsDto>` | (프론트 미사용, 선택 구현) |
| 5 | GET | `/v1/news/recent` | `hours` (기본 24) | `List<CryptoNewsDto>` (배열) | 홈 LatestNews(24), RelatedNewsSection(48) |
| 6 | GET | `/v1/news/search` | `keyword`, `page`, `size` | `Page<CryptoNewsDto>` | 뉴스 목록 검색 |

---

## 4. 응답 DTO 필드 요약 (프론트엔드 기대값)

### CryptocurrencyDto

- id, coinId, symbol, name, imageUrl  
- currentPrice, marketCap, marketCapRank, fullyDilutedValuation, totalVolume  
- high24h, low24h, priceChange24h, priceChangePercentage24h, priceChangePercentage7d, priceChangePercentage30d  
- circulatingSupply, totalSupply, maxSupply  
- ath, athDate, athChangePercentage, atl, atlDate, atlChangePercentage  
- description, homepage, whitepaper, github, twitter, telegram  
- themes (ThemeDto[]), lastUpdated  

(날짜 필드는 ISO-8601 문자열로 내려주면 됨.)

### MarketOverviewDto

- totalMarketCap, totalVolume24h, marketCapChangePercentage24h  
- btcDominance, ethDominance  
- upCount, downCount, totalCoins  

### PriceHistoryDto

- timestamp (ISO-8601 또는 ms), price, marketCap, volume  

### ThemeDto

- id, slug, name, description, color, iconUrl  
- cryptoCount (optional, with-count / 테마 상세 시 사용)  

### CryptoNewsDto

- id, title, content, summary, sourceUrl, source, author, imageUrl, status  
- publishedAt, relatedCryptoSymbols (string[]), relatedThemes (string[]), createdAt  

---

## 5. 프론트엔드 미사용 API (선택 구현)

- `GET /v1/cryptocurrencies/top-losers`
- `GET /v1/cryptocurrencies/top-volume`
- `GET /v1/news/theme/{themeSlug}`

---

## 6. CORS / 환경

- 백엔드 `application.yml`: `server.servlet.context-path: /api`, 포트 8080  
- 프론트엔드: `NEXT_PUBLIC_API_URL=http://localhost:8080/api`  
- CORS: `http://localhost:3000` 허용 필요 (현재 설정됨)

위 목록이 프론트엔드와 1:1로 맞는 “구현해야 할 API 리스트”입니다.  
백엔드 컨트롤러는 이미 존재하므로, 실제 구현은 각 **Service** 레이어(DB 조회, 외부 API 연동, 데이터 채우기)를 완성하면 됩니다.
