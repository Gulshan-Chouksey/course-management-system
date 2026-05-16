import { useState, useRef, useEffect } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import { useTheme } from '../context/ThemeContext';
import { Palette, Sun, Moon, Sparkles, Check } from 'lucide-react';

const THEME_ICONS = {
  dark: Moon,
  light: Sun,
  midnight: Sparkles,
};

function ThemeToggle() {
  const { theme, setTheme, themes } = useTheme();
  const [open, setOpen] = useState(false);
  const ref = useRef();

  useEffect(() => {
    const handler = (e) => {
      if (ref.current && !ref.current.contains(e.target)) setOpen(false);
    };
    document.addEventListener('mousedown', handler);
    return () => document.removeEventListener('mousedown', handler);
  }, []);

  const CurrentIcon = THEME_ICONS[theme] || Palette;

  return (
    <div ref={ref} style={{ position: 'relative' }}>
      <button
        className="theme-toggle-btn"
        onClick={() => setOpen(!open)}
        title="Change theme"
      >
        <CurrentIcon size={18} />
        <span className="theme-toggle-label">Theme</span>
      </button>

      {open && (
        <div className="theme-dropdown">
          <div className="theme-dropdown-title">Choose Theme</div>
          {Object.entries(themes).map(([key, t]) => {
            const Icon = THEME_ICONS[key] || Palette;
            const isActive = theme === key;
            return (
              <button
                key={key}
                className={`theme-option ${isActive ? 'active' : ''}`}
                onClick={() => { setTheme(key); setOpen(false); }}
              >
                <Icon size={16} />
                <span>{t.label}</span>
                <span style={{ fontSize: 16 }}>{t.emoji}</span>
                {isActive && <Check size={14} style={{ marginLeft: 'auto', color: 'var(--success)' }} />}
              </button>
            );
          })}
        </div>
      )}
    </div>
  );
}

export default function DashboardLayout() {
  return (
    <div style={{ display: 'flex', minHeight: '100vh' }}>
      <Sidebar />
      <div style={{
        flex: 1,
        marginLeft: 'var(--sidebar-width)',
        display: 'flex',
        flexDirection: 'column',
        background: 'var(--bg-primary)',
        minHeight: '100vh',
        transition: 'margin-left 0.3s ease',
      }}>
        {/* Top bar with theme toggle */}
        <header className="topbar">
          <div />
          <ThemeToggle />
        </header>

        {/* Page content */}
        <main style={{ flex: 1, padding: '0 32px 32px' }}>
          <Outlet />
        </main>
      </div>

      <style>{`
        .topbar {
          display: flex;
          align-items: center;
          justify-content: space-between;
          padding: 16px 32px;
          position: sticky;
          top: 0;
          z-index: 50;
        }
        .theme-toggle-btn {
          display: flex;
          align-items: center;
          gap: 8px;
          padding: 8px 16px;
          background: var(--bg-card);
          border: 1px solid var(--border);
          border-radius: var(--radius-full);
          color: var(--text-secondary);
          font-size: 13px;
          font-weight: 500;
          cursor: pointer;
          transition: all 0.2s;
        }
        .theme-toggle-btn:hover {
          background: var(--bg-elevated);
          color: var(--text-primary);
          border-color: var(--border-hover);
          box-shadow: var(--shadow-sm);
        }
        .theme-toggle-label {
          display: inline;
        }
        .theme-dropdown {
          position: absolute;
          top: calc(100% + 8px);
          right: 0;
          min-width: 200px;
          background: var(--bg-card);
          border: 1px solid var(--border);
          border-radius: var(--radius-lg);
          padding: 8px;
          box-shadow: var(--shadow-lg);
          animation: scaleIn 0.2s ease-out;
          z-index: 100;
        }
        .theme-dropdown-title {
          font-size: 11px;
          font-weight: 600;
          text-transform: uppercase;
          letter-spacing: 0.05em;
          color: var(--text-tertiary);
          padding: 6px 10px 8px;
        }
        .theme-option {
          display: flex;
          align-items: center;
          gap: 10px;
          width: 100%;
          padding: 10px 12px;
          background: none;
          border: none;
          border-radius: var(--radius-md);
          color: var(--text-secondary);
          font-size: 14px;
          font-weight: 500;
          cursor: pointer;
          transition: all 0.15s;
        }
        .theme-option:hover {
          background: var(--bg-elevated);
          color: var(--text-primary);
        }
        .theme-option.active {
          background: var(--accent-muted);
          color: var(--accent-hover);
        }
        @media (max-width: 768px) {
          .topbar {
            padding: 12px 16px;
            padding-left: 60px;
          }
          main {
            padding: 0 16px 20px !important;
          }
          div[style*="margin-left"] {
            margin-left: 0 !important;
          }
          .theme-toggle-label {
            display: none;
          }
        }
      `}</style>
    </div>
  );
}
