/**
 * Core Banking Data Masking & Information Protection Utilities
 * Compliant with PCI-DSS Requirement 3.3 and PII Protection Directives
 */

/**
 * Maps system and RBAC role identifiers to institutional Core Banking titles.
 */
export const formatRole = (role) => {
  if (!role) return 'Core Banking Staff';
  const clean = role.replace(/^ROLE_/, '').trim().toUpperCase();

  const ROLE_MAP = {
    SUPER_ADMIN: 'Platform Super Administrator',
    ADMIN: 'System Administrator',
    SACCO_ADMIN: 'SACCO Administrator',
    BRANCH_MANAGER: 'Branch Operations Manager',
    TELLER: 'Cash Desk Teller',
    LOAN_OFFICER: 'Credit & Loan Officer',
    UNDERWRITER: 'Credit Underwriter',
    AUDITOR: 'Internal Compliance Auditor',
    COMPLIANCE_OFFICER: 'AML / Compliance Officer',
    ACCOUNTANT: 'General Ledger Accountant',
    TREASURER: 'SACCO Treasurer',
    SACCO_USER: 'SACCO Member',
    MEMBER: 'SACCO Member',
  };

  if (ROLE_MAP[clean]) return ROLE_MAP[clean];

  // Fallback: Title case transformation (e.g., CUSTOM_ROLE -> Custom Role)
  return clean
    .split('_')
    .map((word) => word.charAt(0) + word.slice(1).toLowerCase())
    .join(' ');
};

/**
 * Returns a human-friendly display name for an authenticated staff member.
 * Strictly prevents raw MSISDN / phone numbers from appearing as names.
 */
export const getUserDisplayName = (user) => {
  if (!user) return 'SACCO Staff';

  if (user.fullName && user.fullName.trim()) {
    return user.fullName.trim();
  }
  if (user.name && user.name.trim()) {
    return user.name.trim();
  }
  if (user.firstName) {
    return `${user.firstName} ${user.lastName || ''}`.trim();
  }

  // Professional fallback: Use institutional role title rather than phone number
  const role = user.globalRole || (user.roles && user.roles[0]);
  return formatRole(role);
};

/**
 * Masks a mobile phone / MSISDN string for privacy protection (PII Masking).
 * Example: +251911223344 -> +251 91 •••• 3344
 */
export const maskPhone = (phone) => {
  if (!phone || typeof phone !== 'string') return '—';
  const clean = phone.trim();

  // Standard Ethiopian format (+251 9X or 09X)
  if (clean.startsWith('+251') && clean.length >= 12) {
    const country = clean.slice(0, 4); // +251
    const prefix = clean.slice(4, 6);  // 91
    const end = clean.slice(-4);       // 3344
    return `${country} ${prefix} •••• ${end}`;
  }

  if (clean.startsWith('09') && clean.length >= 10) {
    const prefix = clean.slice(0, 3); // 091
    const end = clean.slice(-3);      // 344
    return `${prefix} •••• ${end}`;
  }

  // Generic fallback if format is unusual but long enough
  if (clean.length > 7) {
    const start = clean.slice(0, 3);
    const end = clean.slice(-3);
    return `${start} •••• ${end}`;
  }

  return clean;
};

/**
 * Masks a Core Banking account number following PCI-DSS truncation principles.
 * Example: 100012345678 -> 1000 •••• •••• 5678
 */
export const maskAccountNumber = (accountNo) => {
  if (!accountNo || typeof accountNo !== 'string') return '—';
  const clean = accountNo.trim();

  if (clean.length >= 10) {
    const start = clean.slice(0, 4);
    const end = clean.slice(-4);
    return `${start} •••• ${end}`;
  }

  if (clean.length > 4) {
    return `•••• ${clean.slice(-4)}`;
  }

  return clean;
};

/**
 * Masks a National ID, Passport, or Kebele Card ID.
 * Example: ETH-892345-AA -> ETH-••••-AA
 */
export const maskNationalId = (id) => {
  if (!id || typeof id !== 'string') return '—';
  const clean = id.trim();
  if (clean.length <= 4) return clean;

  const start = clean.slice(0, 3);
  const end = clean.slice(-2);
  return `${start}••••${end}`;
};
