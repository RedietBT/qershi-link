import React from 'react';
import { BrowserRouter } from 'react-router-dom';
import { ThemeProvider } from './common/context/ThemeContext';
import { AppRoutes } from './routes/AppRoutes';

function App() {
  return (
    <BrowserRouter>
      <ThemeProvider>
        <AppRoutes />
      </ThemeProvider>
    </BrowserRouter>
  );
}

export default App;