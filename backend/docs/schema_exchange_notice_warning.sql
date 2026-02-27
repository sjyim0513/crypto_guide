-- ============================================================
-- 한국 5대 원화 거래소: 공지(notice) + 경보(warning) 테이블
-- 빗썸 API 스키마 기반, 다거래소 공통 저장용
-- PostgreSQL
-- ============================================================

-- 1. notice (거래소 공지사항)
-- 빗썸: categories, title, pc_url, published_at, modified_at
-- 고팍스: id, type, title, content, createdAt, updatedAt → url은 https://www.gopax.co.kr/notice/detail?id={id} 로 생성
-- 업비트/코인원/코빗: 크롤링으로 제목·URL 수집
CREATE TABLE notice (
    id              BIGSERIAL PRIMARY KEY,
    exchange        VARCHAR(50) NOT NULL,   -- bithumb, gopax, upbit, coinone, korbit
    external_id     VARCHAR(100) NOT NULL,  -- API id 또는 URL 슬러그 (중복 방지)
    title           VARCHAR(500) NOT NULL,
    url             VARCHAR(1000),         -- 공지 상세 URL (고팍스는 id로 조합)
    categories      VARCHAR(500),         -- 빗썸: JSON 배열 문자열 예 '["안내","점검"]'
    published_at    TIMESTAMP,             -- 공지 게시시간 (빗썸: yyyy-MM-dd hh:mm:ss)
    modified_at     TIMESTAMP,             -- 공지 수정시간
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    content         TEXT,                  -- (선택) 나중에 URL로 본문 수집 시
    summary         VARCHAR(2000),         -- (선택) LLM 요약
    UNIQUE (exchange, external_id)
);

CREATE INDEX idx_notice_exchange_created ON notice (exchange, created_at DESC);
CREATE INDEX idx_notice_published_at ON notice (published_at DESC);

COMMENT ON TABLE notice IS '거래소 공지사항 (빗썸/고팍스 API + 업비트/코인원/코빗 크롤링)';
COMMENT ON COLUMN notice.external_id IS '거래소별 고유 식별자. 빗썸: URL 또는 제목+일시, 고팍스: id, 크롤링: URL 또는 slug';
COMMENT ON COLUMN notice.categories IS '빗썸만 사용. JSON array 문자열';

-- 2. warning (마켓/토큰 경보)
-- 빗썸: market, warning_type, warning_step, end_date
-- (다른 거래소 경보 API 있으면 동일 스키마로 exchange 구분해 저장)
CREATE TABLE warning (
    id              BIGSERIAL PRIMARY KEY,
    exchange        VARCHAR(50) NOT NULL,   -- bithumb, (추후 gopax 등)
    market          VARCHAR(50) NOT NULL,  -- 빗썸: KRW-SXP 등
    warning_type    VARCHAR(100) NOT NULL, -- PRICE_SUDDEN_FLUCTUATION, PRICE_DIFFERENCE_HIGH, SPECIFIC_ACCOUNT_HIGH_TRANSACTION, TRADING_VOLUME_SUDDEN_FLUCTUATION, DEPOSIT_AMOUNT_SUDDEN_FLUCTUATION
    warning_step    VARCHAR(50) NOT NULL,  -- CAUTION, WARNING, DANGER
    end_date        TIMESTAMP,             -- 경보 종료일시 (KST, yyyy-MM-dd HH:mm:ss)
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (exchange, market, warning_type, warning_step, end_date)
);

CREATE INDEX idx_warning_exchange ON warning (exchange);
CREATE INDEX idx_warning_market ON warning (market);
CREATE INDEX idx_warning_end_date ON warning (end_date);

COMMENT ON TABLE warning IS '거래소별 마켓 경보 (가격 급등락, 거래량 급등, 입금량 급등, 시세차이, 소수계정 집중 등)';
COMMENT ON COLUMN warning.warning_type IS 'PRICE_SUDDEN_FLUCTUATION, PRICE_DIFFERENCE_HIGH, SPECIFIC_ACCOUNT_HIGH_TRANSACTION, TRADING_VOLUME_SUDDEN_FLUCTUATION, DEPOSIT_AMOUNT_SUDDEN_FLUCTUATION';
COMMENT ON COLUMN warning.warning_step IS 'CAUTION(주의), WARNING(경고), DANGER(위험)';


-- ============================================================
-- V2

-- =========================================================
-- notice: 거래소 공지
-- warning: 거래소 경보
-- =========================================================

CREATE TABLE IF NOT EXISTS notice (
    id              BIGSERIAL PRIMARY KEY,
    exchange        VARCHAR(20) NOT NULL,  -- bithumb, gopax, upbit, coinone, korbit
    external_id     VARCHAR(200) NOT NULL, -- 거래소 고유 ID (없으면 URL)
    title           VARCHAR(500) NOT NULL,
    url             VARCHAR(1000) NOT NULL,
    categories      JSONB,                 -- 빗썸 categories 배열
    notice_type     SMALLINT,              -- 고팍스 type(0/1/2/3)
    published_at    TIMESTAMPTZ,
    modified_at     TIMESTAMPTZ,
    content         TEXT,                  -- 추후 본문 수집
    summary         TEXT,                  -- 추후 LLM 요약
    raw_payload     JSONB NOT NULL DEFAULT '{}'::jsonb,
    first_seen_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_notice_exchange_external UNIQUE (exchange, external_id)
);

CREATE INDEX IF NOT EXISTS idx_notice_exchange_published
    ON notice (exchange, published_at DESC);

CREATE INDEX IF NOT EXISTS idx_notice_created_at
    ON notice (created_at DESC);


CREATE TABLE IF NOT EXISTS warning (
    id              BIGSERIAL PRIMARY KEY,
    exchange        VARCHAR(20) NOT NULL,   -- 현재 bithumb
    market          VARCHAR(50) NOT NULL,   -- KRW-BTC 등
    warning_type    VARCHAR(64) NOT NULL,   -- PRICE_SUDDEN_FLUCTUATION ...
    warning_step    VARCHAR(20) NOT NULL,   -- CAUTION/WARNING/DANGER
    end_at          TIMESTAMPTZ,
    raw_payload     JSONB NOT NULL DEFAULT '{}'::jsonb,
    first_seen_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_seen_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_warning_dedup UNIQUE (exchange, market, warning_type, warning_step, end_at)
);

CREATE INDEX IF NOT EXISTS idx_warning_exchange_market
    ON warning (exchange, market);

CREATE INDEX IF NOT EXISTS idx_warning_end_at
    ON warning (end_at);
