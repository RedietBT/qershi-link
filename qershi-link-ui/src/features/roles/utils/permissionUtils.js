/**
 * Helper utility to safely extract clean Permission Display Names from PermissionEntity or String/Object representations.
 */
export const getPermissionDisplayName = (perm) => {
  if (!perm) return 'UNKNOWN_PERMISSION';
  if (typeof perm === 'string') return perm;
  if (perm.permissionName) return perm.permissionName;
  if (perm.name) return perm.name;
  if (perm.authority) return perm.authority;
  
  // JPA Entity mapping: resource="ACCOUNT", action="OPEN" -> ACCOUNT_OPEN
  if (perm.resource && perm.action) {
    return `${perm.resource}_${perm.action}`;
  }

  if (perm.permission) {
    if (typeof perm.permission === 'string') return perm.permission;
    if (perm.permission.permissionName) return perm.permission.permissionName;
    if (perm.permission.name) return perm.permission.name;
    if (perm.permission.resource && perm.permission.action) {
      return `${perm.permission.resource}_${perm.permission.action}`;
    }
  }

  return 'PERMISSION_ITEM';
};

/**
 * Extracts permission description or fallback.
 */
export const getPermissionDescription = (perm) => {
  if (!perm || typeof perm === 'string') return '';
  return perm.description || perm.permission?.description || '';
};

/**
 * Extracts resource category for a permission item.
 */
export const getPermissionResourceCategory = (perm) => {
  if (!perm) return 'GENERAL';
  if (typeof perm === 'object' && perm.resource) return perm.resource.toUpperCase();
  if (typeof perm === 'object' && perm.permission?.resource) return perm.permission.resource.toUpperCase();

  const name = getPermissionDisplayName(perm);
  if (name.includes('_')) {
    return name.split('_')[0].toUpperCase();
  }
  return 'GENERAL';
};

/**
 * Groups an array of permission objects by their resource category.
 */
export const groupPermissionsByResource = (permissions = []) => {
  const groups = {};

  permissions.forEach((perm) => {
    const category = getPermissionResourceCategory(perm);
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(perm);
  });

  return groups;
};
