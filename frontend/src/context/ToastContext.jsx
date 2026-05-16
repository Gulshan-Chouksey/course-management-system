import { createContext, useContext, useState, useCallback } from 'react';
import { X, CheckCircle2, AlertTriangle, Info, AlertCircle } from 'lucide-react';

const ToastContext = createContext(null);

const ICONS = {
  success: CheckCircle2,
  error: AlertCircle,
  warning: AlertTriangle,
  info: Info,
};

const COLORS = {
  success: 'var(--success)',
  error: 'var(--error)',
  warning: 'var(--warning)',
  info: 'var(--info)',
};

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const addToast = useCallback((message, type = 'info', duration = 4000) => {
    const id = Date.now() + Math.random();
    setToasts((prev) => [...prev, { id, message, type }]);
    setTimeout(() => setToasts((prev) => prev.filter((t) => t.id !== id)), duration);
  }, []);

  const removeToast = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const toast = {
    success: (msg) => addToast(msg, 'success'),
    error: (msg) => addToast(msg, 'error'),
    warning: (msg) => addToast(msg, 'warning'),
    info: (msg) => addToast(msg, 'info'),
  };

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div style={{
        position: 'fixed', top: 20, right: 20, zIndex: 10000,
        display: 'flex', flexDirection: 'column', gap: 10, maxWidth: 380,
      }}>
        {toasts.map((t) => {
          const Icon = ICONS[t.type];
          return (
            <div key={t.id} style={{
              display: 'flex', alignItems: 'flex-start', gap: 12,
              padding: '14px 16px', borderRadius: 12,
              background: 'var(--bg-card)', border: '1px solid var(--border)',
              boxShadow: 'var(--shadow-lg)', animation: 'slideInUp 0.3s ease-out',
              borderLeft: `3px solid ${COLORS[t.type]}`,
            }}>
              <Icon size={18} color={COLORS[t.type]} style={{ marginTop: 1, flexShrink: 0 }} />
              <span style={{ flex: 1, fontSize: 14, color: 'var(--text-primary)', lineHeight: 1.5 }}>{t.message}</span>
              <button onClick={() => removeToast(t.id)} style={{
                background: 'none', border: 'none', color: 'var(--text-tertiary)',
                cursor: 'pointer', padding: 2, flexShrink: 0,
              }}>
                <X size={14} />
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const ctx = useContext(ToastContext);
  if (!ctx) throw new Error('useToast must be inside ToastProvider');
  return ctx;
}
