-- =========================================================================
-- V6: Add Employee ID and External Employee ID to Member Employments
-- Supports hybrid Employee ID (auto-generated CBS ID & external HRMS ID).
-- =========================================================================

ALTER TABLE member_employments
    ADD COLUMN employee_id VARCHAR(50),
    ADD COLUMN external_employee_id VARCHAR(100);

CREATE INDEX idx_me_employee_id ON member_employments(employee_id);
CREATE INDEX idx_me_external_employee_id ON member_employments(external_employee_id);
