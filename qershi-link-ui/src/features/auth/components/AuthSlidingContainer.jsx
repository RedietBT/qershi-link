import React, { useState } from 'react';
import { LoginForm } from './LoginForm';
import { SaccoOnboardingForm } from '../../superadmin/components/SaccoOnboardingForm';

export const AuthSlidingContainer = ({ onLoginSuccess, onOnboardSuccess }) => {
  const [isRightPanelActive, setIsRightPanelActive] = useState(false);

  return (
    <div className="min-h-screen flex items-center justify-center p-4 bg-[var(--bdae-bg)] text-[var(--bdae-text-primary)] transition-colors duration-300">
      
      {/* Central Double-Sided Sliding Card */}
      <div className="relative overflow-hidden w-full max-w-4xl min-h-[540px] rounded-3xl shadow-2xl bg-[var(--bdae-surface)] border border-[var(--bdae-border)] flex">
        
        {/* ==================== FORM 1: SIGN IN (LEFT HALF) ==================== */}
        <div 
          className={`w-full md:w-1/2 p-8 md:p-10 flex flex-col justify-center transition-all duration-700 ease-in-out ${
            isRightPanelActive ? 'opacity-0 pointer-events-none translate-x-full' : 'opacity-100 z-10'
          }`}
        >
          <LoginForm onSuccess={onLoginSuccess} />
        </div>

        {/* ==================== FORM 2: SACCO ONBOARDING (RIGHT HALF) ==================== */}
        <div 
          className={`w-full md:w-1/2 p-8 md:p-10 flex flex-col justify-center transition-all duration-700 ease-in-out absolute right-0 top-0 bottom-0 ${
            isRightPanelActive ? 'opacity-100 z-10 translate-x-0' : 'opacity-0 pointer-events-none -translate-x-full'
          }`}
        >
          <SaccoOnboardingForm onSuccess={onOnboardSuccess} />
        </div>

        {/* ==================== OVERLAY SLIDING PANEL ==================== */}
        <div 
          className={`hidden md:block absolute top-0 left-1/2 w-1/2 h-full overflow-hidden transition-transform duration-700 ease-in-out z-20 ${
            isRightPanelActive ? '-translate-x-full' : 'translate-x-0'
          }`}
        >
          {/* Overlay Inner Container */}
          <div 
            className={`w-[200%] h-full text-white relative -left-full transition-transform duration-700 ease-in-out flex ${
              isRightPanelActive ? 'translate-x-1/2' : 'translate-x-0'
            }`}
            style={{
              background: `linear-gradient(135deg, var(--bdae-primary), var(--bdae-secondary))`
            }}
          >
            
            {/* Left Overlay Panel (Shows when Onboarding form is active) */}
            <div className="w-1/2 h-full p-10 flex flex-col items-center justify-center text-center space-y-4">
              <h2 className="text-3xl font-extrabold tracking-tight">Welcome Back!</h2>
              <p className="text-xs text-white/90 max-w-xs leading-relaxed font-medium">
                To keep connected with Qershi-Link Core Banking, sign in with your SACCO credentials.
              </p>
              <button
                onClick={() => setIsRightPanelActive(false)}
                className="px-8 py-3 rounded-full border-2 border-white text-white hover:bg-white hover:text-[var(--bdae-primary)] text-xs font-bold uppercase tracking-wider transition-all duration-300 shadow-md hover:scale-105 mt-2"
              >
                SIGN IN
              </button>
            </div>

            {/* Right Overlay Panel (Shows when Sign In form is active) */}
            <div className="w-1/2 h-full p-10 flex flex-col items-center justify-center text-center space-y-4">
              <h2 className="text-3xl font-extrabold tracking-tight">Get Started!</h2>
              <p className="text-xs text-white/90 max-w-xs leading-relaxed font-medium">
                Enter SACCO institution details and onboard a new tenant to the Qershi-Link platform.
              </p>
              <button
                onClick={() => setIsRightPanelActive(true)}
                className="px-8 py-3 rounded-full border-2 border-white text-white hover:bg-white hover:text-[var(--bdae-primary)] text-xs font-bold uppercase tracking-wider transition-all duration-300 shadow-md hover:scale-105 mt-2"
              >
                ONBOARD SACCO
              </button>
            </div>

          </div>
        </div>

        {/* Mobile Toggle Switcher */}
        <div className="md:hidden absolute bottom-3 left-0 right-0 text-center">
          <button
            onClick={() => setIsRightPanelActive(!isRightPanelActive)}
            className="text-xs text-[var(--bdae-secondary)] hover:underline font-bold"
          >
            {isRightPanelActive ? 'Switch to Sign In' : 'Switch to SACCO Onboarding'}
          </button>
        </div>

      </div>

    </div>
  );
};
