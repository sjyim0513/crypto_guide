# CryptoGuide API — 데이터베이스 테이블 명세

JPA 엔티티 기준 테이블 구조입니다. `ddl-auto: update`로 스키마가 자동 반영됩니다.

---

## 1. `cryptocurrencies`

암호화폐 마스터 및 시세 정보.

| 컬럼명 | 타입 | 제약 | 설명 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 자동 증가 ID |
| coin_id | VARCHAR(255) | UNIQUE, NOT NULL | 코인 식별자 (예: bitcoin) |
| symbol | VARCHAR(255) | NOT NULL | 심볼 (예: BTC) |
| name | VARCHAR(255) | NOT NULL | 이름 (예: Bitcoin) |
| image_url | VARCHAR(500) | | 로고 이미지 URL |
| current_price | NUMERIC(30,10) | | 현재가 |
| market_cap | NUMERIC(30,2) | | 시가총액 |
| market_cap_rank | INTEGER | | 시가총액 순위 |
| fully_diluted_valuation | NUMERIC(30,2) | | 완전희석가치(FDV) |
| total_volume | NUMERIC(30,2) | | 24h 거래량 |
| high_24h | NUMERIC(30,10) | | 24h 고가 |
| low_24h | NUMERIC(30,10) | | 24h 저가 |
| price_change_24h | NUMERIC(10,4) | | 24h 가격 변동액 |
| price_change_percentage_24h | NUMERIC(10,4) | | 24h 변동률(%) |
| price_change_percentage_7d | NUMERIC(10,4) | | 7일 변동률(%) |
| price_change_percentage_30d | NUMERIC(10,4) | | 30일 변동률(%) |
| circulating_supply | NUMERIC(30,2) | | 유통 공급량 |
| total_supply | NUMERIC(30,2) | | 총 공급량 |
| max_supply | NUMERIC(30,2) | | 최대 공급량 |
| ath | NUMERIC(30,10) | | 역대 최고가 |
| ath_date | TIMESTAMP | | ATH 일시 |
| ath_change_percentage | NUMERIC(10,4) | | ATH 대비 변동률(%) |
| atl | NUMERIC(30,10) | | 역대 최저가 |
| atl_date | TIMESTAMP | | ATL 일시 |
| atl_change_percentage | NUMERIC(10,4) | | ATL 대비 변동률(%) |
| description | VARCHAR(5000) | | 프로젝트 설명 |
| homepage | VARCHAR(500) | | 홈페이지 URL |
| whitepaper | VARCHAR(500) | | 백서 URL |
| github | VARCHAR(500) | | GitHub URL |
| twitter | VARCHAR(500) | | Twitter URL |
| telegram | VARCHAR(500) | | Telegram URL |
| theme_large_id | BIGINT | FK → themes.id | 테마 대분류 (1개) |
| theme_medium_id | BIGINT | FK → themes.id | 테마 중분류 (1개) |
| theme_small_id | BIGINT | FK → themes.id | 테마 소분류 (1개) |
| last_updated | TIMESTAMP | | 마지막 갱신 시각 |
| created_at | TIMESTAMP | NOT NULL | 생성 시각 |

**엔티티:** `com.cryptoguide.api.entity.Cryptocurrency`

---

## 2. `themes`

테마/카테고리 마스터. 코인은 대·중·소 각 1개씩 FK로 참조한다.

| 컬럼명 | 타입 | 제약 | 설명 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 자동 증가 ID |
| slug | VARCHAR(255) | UNIQUE, NOT NULL | URL용 식별자 (예: defi, stablecoin) |
| name | VARCHAR(255) | NOT NULL | 표시 이름 |
| description | VARCHAR(1000) | | 설명 |
| color | VARCHAR(255) | | Hex 색상 코드 |
| icon_url | VARCHAR(255) | | 아이콘 URL |
| created_at | TIMESTAMP | NOT NULL | 생성 시각 |

**엔티티:** `com.cryptoguide.api.entity.Theme`

---

## 3. `crypto_news`

암호화폐 뉴스 (원문 + AI 요약).

| 컬럼명 | 타입 | 제약 | 설명 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 자동 증가 ID |
| title | VARCHAR(255) | NOT NULL | 제목 |
| content | VARCHAR(10000) | | 본문 (마크다운) |
| summary | VARCHAR(2000) | | AI 요약 |
| source_url | VARCHAR(500) | | 원문 URL |
| source | VARCHAR(255) | NOT NULL | 매체명 (예: CoinDesk) |
| author | VARCHAR(255) | | 작성자 |
| image_url | VARCHAR(255) | | 대표 이미지 URL |
| status | VARCHAR(255) | | PENDING / PROCESSING / COMPLETED / FAILED |
| published_at | TIMESTAMP | | 기사 발행 시각 |
| created_at | TIMESTAMP | NOT NULL | 생성 시각 |
| updated_at | TIMESTAMP | | 수정 시각 |

**엔티티:** `com.cryptoguide.api.entity.CryptoNews`

---

## 4. `price_history`

코인별 가격/시총/거래량 시계열 (차트용).

| 컬럼명 | 타입 | 제약 | 설명 |
|--------|------|------|------|
| id | BIGSERIAL | PK | 자동 증가 ID |
| cryptocurrency_id | BIGINT | FK, NOT NULL | cryptocurrencies.id |
| price | NUMERIC(30,10) | NOT NULL | 시점 가격 |
| market_cap | NUMERIC(30,2) | | 시점 시가총액 |
| volume | NUMERIC(30,2) | | 시점 거래량 |
| interval | VARCHAR(255) | NOT NULL | MINUTE_1, HOUR_1, DAY_1 등 |
| timestamp | TIMESTAMP | NOT NULL | 시점 일시 |
| created_at | TIMESTAMP | NOT NULL | 레코드 생성 시각 |

**인덱스:** `idx_price_history_crypto_timestamp` (cryptocurrency_id, timestamp)

**엔티티:** `com.cryptoguide.api.entity.PriceHistory`

**TimeInterval:** MINUTE_1, MINUTE_5, MINUTE_15, HOUR_1, HOUR_4, DAY_1, WEEK_1

---

## 5. `news_cryptocurrencies` (조인 테이블)

뉴스 ↔ 관련 암호화폐 다대다 매핑.

| 컬럼명 | 타입 | 제약 | 설명 |
|--------|------|------|------|
| news_id | BIGINT | FK, PK | crypto_news.id |
| cryptocurrency_id | BIGINT | FK, PK | cryptocurrencies.id |

**연결:** `CryptoNews.relatedCryptos` ↔ Cryptocurrency

---

## 6. `news_themes` (조인 테이블)

뉴스 ↔ 관련 테마 다대다 매핑.

| 컬럼명 | 타입 | 제약 | 설명 |
|--------|------|------|------|
| news_id | BIGINT | FK, PK | crypto_news.id |
| theme_id | BIGINT | FK, PK | themes.id |

**연결:** `CryptoNews.relatedThemes` ↔ Theme

---

## ER 요약

```
cryptocurrencies ──(theme_large_id, theme_medium_id, theme_small_id)──→ themes
       ↑
       │  news_cryptocurrencies
       │         ↑
       └── crypto_news
                 │
           news_themes
                 │
                 ↓
             themes

cryptocurrencies ←── price_history (N:1)
```

- **cryptocurrencies**: 코인 마스터 + 시세. 코인당 테마 대·중·소 각 1개 FK.
- **themes**: 테마 마스터
- **crypto_news**: 뉴스 + AI 요약
- **price_history**: 코인별 시계열 (차트)
- 조인 테이블 2개: 뉴스–코인, 뉴스–테마
