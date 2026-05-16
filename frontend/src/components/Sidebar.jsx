import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { logoutUser } from '../api';
import {
  LayoutDashboard, BookOpen, Users, BarChart3, GraduationCap,
  ClipboardList, LogOut, Menu, X, ChevronLeft, PenTool
} from 'lucide-react';

// Student face avatars using DiceBear API (different styles per user)
const AVATAR_STYLES = ['adventurer', 'avataaars', 'big-smile', 'lorelei', 'micah', 'miniavs', 'personas'];

function getUserAvatar(username) {
  // Deterministic style based on username hash
  let hash = 0;
  for (let i = 0; i < (username || '').length; i++) {
    hash = ((hash << 5) - hash) + username.charCodeAt(i);
    hash |= 0;
  }
  const styleIdx = Math.abs(hash) % AVATAR_STYLES.length;
  const style = AVATAR_STYLES[styleIdx];
  return `https://api.dicebear.com/9.x/${style}/svg?seed=${encodeURIComponent(username || 'user')}&backgroundColor=b6e3f4,c0aede,d1d4f9,ffd5dc,ffdfbf`;
}

const NAV_ITEMS = {
  ADMIN: [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/courses', icon: BookOpen, label: 'Courses' },
    { to: '/students', icon: Users, label: 'Students' },
    { to: '/reports', icon: BarChart3, label: 'Reports' },
  ],
  FACULTY: [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/courses', icon: BookOpen, label: 'My Courses' },
    { to: '/students', icon: Users, label: 'Students' },
    { to: '/reports', icon: BarChart3, label: 'Reports' },
  ],
  STUDENT: [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/courses', icon: BookOpen, label: 'Browse Courses' },
    { to: '/enrollments', icon: ClipboardList, label: 'My Enrollments' },
    { to: '/grades', icon: GraduationCap, label: 'My Grades' },
    { to: '/reports', icon: BarChart3, label: 'Transcript' },
  ],
};

