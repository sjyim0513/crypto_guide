import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx,mdx}',
    './src/components/**/*.{js,ts,jsx,tsx,mdx}',
    './src/app/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          DEFAULT: 'var(--primary)',
          light: 'var(--primary-light)',
          dark: 'var(--primary-dark)',
          50: 'var(--primary-50)',
          100: 'var(--primary-100)',
        },
        secondary: {
          DEFAULT: 'var(--secondary)',
          light: 'var(--secondary-light)',
          dark: 'var(--secondary-dark)',
        },
        accent: {
          DEFAULT: 'var(--accent)',
          light: 'var(--accent-light)',
          dark: 'var(--accent-dark)',
        },
        bg: {
          body: 'var(--bg-body)',
          white: 'var(--bg-white)',
          card: 'var(--bg-card)',
          muted: 'var(--bg-muted)',
          hover: 'var(--bg-hover)',
          section: 'var(--bg-section)',
        },
        tx: {
          primary: 'var(--text-primary)',
          secondary: 'var(--text-secondary)',
          muted: 'var(--text-muted)',
          light: 'var(--text-light)',
        },
        up: 'var(--up)',
        down: 'var(--down)',
        success: 'var(--success)',
        warning: 'var(--warning)',
        danger: 'var(--danger)',
        info: 'var(--info)',
        border: {
          DEFAULT: 'var(--border)',
          light: 'var(--border-light)',
          dark: 'var(--border-dark)',
        },
      },
      fontFamily: {
        sans: ['Pretendard', '-apple-system', 'BlinkMacSystemFont', 'sans-serif'],
        mono: ['SF Mono', 'Monaco', 'monospace'],
      },
      fontSize: {
        '2xs': ['10px', '14px'],
      },
      boxShadow: {
        sm: 'var(--shadow-sm)',
        md: 'var(--shadow-md)',
        lg: 'var(--shadow-lg)',
      },
      borderRadius: {
        sm: 'var(--radius-sm)',
        md: 'var(--radius-md)',
        lg: 'var(--radius-lg)',
      },
      maxWidth: {
        page: '1360px',
      },
    },
  },
  plugins: [],
};

export default config;
