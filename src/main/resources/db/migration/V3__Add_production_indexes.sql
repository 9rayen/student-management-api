-- Production-specific optimizations
-- Note: email index already exists from V1, so we skip it here
CREATE INDEX IF NOT EXISTS idx_student_name ON student(name);
CREATE INDEX IF NOT EXISTS idx_student_dob ON student(dob);
