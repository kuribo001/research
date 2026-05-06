-- =========================================================
-- MIGRATION PART 2
-- Performance indexes for CZML & API
-- =========================================================

-- Wind layers: fast ordered fetch by altitude
CREATE INDEX IF NOT EXISTS idx_wind_event_altitude
    ON jma_volcano_wind_layer (event_id, altitude_m);

-- Ash forecasts: fast timeline fetch
CREATE INDEX IF NOT EXISTS idx_ash_forecast_event_time
    ON jma_ash_forecast (event_id, start_time);

-- Ash items: optional filter by kind
CREATE INDEX IF NOT EXISTS idx_ash_item_forecast_kind
    ON jma_ash_forecast_item (forecast_id, kind_name);
