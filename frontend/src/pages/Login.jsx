import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useToast } from '../context/ToastContext';
import { useTheme } from '../context/ThemeContext';
import { loginUser } from '../api';
import { LogIn, Eye, EyeOff, BookOpen, Users, GraduationCap, BarChart3, PenTool, Sun, Moon, Sparkles } from 'lucide-react';

const DEMO_CREDENTIALS = [
  { role: 'Admin', username: 'admin', password: 'Admin@123', color: 'var(--error)', icon: '🛡️' },
  { role: 'Faculty', username: 'john.smith', password: 'Faculty@123', color: 'var(--cyan)', icon: '👨‍🏫' },
  { role: 'Student', username: 'alice.anderson', password: 'Student@123', color: 'var(--success)', icon: '🎓' },
];

const FEATURES = [
  { icon: BookOpen, label: 'Course Management', desc: 'Browse and enroll in courses' },
  { icon: GraduationCap, label: 'Grade Tracking', desc: 'Real-time GPA & transcripts' },
  { icon: Users, label: 'Faculty Portal', desc: 'Manage students and grades' },
  { icon: BarChart3, label: 'Analytics', desc: 'Insights with visual reports' },
];

export default function Login() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();
  const { theme, cycleTheme, themes } = useTheme();

  const THEME_ICONS = { dark: Moon, light: Sun, midnight: Sparkles };
  const ThemeIcon = THEME_ICONS[theme] || Moon;

  const handleLogin = async (e) => {
    e.preventDefault();
    if (!username || !password) { toast.warning('Please fill in all fields'); return; }
    setLoading(true);
    try {
      const data = await loginUser(username, password);
      login({ username, role: data.role });
      toast.success(`Welcome back, ${username}!`);
      navigate('/dashboard');
    } catch (err) {
      toast.error(err.message || 'Login failed');
    } finally {
      setLoading(false);
    }
  };

  const fillCredentials = (u, p) => {
    setUsername(u);
    setPassword(p);
    toast.info(`Filled credentials for ${u}`);
  };

  return (
    <div className="login-page">
      {/* Theme toggle on login page */}
      <button className="login-theme-btn" onClick={cycleTheme} title={`Switch to ${themes[Object.keys(themes)[(Object.keys(themes).indexOf(theme) + 1) % Object.keys(themes).length]]?.label} theme`}>
        <ThemeIcon size={16} />
        <span>{themes[theme]?.emoji}</span>
      </button>

      {/* Animated background */}
      <div className="login-bg">
        <div className="login-orb orb-1" />
        <div className="login-orb orb-2" />
        <div className="login-orb orb-3" />
      </div>

      <div className="login-container">
        {/* Left panel */}
        <div className="login-left">
          <div className="login-brand">
            <div className="login-logo"><PenTool size={22} color="white" strokeWidth={2.2} /></div>
            <h1>AcademiaX</h1>
            <p>Next-generation platform for managing academic records, courses, and student progress.</p>
          </div>

          <div className="login-features">
            {FEATURES.map((f, i) => (
              <div key={i} className="login-feature" style={{ animationDelay: `${i * 0.1}s` }}>
                <div className="login-feature-icon">
                  <f.icon size={20} />
                </div>
                <div>
                  <div style={{ fontWeight: 600, fontSize: 14 }}>{f.label}</div>
                  <div style={{ fontSize: 12, color: 'rgba(255,255,255,0.6)' }}>{f.desc}</div>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Right panel */}
        <div className="login-right">
          <div className="login-form-header">
            <h2>Welcome Back</h2>
            <p>Sign in to continue to your dashboard</p>
          </div>

          <form onSubmit={handleLogin} className="login-form">
            <div className="form-group">
              <label className="form-label">Username</label>
              <input
                type="text" className="form-input"
                value={username} onChange={(e) => setUsername(e.target.value)}
                placeholder="Enter your username"
                autoComplete="username" autoFocus
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <input
                  type={showPass ? 'text' : 'password'} className="form-input"
                  value={password} onChange={(e) => setPassword(e.target.value)}
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  style={{ paddingRight: 44 }}
                />
                <button type="button" onClick={() => setShowPass(!showPass)}
                  style={{
                    position: 'absolute', right: 12, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', color: 'var(--text-tertiary)', cursor: 'pointer',
                  }}>
                  {showPass ? <EyeOff size={18} /> : <Eye size={18} />}
                </button>
              </div>
            </div>

            <button type="submit" className="btn btn-primary login-submit" disabled={loading}>
              {loading ? <div className="spinner" style={{ width: 18, height: 18, borderWidth: 2 }} /> : <LogIn size={18} />}
              {loading ? 'Signing in...' : 'Sign In'}
            </button>
          </form>

          {/* Demo credentials */}
          <div className="login-demo">
            <div className="login-demo-title">
              <span>Demo Credentials</span>
              <span style={{ fontSize: 11, color: 'var(--text-tertiary)' }}>Click to auto-fill</span>
            </div>
            <div className="login-demo-grid">
              {DEMO_CREDENTIALS.map((cred, i) => (
                <button key={i} className="login-demo-card"
                  onClick={() => fillCredentials(cred.username, cred.password)}>
                  <span style={{ fontSize: 20 }}>{cred.icon}</span>
                  <div>
                    <div style={{ fontSize: 12, fontWeight: 600, color: cred.color }}>{cred.role}</div>
                    <div style={{ fontSize: 12, color: 'var(--text-secondary)' }}>{cred.username}</div>
                  </div>
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>

      <style>{`
        .login-page {
          min-height: 100vh;
          display: flex;
          align-items: center;
          justify-content: center;
          padding: 20px;
          position: relative;
          overflow: hidden;
          background: var(--bg-root);
        }
        .login-theme-btn {
          position: fixed;
          top: 20px;
          right: 20px;
          z-index: 10;
          display: flex;
          align-items: center;
          gap: 6px;
          padding: 8px 14px;
          background: rgba(255,255,255,0.08);
          backdrop-filter: blur(12px);
          border: 1px solid rgba(255,255,255,0.12);
          border-radius: var(--radius-full);
          color: var(--text-primary);
          font-size: 13px;
          cursor: pointer;
          transition: all 0.2s;
        }
        .login-theme-btn:hover {
          background: rgba(255,255,255,0.15);
          transform: translateY(-1px);
          box-shadow: var(--shadow-md);
        }
        .login-bg {
          position: absolute;
          inset: 0;
          overflow: hidden;
        }
        .login-orb {
          position: absolute;
          border-radius: 50%;
          filter: blur(80px);
          opacity: 0.3;
        }
        .orb-1 { width: 400px; height: 400px; background: #6366f1; top: -100px; right: -100px; }
        .orb-2 { width: 300px; height: 300px; background: #8b5cf6; bottom: -50px; left: -50px; }
        .orb-3 { width: 200px; height: 200px; background: #06b6d4; top: 50%; left: 40%; }
        .login-container {
          display: flex;
          max-width: 960px;
          width: 100%;
          background: var(--bg-secondary);
          border-radius: var(--radius-xl);
          border: 1px solid var(--border);
          overflow: hidden;
          box-shadow: var(--shadow-lg);
          position: relative;
          z-index: 1;
          animation: scaleIn 0.5s ease-out;
        }
        .login-left {
          flex: 1;
          padding: 48px 40px;
          background: var(--gradient-primary);
          display: flex;
          flex-direction: column;
          justify-content: center;
          position: relative;
          overflow: hidden;
        }
        .login-left::before {
          content: '';
          position: absolute;
          top: -50%;
          right: -50%;
          width: 200%;
          height: 200%;
          background: radial-gradient(circle, rgba(255,255,255,0.08) 0%, transparent 50%);
        }
        .login-brand { position: relative; z-index: 1; margin-bottom: 36px; }
        .login-logo {
          display: inline-flex;
          align-items: center;
          justify-content: center;
          width: 48px; height: 48px;
          background: rgba(255,255,255,0.2);
          border-radius: 14px;
          font-size: 18px;
          font-weight: 800;
          color: white;
          margin-bottom: 20px;
          backdrop-filter: blur(8px);
        }
        .login-brand h1 {
          font-size: 28px;
          font-weight: 700;
          color: white;
          line-height: 1.3;
          margin-bottom: 12px;
        }
        .login-brand p {
          color: rgba(255,255,255,0.7);
          font-size: 14px;
          line-height: 1.6;
        }
        .login-features { display: flex; flex-direction: column; gap: 12px; position: relative; z-index: 1; }
        .login-feature {
          display: flex; align-items: center; gap: 14px;
          padding: 12px 14px;
          background: rgba(255,255,255,0.08);
          border-radius: var(--radius-md);
          color: white;
          backdrop-filter: blur(4px);
          animation: slideInUp 0.4s ease-out backwards;
        }
        .login-feature-icon {
          width: 38px; height: 38px;
          background: rgba(255,255,255,0.15);
          border-radius: var(--radius-sm);
          display: flex; align-items: center; justify-content: center;
          flex-shrink: 0;
        }
        .login-right {
          flex: 1;
          padding: 48px 40px;
          display: flex;
          flex-direction: column;
          justify-content: center;
        }
        .login-form-header { margin-bottom: 32px; }
        .login-form-header h2 { font-size: 24px; font-weight: 700; color: var(--text-primary); margin-bottom: 6px; }
        .login-form-header p { color: var(--text-secondary); font-size: 14px; }
        .login-form { margin-bottom: 28px; }
        .login-submit {
          width: 100%;
          padding: 12px;
          font-size: 15px;
          justify-content: center;
          margin-top: 8px;
        }
        .login-demo { border-top: 1px solid var(--border); padding-top: 20px; }
        .login-demo-title {
          display: flex; justify-content: space-between; align-items: center;
          margin-bottom: 12px;
          font-size: 13px; font-weight: 600; color: var(--text-secondary);
        }
        .login-demo-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 8px; }
        .login-demo-card {
          display: flex; flex-direction: column; align-items: center; gap: 6px;
          padding: 12px 8px;
          background: var(--bg-elevated);
          border: 1px solid var(--border);
          border-radius: var(--radius-md);
          cursor: pointer;
          transition: all 0.2s;
          text-align: center;
        }
        .login-demo-card:hover {
          background: var(--bg-hover);
          border-color: var(--border-hover);
          transform: translateY(-2px);
        }
        @media (max-width: 768px) {
          .login-container { flex-direction: column; }
          .login-left { padding: 32px 24px; }
          .login-right { padding: 32px 24px; }
          .login-demo-grid { grid-template-columns: 1fr; }
        }
      `}</style>
    </div>
  );
}
