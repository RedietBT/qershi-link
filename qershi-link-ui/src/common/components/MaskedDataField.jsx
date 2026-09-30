import React, { useState } from 'react';
import { Eye, EyeOff } from 'lucide-react';
import { maskPhone, maskAccountNumber, maskNationalId } from '../utils/masking';

/**
 * Reusable Masked Data Field Component
 * Supports phone, account, nationalId types with optional peek/reveal toggle.
 */
export const MaskedDataField = ({
  value,
  type = 'phone', // 'phone' | 'account' | 'nationalId' | 'custom'
  allowReveal = false,
  className = '',
}) => {
  const [isRevealed, setIsRevealed] = useState(false);

  if (!value) return <span className="text-[var(--bdae-text-secondary)]">—</span>;

  let maskedValue = value;
  if (type === 'phone') {
    maskedValue = maskPhone(value);
  } else if (type === 'account') {
    maskedValue = maskAccountNumber(value);
  } else if (type === 'nationalId') {
    maskedValue = maskNationalId(value);
  }

  return (
    <span className={`inline-flex items-center gap-1.5 font-mono ${className}`}>
      <span>{isRevealed ? value : maskedValue}</span>
      {allowReveal && (
        <button
          type="button"
          onClick={(e) => {
            e.stopPropagation();
            setIsRevealed(!isRevealed);
          }}
          className="text-[var(--bdae-text-secondary)] hover:text-[var(--bdae-primary)] transition-colors p-0.5 rounded focus:outline-none"
          title={isRevealed ? 'Hide sensitive data' : 'Reveal unmasked data'}
        >
          {isRevealed ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
        </button>
      )}
    </span>
  );
};
