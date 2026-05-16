import { createContext, useContext, useState, useCallback } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const saved = sessionStorage.getItem('cms_user');
    return saved ? JSON.parse(saved) : null;
  });

  const login = useCallback((userData) => {
    setUser(userData);
    sessionStorage.setItem('cms_user', JSON.stringify(userData));
  }, []);

  const logout = useCallback(() => {
    setUser(null);
    sessionStorage.removeItem('cms_user');
  }, []);

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
}
