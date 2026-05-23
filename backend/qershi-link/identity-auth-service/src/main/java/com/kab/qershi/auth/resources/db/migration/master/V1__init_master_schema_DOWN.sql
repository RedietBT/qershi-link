DROP INDEX IF EXISTS master_schema.idx_sacco_parent_union;
DROP INDEX IF EXISTS master_schema.idx_users_msisdn;
DROP TABLE IF EXISTS master_schema.users;
DROP TABLE IF EXISTS master_schema.sacco_registry;

DROP TYPE IF EXISTS master_schema_user_status;
DROP TYPE IF EXISTS master_schema_global_role;
DROP TYPE IF EXISTS master_schema_sacco_status;