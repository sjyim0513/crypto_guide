# 한국 5대 원화 거래소 공지사항 파이프라인 — TODO

## 1. API/크롤링 대상 검증 요약

| 거래소 | 공지사항 API 여부 | 비고 |
|--------|-------------------|------|
| **빗썸** | ✅ 있음 | PUBLIC API > 서비스 정보 > [공지사항 조회](https://apidocs.bithumb.com/) (V2.1.5, 2025-07~) |
| **고팍스** | ✅ 있음 | 공개 API `GET /notices` (인증 불필요). 아래 엔드포인트 명세 참고 |
| **업비트** | ❌ 없음 | docs.upbit.com/changelog 는 웹페이지. 공지 목록용 REST API 미제공 → **크롤링** |
| **코인원** | ❌ 없음 | 개발자 문서에 공지사항 조회 API 없음 → **크롤링** |
| **코빗** | ❌ 없음 | docs.korbit.co.kr 에 공지 목록 API 미명시 → **크롤링** |

### API 엔드포인트 명세 (빗썸 · 고팍스)

**빗썸 (Bithumb)**  
- **공지사항 조회:** `GET https://api.bithumb.com/v1/notices`  
  - 파라미터: `count`(1~20, 기본 5)  
  - 응답: `categories`(Array), `title`, `pc_url`, `published_at`, `modified_at` (yyyy-MM-dd hh:mm:ss)  
  - 제한: 1회/초/IP  
- **경보제:** `GET https://api.bithumb.com/v1/market/virtual_asset_warning`  
  - 응답: `market`, `warning_type`, `warning_step`, `end_date`  
  - warning_type: PRICE_SUDDEN_FLUCTUATION, PRICE_DIFFERENCE_HIGH, SPECIFIC_ACCOUNT_HIGH_TRANSACTION, TRADING_VOLUME_SUDDEN_FLUCTUATION, DEPOSIT_AMOUNT_SUDDEN_FLUCTUATION  
  - warning_step: CAUTION, WARNING, DANGER

**고팍스 (Gopax)**  
- 문서: [gopax.github.io/API](https://gopax.github.io/API/index.html#53976d821f)  
- **공지사항 조회:** `GET https://api.gopax.co.kr/notices` (공개 API, 인증 불필요)  
- 쿼리: `limit`(기본 20, 최대 20), `page`(0부터), `type`(0=전체, 1=일반, 2=상장, 3=이벤트), `format`(0=HTML, 1=텍스트)  
- 응답: `id`, `type`, `title`, `content`, `createdAt`, `updatedAt`  
  - **URL**: API에 없음 → `https://www.gopax.co.kr/notice/detail?id={id}` 로 생성해 저장

---

## 2. DB 구조 (2테이블)

- **notice**: 거래소 공지사항. 제목·URL·발행/수정일 저장. 본문(content)·요약(summary)은 추후 수집/LLM용.
- **warning**: 마켓 경보(거래량 급등, 입금량 급등, 시세차이 등). 빗썸 경보제 API 스키마 기준.

**DDL**: `docs/schema_exchange_notice_warning.sql` (PostgreSQL) — 직접 실행해 테이블 생성.

---

## 3. TODO 리스트

### Phase 0: 검증 및 설계
- [x] **0-1** 빗썸 공지·경보 API 스키마 반영 (위 명세 + `schema_exchange_notice_warning.sql`)
- [ ] **0-2** 업비트·코인원·코빗 공지 페이지 HTML 구조 파악 (크롤링 셀렉터)

### Phase 1: 데이터 모델 및 저장소
- [ ] **1-1** `notice`, `warning` 테이블 생성 (SQL 실행: `docs/schema_exchange_notice_warning.sql`)
- [ ] **1-2** JPA 엔티티 `Notice`, `Warning` 및 Repository 추가 (거래소별 최신순, external_id 중복 방지)
- [ ] **1-3** 기존 `crypto_news` 와 분리 유지

### Phase 2: API 수집 (빗썸, 고팍스) — 5분마다, 공지 20건
- [ ] **2-1** 빗썸 공지 `GET /v1/notices?count=20` 호출 → DB 최신 공지와 비교 후 그 이후만 저장 (중복 없이)
- [ ] **2-2** 빗썸 경보 `GET /v1/market/virtual_asset_warning` 호출 → UPSERT (exchange+market+warning_type+warning_step+end_date)
- [ ] **2-3** 고팍스 공지 `GET /notices?limit=20` 호출 → url은 `https://www.gopax.co.kr/notice/detail?id={id}` 로 저장, 동일 dedup 로직
- [ ] **2-4** 스케줄러 5분 주기로 빗썸(공지+경보)·고팍스(공지) 호출

### Phase 3: 크롤링 수집 (업비트, 코인원, 코빗)
- [ ] **3-1** 크롤링 의존성 추가 (Jsoup 등)
- [ ] **3-2** 업비트 [공지](https://upbit.com/service_center/notice) 크롤링 (제목·URL → notice 저장, external_id=URL 또는 slug)
- [ ] **3-3** 코인원 [공지](https://coinone.co.kr/info/notice) 크롤링
- [ ] **3-4** 코빗 [공지](https://www.korbit.co.kr/notice/) 크롤링
- [ ] **3-5** 크롤링 스케줄러 등록 (5분 또는 10~15분, API와 시간 분산)
- [ ] **3-6** User-Agent·요청 간격 예절

### Phase 3.5 (추후): 본문 수집 + LLM 요약
- [ ] 공지 URL 방문해 본문 수집 → `notice.content` 저장
- [ ] LLM으로 요약 → `notice.summary` 저장

### Phase 4: API 노출 및 운영
- [ ] **4-1** 공지사항 조회 REST API 추가 (전체/거래소별, 페이징·최신순)
- [ ] **4-2** README 또는 API 문서에 공지 파이프라인·수집 주기 명시
- [ ] **4-3** (선택) 수집 실패 시 로그/알림으로 모니터링

---

## 4. 실행 순서 제안

1. **Phase 1** — SQL로 테이블 생성 후 JPA 엔티티·Repository
2. **Phase 2** — 빗썸(공지+경보)·고팍스(공지) 5분 스케줄, 20건·중복 제거
3. **Phase 3** — 업비트/코인원/코빗 크롤링 (제목·URL 저장)
4. **Phase 4** — 조회 API 및 문서화
5. **Phase 3.5** — (추후) URL 본문 수집 + LLM 요약

---

## 5. 검수 시 확인할 점

- [ ] notice / warning 2테이블 구조 동의
- [ ] 공지 20건·5분 주기·DB 최신과 비교해 이후만 저장 방식 동의
- [ ] 고팍스 URL: `https://www.gopax.co.kr/notice/detail?id={id}` 로 저장
- [ ] 크롤링 타겟: upbit.com/service_center/notice, coinone.co.kr/info/notice, korbit.co.kr/notice/

검수 후 "Phase 1부터 진행해줘" 등 실행 지시만 주시면 그에 맞춰 구현하겠습니다.
