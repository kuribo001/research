-- =========================================================
-- MIGRATION PART 1
-- add coordinate column
-- =========================================================

ALTER TABLE jma_volcano_geometry
    ADD COLUMN IF NOT EXISTS vol_lat DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS vol_lon DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS vol_ele INTEGER,
    ADD COLUMN IF NOT EXISTS crater_lat DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS crater_lon DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS crater_ele DOUBLE PRECISION;

ALTER TABLE jma_volcano_master
    ADD COLUMN IF NOT EXISTS vol_lat DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS vol_lon DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS vol_ele INTEGER;
