import React, { createContext, useContext } from 'react';

const ThemeContext = createContext(undefined);

export const ThemeProvider = ({ children }) => {
  const setTheme = (config) => {
    const root = document.documentElement;
    if (config.primary) root.style.setProperty('--bdae-primary', config.primary);
    if (config.secondary) root.style.setProperty('--bdae-secondary', config.secondary);
    if (config.tertiary) root.style.setProperty('--bdae-tertiary', config.tertiary);
  };

  const resetTheme = () => {
    document.documentElement.style.removeProperty('--bdae-primary');
    document.documentElement.style.removeProperty('--bdae-secondary');
    document.documentElement.style.removeProperty('--bdae-tertiary');
  };

  return (
    <ThemeContext.Provider value={{ setTheme, resetTheme }}>
      {children}
    </ThemeContext.Provider>
  );
};

export const useTheme = () => {
  const context = useContext(ThemeContext);
  if (!context) throw new Error('useTheme must be used within a ThemeProvider');
  return context;
};
