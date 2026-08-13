import React from 'react';
import { ThemeProvider } from './common/context/ThemeContext';
import { useAuthStore } from './common/store/useAuthStore';
import { LoginPage } from './features/auth/pages/LoginPage';
import { LogOut, CheckCircle2 } from 'lucide-react';

function AppContent() {
  const user = useAuthStore((state) => state.user);
  const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
  const logout = useAuthStore((state) => state.logout);

  if (!isAuthenticated) {
    return (
      <LoginPage 
        onLoginSuccess={(data) => console.log('Login Success:', data)} 
        onOnboardSuccess={(data) => console.log('Onboarding Success:', data)}
      />
    );
  }

  return (
    <div className="min-h-screen bg-[var(--bdae-bg)] text-[var(--bdae-text-primary)] p-8 max-w-md mx-auto space-y-6 flex flex-col justify-center">
      <div 
        className="bdae-card p-6 text-white rounded-2xl shadow-xl space-y-4"
        style={{ background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))` }}
      >
        <div className="flex items-center space-x-2">
          <CheckCircle2 className="w-6 h-6 text-white" />
          <h1 className="text-xl font-bold">Successfully Authenticated</h1>
        </div>
        
        <div className="text-xs space-y-1 bg-white/10 p-3 rounded-xl font-mono">
          <p><strong>MSISDN:</strong> {user?.msisdn}</p>
          <p><strong>User ID:</strong> {user?.userId}</p>
          <p><strong>Tenant:</strong> {user?.saccoId || 'Default'}</p>
        </div>

        <button
          onClick={logout}
          className="w-full py-2.5 bg-white text-black hover:bg-white/90 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition-all shadow-md"
        >
          <LogOut className="w-4 h-4" />
          <span>Sign Out</span>
        </button>
      </div>
    </div>
  );
}

function App() {
  return (
    <ThemeProvider>
      <AppContent />
    </ThemeProvider>
  );
}

export default App;