import { createContext, useContext, useState, useEffect, useCallback } from 'react';

const ThemeContext = createContext(null);

const THEMES = {
  dark: {
    label: 'Dark',
    emoji: '🌙',
    vars: {
      '--bg-root': '#060913',
      '--bg-primary': '#0b1120',
      '--bg-secondary': '#111827',
      '--bg-card': '#151e2e',
      '--bg-elevated': '#1c2740',
      '--bg-hover': '#243049',
      '--text-primary': '#f1f5f9',
      '--text-secondary': '#94a3b8',
      '--text-tertiary': '#64748b',
      '--text-inverse': '#0f172a',
      '--border': 'rgba(255, 255, 255, 0.06)',
      '--border-hover': 'rgba(255, 255, 255, 0.12)',
      '--shadow-sm': '0 1px 2px rgba(0, 0, 0, 0.3)',
      '--shadow-md': '0 4px 12px rgba(0, 0, 0, 0.4)',
      '--shadow-lg': '0 8px 32px rgba(0, 0, 0, 0.5)',
    },
  },
  light: {
    label: 'Light',
    emoji: '☀️',
    vars: {
      '--bg-root': '#f0f2f5',
      '--bg-primary': '#f8f9fb',
      '--bg-secondary': '#ffffff',
      '--bg-card': '#ffffff',
      '--bg-elevated': '#f1f3f7',
      '--bg-hover': '#e8ebf0',
      '--text-primary': '#0f172a',
      '--text-secondary': '#475569',
      '--text-tertiary': '#94a3b8',
      '--text-inverse': '#f1f5f9',
      '--border': 'rgba(0, 0, 0, 0.08)',
      '--border-hover': 'rgba(0, 0, 0, 0.15)',
      '--shadow-sm': '0 1px 3px rgba(0, 0, 0, 0.06)',
      '--shadow-md': '0 4px 12px rgba(0, 0, 0, 0.08)',
      '--shadow-lg': '0 8px 32px rgba(0, 0, 0, 0.12)',
    },
  },
  midnight: {
    label: 'Midnight',
    emoji: '🌌',
    vars: {
      '--bg-root': '#0a0015',
      '--bg-primary': '#10002b',
      '--bg-secondary': '#1a0040',
      '--bg-card': '#1f0050',
      '--bg-elevated': '#2a0066',
      '--bg-hover': '#35007a',
      '--text-primary': '#f1f5f9',
      '--text-secondary': '#c4b5fd',
      '--text-tertiary': '#8b5cf6',
      '--text-inverse': '#0f172a',
      '--border': 'rgba(139, 92, 246, 0.15)',
      '--border-hover': 'rgba(139, 92, 246, 0.3)',
      '--shadow-sm': '0 1px 2px rgba(0, 0, 0, 0.4)',
      '--shadow-md': '0 4px 12px rgba(0, 0, 0, 0.5)',
      '--shadow-lg': '0 8px 32px rgba(0, 0, 0, 0.6)',
    },
  },
};

export function ThemeProvider({ children }) {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem('cms_theme') || 'dark';
  });

  const applyTheme = useCallback((themeName) => {
    const t = THEMES[themeName];
    if (!t) return;
    const root = document.documentElement;
    Object.entries(t.vars).forEach(([key, value]) => {
      root.style.setProperty(key, value);
    });
    document.body.setAttribute('data-theme', themeName);
  }, []);

  useEffect(() => {
    applyTheme(theme);
    localStorage.setItem('cms_theme', theme);
  }, [theme, applyTheme]);

  const cycleTheme = useCallback(() => {
    const keys = Object.keys(THEMES);
    const idx = keys.indexOf(theme);
    setTheme(keys[(idx + 1) % keys.length]);
  }, [theme]);

  return (
    <ThemeContext.Provider value={{ theme, setTheme, cycleTheme, themes: THEMES }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error('useTheme must be inside ThemeProvider');
  return ctx;
}
