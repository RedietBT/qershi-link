import React from 'react';

/**
 * 404 Page Not Found Component
 */
export const NotFoundPage = () => {
  return (
    <div className="min-h-screen flex items-center justify-center p-6 bg-[var(--bdae-bg)] text-[var(--bdae-text-primary)]">
      <div className="bdae-card p-8 max-w-md w-full text-center space-y-4 shadow-2xl border border-[var(--bdae-border)]">
        <h1 className="text-4xl font-extrabold text-[var(--bdae-secondary)]">404</h1>
        <h2 className="text-lg font-bold">Page Not Found</h2>
        <p className="text-xs text-[var(--bdae-text-secondary)]">
          The page route you are looking for does not exist on Qershi-Link Platform.
        </p>
        <a href="/dashboard" className="bdae-btn-primary block w-full py-2.5 text-xs font-bold rounded-xl">
          Return to Dashboard
        </a>
      </div>
    </div>
  );
};
