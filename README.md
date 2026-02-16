# Crypto Guide

가상자산 정보/분류 서비스 - FnGuide Company Guide 스타일의 암호화폐 정보 플랫폼

## 프로젝트 구조

```
crypto-guide/
├── frontend/          # Next.js + Tailwind CSS
│   ├── src/
│   │   ├── app/       # App Router
│   │   ├── components/
│   │   ├── lib/
│   │   └── styles/
│   └── ...
├── backend/           # Spring Boot + PostgreSQL
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── ...
└── README.md
```

## 기술 스택

### Frontend
- Next.js 14 (App Router)
- Tailwind CSS
- TypeScript

### Backend
- Spring Boot 3.x
- PostgreSQL
- OpenAI API (뉴스 요약)

## 주요 기능

1. **가상자산 정보 조회**
   - 실시간 가격 정보
   - 시가총액, 거래량 등 주요 지표
   - 가격 차트

2. **테마 분류**
   - 스테이블코인, 모듈러, DePIN 등
   - 테마별 코인 목록

3. **뉴스 및 분석**
   - 실시간 뉴스 수집
   - AI 기반 뉴스 요약
   - 프로젝트별 뉴스 아카이브

4. **Financial Highlight**
   - 프로젝트 재무 지표
   - TVL, 수익 현황 등

## 시작하기

### Frontend
```bash
cd frontend
npm install
npm run dev
```

### Backend
```bash
cd backend
./gradlew bootRun
```

## 환경 변수

### Frontend (.env.local)
```
NEXT_PUBLIC_API_URL=http://localhost:8080
```

### Backend (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/crypto_guide
    username: your_username
    password: your_password

openai:
  api-key: your_openai_api_key

external:
  coingecko:
    api-key: your_coingecko_api_key
```
