/**
 * Real-Time Input Sanitization & Format Utilities for Qershi-Link Core Banking UI.
 */

/**
 * Sanitizes phone input into standard Ethiopian MSISDN format (+2519...).
 */
export const sanitizeMsisdn = (input = '') => {
  let cleaned = input.trim().replaceAll(/\s+/g, '');
  
  if (/^09\d{8}$/.test(cleaned)) {
    return '+251' + cleaned.slice(1);
  }
  if (/^9\d{8}$/.test(cleaned)) {
    return '+251' + cleaned;
  }
  if (/^2519\d{8}$/.test(cleaned)) {
    return '+' + cleaned;
  }
  if (/^\+2519\d{8}$/.test(cleaned)) {
    return cleaned;
  }
  return cleaned;
};

/**
 * Validates if the given MSISDN strictly matches +2519XXXXXXXX (13 chars total).
 */
export const isValidMsisdn = (msisdn = '') => {
  return /^\+2519\d{8}$/.test(msisdn);
};

/**
 * Validates SACCO Name: Must be at least 2 characters and MUST NOT start with a digit.
 */
export const isValidSaccoName = (name = '') => {
  const trimmed = name.trim();
  if (trimmed.length < 2) return false;
  return !/^\d/.test(trimmed);
};

/**
 * Sanitizes text inputs by collapsing multiple spaces into a single space.
 */
export const sanitizeText = (input = '') => {
  return input.replaceAll(/\s+/g, ' ');
};

/**
 * Enforces non-negative numbers for monetary and share requirement inputs.
 */
export const sanitizePositiveNumber = (input) => {
  const val = Number(input);
  return isNaN(val) || val < 0 ? 0 : val;
};
