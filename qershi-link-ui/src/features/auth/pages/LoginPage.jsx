import React from 'react';
import { AuthSlidingContainer } from '../components/AuthSlidingContainer';

export const LoginPage = ({ onLoginSuccess, onOnboardSuccess }) => {
  return (
    <AuthSlidingContainer 
      onLoginSuccess={onLoginSuccess} 
      onOnboardSuccess={onOnboardSuccess} 
    />
  );
};
