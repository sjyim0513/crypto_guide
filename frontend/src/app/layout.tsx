import type { Metadata } from 'next';
import './globals.css';

export const metadata: Metadata = {
  title: 'CryptoGuide | 암호화폐 실시간 시세와 투자 정보',
  description: '실시간 암호화폐 시세, 테마 분류, AI 뉴스 요약 - 종합 암호화폐 정보 플랫폼',
  keywords: ['암호화폐', '가상자산', '비트코인', '이더리움', '시세', '뉴스', '코인'],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="ko">
      <head>
        <link
          rel="stylesheet"
          as="style"
          crossOrigin="anonymous"
          href="https://cdn.jsdelivr.net/gh/orioncactus/pretendard@v1.3.9/dist/web/static/pretendard.min.css"
        />
      </head>
      <body className="antialiased bg-bg-body">
        {children}
      </body>
    </html>
  );
}
