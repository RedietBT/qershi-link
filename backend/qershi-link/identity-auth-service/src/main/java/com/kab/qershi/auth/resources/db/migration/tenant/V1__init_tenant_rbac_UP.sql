-- 1. Create Roles Table
CREATE TABLE roles (
    role_id UUID PRIMARY KEY,
    role_name VARCHAR(50) NOT NULL,
    is_system_defined BOOLEAN DEFAULT FALSE, --If TRUE, the role (like ADMIN) cannot be deleted or modified by local users.
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 2. Create Role Permissions Link Table
CREATE TABLE role_permissions (
    role_id UUID NOT NULL,
    permission_code VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission_code),
    CONSTRAINT fk_permissions_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);

-- 3. Create User Roles Bridge Table
CREATE TABLE user_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_local_role FOREIGN KEY (role_id) REFERENCES roles(role_id) ON DELETE CASCADE
);