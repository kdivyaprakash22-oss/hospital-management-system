-- Run this migration against an existing database after checking for duplicate active admissions.
-- It stops safely with a clear error if the current data violates the intended rule.
DO $$
BEGIN
  IF EXISTS (
    SELECT patient_id
    FROM admissions
    WHERE status = 'ADMITTED'
    GROUP BY patient_id
    HAVING COUNT(*) > 1
  ) THEN
    RAISE EXCEPTION 'Migration stopped: one or more patients have multiple active admissions. Resolve those records, then rerun this migration.';
  END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_active_admission_patient
  ON admissions(patient_id)
  WHERE status = 'ADMITTED';
