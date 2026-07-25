-- V4: Multi-tenant upgrade for user_roles
-- Adds sacco_id to ensure role assignments are tenant-scoped

-- 1. Add sacco_id column
ALTER TABLE master_schema.user_roles ADD COLUMN sacco_id UUID;

-- 2. NOTE: If you have existing data, you must populate sacco_id here
-- before adding the NOT NULL constraint.
-- If your table is empty, you can skip the UPDATE.
-- UPDATE master_schema.user_roles SET sacco_id = 'YOUR_DEFAULT_SACCO_UUID';

-- 3. Now make it mandatory
ALTER TABLE master_schema.user_roles ALTER COLUMN sacco_id SET NOT NULL;

-- 4. Redefine the primary key to include the new tenant scope
ALTER TABLE master_schema.user_roles DROP CONSTRAINT user_roles_pkey;
ALTER TABLE master_schema.user_roles ADD PRIMARY KEY (user_id, role_id, sacco_id);

-- 5. Add foreign key constraint
ALTER TABLE master_schema.user_roles
ADD CONSTRAINT fk_user_roles_sacco FOREIGN KEY (sacco_id)
REFERENCES master_schema.sacco_registry(sacco_id) ON DELETE CASCADE;