import { TrendingUp, TrendingDown, Minus } from 'lucide-react';

export default function StatCard({ title, value, icon: Icon, color, gradient, trend, trendLabel }) {
  const bg = gradient || `linear-gradient(135deg, ${color || 'var(--accent)'}, ${color || 'var(--accent)'}44)`;

  return (
    <div className="stat-card" style={{ animation: 'fadeIn 0.4s ease-out' }}>
      <div className="stat-card-top">
        <div>
          <p className="stat-card-label">{title}</p>
          <h3 className="stat-card-value">{value}</h3>
          {trend !== undefined && (
            <div className="stat-card-trend" style={{ color: trend > 0 ? 'var(--success)' : trend < 0 ? 'var(--error)' : 'var(--text-tertiary)' }}>
              {trend > 0 ? <TrendingUp size={14} /> : trend < 0 ? <TrendingDown size={14} /> : <Minus size={14} />}
              <span>{trendLabel || `${Math.abs(trend)}%`}</span>
            </div>
          )}
        </div>
        <div className="stat-card-icon" style={{ background: bg }}>
          {Icon && <Icon size={22} color="white" />}
        </div>
      </div>

      <style>{`
        .stat-card {
          background: var(--bg-card);
          border: 1px solid var(--border);
          border-radius: var(--radius-lg);
          padding: 22px;
          transition: all 0.3s ease;
          position: relative;
          overflow: hidden;
        }
        .stat-card::before {
          content: '';
          position: absolute;
          top: 0; left: 0; right: 0;
          height: 3px;
          background: ${bg};
          opacity: 0;
          transition: opacity 0.3s;
        }
        .stat-card:hover::before { opacity: 1; }
        .stat-card:hover { border-color: var(--border-hover); transform: translateY(-2px); box-shadow: var(--shadow-md); }
        .stat-card-top { display: flex; justify-content: space-between; align-items: flex-start; }
        .stat-card-label { font-size: 13px; color: var(--text-tertiary); font-weight: 500; margin-bottom: 6px; }
        .stat-card-value { font-size: 28px; font-weight: 700; color: var(--text-primary); line-height: 1.2; }
        .stat-card-trend { display: flex; align-items: center; gap: 4px; font-size: 12px; font-weight: 500; margin-top: 6px; }
        .stat-card-icon {
          width: 48px; height: 48px; border-radius: var(--radius-md);
          display: flex; align-items: center; justify-content: center;
          flex-shrink: 0;
        }
      `}</style>
    </div>
  );
}
