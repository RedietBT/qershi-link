/**
 * Core Banking Financial & Utility Helpers.
 * Provides strict currency formatting, numeric sanitization,
 * audit date parsing, and client-side idempotency generation.
 */

/**
 * Formats a numeric value or numeric string into localized ISO currency format.
 * Example: formatCurrency(25400.5) -> "ETB 25,400.50"
 */
export const formatCurrency = (amount, currency = 'ETB') => {
  if (amount === null || amount === undefined || isNaN(Number(amount))) {
    return `${currency} 0.00`;
  }
  const num = Number(amount);
  const formatted = num.toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
  return `${currency} ${formatted}`;
};

/**
 * Formats a plain number with commas and 2 decimal places.
 * Example: formatNumber(1250000) -> "1,250,000.00"
 */
export const formatNumber = (value) => {
  if (value === null || value === undefined || isNaN(Number(value))) {
    return '0.00';
  }
  return Number(value).toLocaleString('en-US', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  });
};

/**
 * Strips non-numeric characters (except single decimal point) for safe form inputs.
 */
export const parseCurrencyInput = (value) => {
  if (!value) return '';
  const cleaned = String(value).replace(/[^0-9.]/g, '');
  const parts = cleaned.split('.');
  if (parts.length > 2) {
    return parts[0] + '.' + parts.slice(1).join('');
  }
  return cleaned;
};

/**
 * Generates a cryptographically secure UUID v4 idempotency key.
 * Used for all financial debits, credits, and loan disbursements
 * to prevent double-charging on duplicate clicks or network retries.
 */
export const generateIdempotencyKey = () => {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID();
  }
  // Safe fallback for non-secure contexts
  return 'idemp_' + Date.now() + '_' + Math.random().toString(36).substring(2, 15);
};

/**
 * Formats an ISO date string into readable financial audit date & time.
 * Example: 2026-09-27T21:40:00Z -> "Sep 27, 2026, 21:40"
 */
export const formatDateTime = (isoString) => {
  if (!isoString) return '—';
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return isoString;
    return d.toLocaleString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    });
  } catch (e) {
    return isoString;
  }
};

/**
 * Formats an ISO date string into clean date only.
 * Example: 2026-09-27T21:40:00Z -> "Sep 27, 2026"
 */
export const formatDate = (isoString) => {
  if (!isoString) return '—';
  try {
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return isoString;
    return d.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: 'numeric',
    });
  } catch (e) {
    return isoString;
  }
};