export default function Sidebar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);
  const [mobileOpen, setMobileOpen] = useState(false);

  const items = NAV_ITEMS[user?.role] || NAV_ITEMS.STUDENT;

  const handleLogout = async () => {
    try { await logoutUser(); } catch (e) { /* ignore */ }
    logout();
    navigate('/');
  };

  const roleLabel = user?.role === 'ADMIN' ? 'Administrator' : user?.role === 'FACULTY' ? 'Faculty' : 'Student';
  const roleColor = user?.role === 'ADMIN' ? 'var(--error)' : user?.role === 'FACULTY' ? 'var(--cyan)' : 'var(--success)';
  const avatarUrl = getUserAvatar(user?.username);

  return (
    <>
      {/* Mobile toggle */}
      <button className="sidebar-mobile-toggle" onClick={() => setMobileOpen(!mobileOpen)}>
        {mobileOpen ? <X size={20} /> : <Menu size={20} />}
      </button>

      {mobileOpen && <div className="sidebar-backdrop" onClick={() => setMobileOpen(false)} />}

      <aside className={`sidebar ${collapsed ? 'collapsed' : ''} ${mobileOpen ? 'mobile-open' : ''}`}>
        {/* Logo with Pen Nib Icon */}
        <div className="sidebar-logo">
          {!collapsed && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <div className="sidebar-logo-icon">
                <PenTool size={20} color="white" strokeWidth={2.2} />
              </div>
              <div>
                <div style={{ fontSize: 15, fontWeight: 700, color: 'var(--text-primary)' }}>CMS</div>
                <div style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>Course Management</div>
              </div>
            </div>
          )}
          {collapsed && (
            <div className="sidebar-logo-icon" style={{ margin: '0 auto' }}>
              <PenTool size={20} color="white" strokeWidth={2.2} />
            </div>
          )}
          <button className="sidebar-collapse-btn" onClick={() => setCollapsed(!collapsed)} title="Toggle sidebar">
            <ChevronLeft size={16} style={{ transform: collapsed ? 'rotate(180deg)' : 'none', transition: 'transform 0.3s' }} />
          </button>
        </div>

        {/* Nav links */}
        <nav className="sidebar-nav">
          {items.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) => `sidebar-link ${isActive ? 'active' : ''}`}
              title={item.label}
            >
              <item.icon size={20} />
              {!collapsed && <span>{item.label}</span>}
            </NavLink>
          ))}
        </nav>

        {/* User with avatar / Logout */}
        <div className="sidebar-footer">
          {!collapsed && (
            <div className="sidebar-user">
              <img
                src={avatarUrl}
                alt={user?.username || 'User'}
                className="sidebar-avatar"
                onError={(e) => { e.target.style.display = 'none'; e.target.nextSibling.style.display = 'flex'; }}
              />
              <div className="sidebar-avatar-fallback" style={{ display: 'none' }}>
                {(user?.username || 'U')[0].toUpperCase()}
              </div>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                  {user?.username || 'User'}
                </div>
                <div style={{ fontSize: 11, color: roleColor, fontWeight: 500 }}>{roleLabel}</div>
              </div>
            </div>
          )}
          {collapsed && (
            <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 8 }}>
              <img
                src={avatarUrl}
                alt={user?.username || 'User'}
                className="sidebar-avatar"
                title={user?.username}
              />
            </div>
          )}
          <button className="sidebar-link" onClick={handleLogout} title="Logout" style={{ color: 'var(--error)', width: '100%' }}>
            <LogOut size={20} />
            {!collapsed && <span>Logout</span>}
          </button>
        </div>
      </aside>

      <style>{`
        .sidebar-mobile-toggle {
          display: none;
          position: fixed;
          top: 16px;
          left: 16px;
          z-index: 1001;
          width: 40px;
          height: 40px;
          border-radius: var(--radius-md);
          background: var(--bg-card);
          border: 1px solid var(--border);
          color: var(--text-primary);
          align-items: center;
          justify-content: center;
        }
        .sidebar-backdrop {
          display: none;
          position: fixed;
          inset: 0;
          background: rgba(0,0,0,0.5);
          z-index: 999;
        }
        .sidebar {
          position: fixed;
          top: 0;
          left: 0;
          bottom: 0;
          width: var(--sidebar-width);
          background: var(--bg-secondary);
          border-right: 1px solid var(--border);
          display: flex;
          flex-direction: column;
          z-index: 1000;
          transition: width 0.3s ease;
          overflow: hidden;
        }
        .sidebar.collapsed {
          width: var(--sidebar-collapsed);
        }
        .sidebar-logo {
          padding: 20px 16px;
          display: flex;
          align-items: center;
          justify-content: space-between;
          border-bottom: 1px solid var(--border);
          min-height: 72px;
        }
        .sidebar-logo-icon {
          width: 38px;
          height: 38px;
          border-radius: 11px;
          background: var(--gradient-primary);
          display: flex;
          align-items: center;
          justify-content: center;
          flex-shrink: 0;
          box-shadow: 0 2px 8px rgba(99, 102, 241, 0.35);
          transition: transform 0.2s;
        }
        .sidebar-logo-icon:hover {
          transform: scale(1.05);
        }
        .sidebar-collapse-btn {
          width: 28px; height: 28px;
          border-radius: var(--radius-sm);
          background: var(--bg-elevated);
          border: 1px solid var(--border);
          color: var(--text-secondary);
          display: flex; align-items: center; justify-content: center;
          cursor: pointer;
          transition: all 0.2s;
          flex-shrink: 0;
        }
        .sidebar-collapse-btn:hover {
          background: var(--bg-hover);
          color: var(--text-primary);
        }
        .sidebar-nav {
          flex: 1;
          padding: 12px 8px;
          display: flex;
          flex-direction: column;
          gap: 4px;
          overflow-y: auto;
        }
        .sidebar-link {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 10px 12px;
          border-radius: var(--radius-md);
          color: var(--text-secondary);
          font-size: 14px;
          font-weight: 500;
          transition: all 0.2s;
          text-decoration: none;
          border: none;
          background: none;
          cursor: pointer;
        }
        .sidebar-link:hover {
          background: var(--bg-elevated);
          color: var(--text-primary);
        }
        .sidebar-link.active {
          background: var(--accent-muted);
          color: var(--accent-hover);
        }
        .sidebar-footer {
          padding: 12px 8px;
          border-top: 1px solid var(--border);
        }
        .sidebar-user {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 8px 12px;
          margin-bottom: 8px;
        }
        .sidebar-avatar {
          width: 38px;
          height: 38px;
          border-radius: 10px;
          border: 2px solid var(--border-hover);
          background: var(--bg-elevated);
          object-fit: cover;
          flex-shrink: 0;
          transition: transform 0.2s, border-color 0.2s;
        }
        .sidebar-avatar:hover {
          transform: scale(1.08);
          border-color: var(--accent);
        }
        .sidebar-avatar-fallback {
          width: 38px;
          height: 38px;
          border-radius: 10px;
          background: var(--gradient-primary);
          display: flex;
          align-items: center;
          justify-content: center;
          font-size: 15px;
          font-weight: 700;
          color: white;
          flex-shrink: 0;
        }
        .collapsed .sidebar-link { justify-content: center; padding: 10px; }
        .collapsed .sidebar-user { justify-content: center; }
        .collapsed .sidebar-logo { justify-content: center; }
        .collapsed .sidebar-collapse-btn { position: absolute; right: 6px; top: 22px; }

        @media (max-width: 768px) {
          .sidebar-mobile-toggle { display: flex; }
          .sidebar-backdrop { display: block; }
          .sidebar { transform: translateX(-100%); }
          .sidebar.mobile-open { transform: translateX(0); }
          .sidebar.collapsed { width: var(--sidebar-width); }
        }
      `}</style>
    </>
  );
}
