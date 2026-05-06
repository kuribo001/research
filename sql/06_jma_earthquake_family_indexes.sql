-- =========================================================
-- JMA Earthquake Feed - Family-specific indexes
-- PostgreSQL
-- =========================================================

-- =========================================================
-- 1. Fast lookup by logical event lifecycle
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_event_lifecycle
    ON jma_earthquake_event (event_id, issued_at DESC, message_code);

-- =========================================================
-- 2. EEW area queries
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eew_scope_area
    ON jma_eew_forecast_area (info_scope, area_code);

-- =========================================================
-- 3. Station lookup by event and type
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_station_event_type
    ON jma_earthquake_station_intensity (earthquake_event_id, station_type);

-- =========================================================
-- 4. Municipality lookup by event and source
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_municipality_event_source
    ON jma_earthquake_municipality_intensity (earthquake_event_id, source_type);

-- =========================================================
-- 5. Notice / commentary drill-down
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_notice_kind
    ON jma_earthquake_notice_item (notice_kind);

-- =========================================================
-- 6. Earthquake count timeline lookup
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_count_item_time
    ON jma_earthquake_count_item (start_time, end_time);

-- =========================================================
-- 7. Special information block lookup
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_special_text_block_type
    ON jma_earthquake_special_text_block (block_type);

-- =========================================================
-- 8. Latest snapshot by current message family
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_eq_latest_snapshot_family
    ON jma_earthquake_latest_event_snapshot (family_name, issued_at DESC);
