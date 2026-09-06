-- Attendance ERP V2
-- PostgreSQL migration.
--
-- Business rules:
--   * Academic Session is no longer supplied by faculty/student.
--   * Attendance belongs to a server-derived semester.
--   * Default semester calendar: Jul-Dec = S1, Jan-Jun = S2.
--   * Domain admin can change semester start month and retention months.
--   * Six months means the previous semester expires when the next semester starts.

BEGIN;

ALTER TABLE erp_attendance_record
    ADD COLUMN IF NOT EXISTS semester_key VARCHAR(20);

-- Backfill historical attendance using the attendance date.
UPDATE erp_attendance_record
SET semester_key = CASE
    WHEN EXTRACT(MONTH FROM attendance_date) >= 7
        THEN EXTRACT(YEAR FROM attendance_date)::int || '-S1'
    ELSE (EXTRACT(YEAR FROM attendance_date)::int - 1) || '-S2'
END
WHERE semester_key IS NULL;

-- Old aggregates were session-scoped and cannot reliably represent two semesters.
-- Rebuild them from the attendance records after migration.
ALTER TABLE erp_attendance_aggregate
    ADD COLUMN IF NOT EXISTS semester_key VARCHAR(20);

DELETE FROM erp_attendance_aggregate;

UPDATE erp_attendance_aggregate
SET semester_key = 'LEGACY'
WHERE semester_key IS NULL;

-- Remove old unique constraints. This module has one business unique constraint
-- on each table in the previous version.
DO $$
DECLARE
    constraint_name TEXT;
BEGIN
    FOR constraint_name IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        WHERE tc.table_schema = current_schema()
          AND tc.table_name = 'erp_attendance_record'
          AND tc.constraint_type = 'UNIQUE'
    LOOP
        EXECUTE format('ALTER TABLE erp_attendance_record DROP CONSTRAINT IF EXISTS %I', constraint_name);
    END LOOP;

    FOR constraint_name IN
        SELECT tc.constraint_name
        FROM information_schema.table_constraints tc
        WHERE tc.table_schema = current_schema()
          AND tc.table_name = 'erp_attendance_aggregate'
          AND tc.constraint_type = 'UNIQUE'
    LOOP
        EXECUTE format('ALTER TABLE erp_attendance_aggregate DROP CONSTRAINT IF EXISTS %I', constraint_name);
    END LOOP;
END $$;

ALTER TABLE erp_attendance_record
    ALTER COLUMN semester_key SET NOT NULL;

ALTER TABLE erp_attendance_aggregate
    ALTER COLUMN semester_key SET NOT NULL;

ALTER TABLE erp_attendance_record
    DROP COLUMN IF EXISTS academic_session;

ALTER TABLE erp_attendance_aggregate
    DROP COLUMN IF EXISTS academic_session;

ALTER TABLE erp_attendance_record
    ADD CONSTRAINT uk_attendance_student_subject_date_period_semester
    UNIQUE (student_id, subject, attendance_date, period_number, semester_key);

ALTER TABLE erp_attendance_aggregate
    ADD CONSTRAINT uk_attendance_aggregate_student_subject_semester
    UNIQUE (student_id, subject, semester_key);

CREATE INDEX IF NOT EXISTS idx_attendance_domain_semester
    ON erp_attendance_record(domain, semester_key);

CREATE INDEX IF NOT EXISTS idx_attendance_aggregate_domain_semester
    ON erp_attendance_aggregate(domain, semester_key);

-- Rebuild aggregate data from authoritative attendance records.
INSERT INTO erp_attendance_aggregate
    (domain, semester_key, subject, total_classes, present_count, absent_count, student_id)
SELECT
    domain,
    semester_key,
    subject,
    COUNT(*) AS total_classes,
    COUNT(*) FILTER (WHERE status = 'PRESENT') AS present_count,
    COUNT(*) FILTER (WHERE status = 'ABSENT') AS absent_count,
    student_id
FROM erp_attendance_record
GROUP BY domain, semester_key, subject, student_id;

CREATE TABLE IF NOT EXISTS erp_attendance_retention_policy (
    id BIGSERIAL PRIMARY KEY,
    domain VARCHAR(100) NOT NULL,
    retention_months INTEGER NOT NULL DEFAULT 6,
    semester_start_month INTEGER NOT NULL DEFAULT 7,
    CONSTRAINT uk_attendance_retention_domain UNIQUE (domain),
    CONSTRAINT ck_attendance_retention_months CHECK (retention_months BETWEEN 6 AND 120),
    CONSTRAINT ck_attendance_retention_semester_month CHECK (semester_start_month BETWEEN 1 AND 12)
);

COMMIT;
