-- =========================================================
-- JMA Volcano Feed - Initial Database Schema
-- PostgreSQL
-- =========================================================

-- =========================================================
-- 1. Atom Feed Entry (eqvol / eqvol_l)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_feed_entry (
    entry_id TEXT PRIMARY KEY,
    title TEXT,
    content TEXT,
    author_name TEXT,
    detail_url TEXT NOT NULL,
    message_code TEXT,
    status TEXT DEFAULT 'NEW',
    s3_status BOOLEAN DEFAULT false,
    feed_updated_at TIMESTAMPTZ,
    crawled_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_feed_message_code
    ON jma_feed_entry (message_code);

CREATE INDEX IF NOT EXISTS idx_feed_updated_at
    ON jma_feed_entry (feed_updated_at);

-- =========================================================
-- 2. Volcano Event (core / head)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_event (
    event_id TEXT PRIMARY KEY,
    entry_id TEXT UNIQUE,
    info_kind TEXT,
    info_type TEXT,
    issued_at TIMESTAMPTZ,
    event_time TIMESTAMPTZ,
    event_time_utc TIMESTAMPTZ,
    volcano_code TEXT,
    volcano_name TEXT,
    alert_kind_code TEXT,
    alert_kind_name TEXT,
    last_alert_kind_code TEXT,
    last_alert_kind_name TEXT,
    created_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_event_entry
        FOREIGN KEY (entry_id)
        REFERENCES jma_feed_entry(entry_id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_event_volcano_code
    ON jma_volcano_event (volcano_code);

CREATE INDEX IF NOT EXISTS idx_event_time
    ON jma_volcano_event (event_time);

-- =========================================================
-- 3. Volcano Geometry / Crater
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_geometry (
    event_id TEXT PRIMARY KEY,
    volcano_code TEXT,
    volcano_coord TEXT,
    crater_name TEXT,
    crater_coord TEXT,
    CONSTRAINT fk_geometry_event
        FOREIGN KEY (event_id)
        REFERENCES jma_volcano_event(event_id)
        ON DELETE CASCADE
);

-- =========================================================
-- 4. Volcano Observation (plume / wind / other)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_observation (
    event_id TEXT PRIMARY KEY,
    plume_direction_text TEXT,
    plume_height_above_crater_m INTEGER,
    plume_height_asl_ft INTEGER,
    wind_observation_time TIMESTAMPTZ,
    other_observation TEXT,
    CONSTRAINT fk_observation_event
        FOREIGN KEY (event_id)
        REFERENCES jma_volcano_event(event_id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS jma_volcano_wind_layer (
    id BIGSERIAL PRIMARY KEY,
    event_id TEXT NOT NULL,
    altitude_m INTEGER,
    direction TEXT,
    speed_ms INTEGER,
    CONSTRAINT fk_wind_event
        FOREIGN KEY (event_id)
        REFERENCES jma_volcano_event(event_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_wind_event_id
    ON jma_volcano_wind_layer (event_id);

-- =========================================================
-- 5. Ash Forecast
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_ash_forecast (
    id BIGSERIAL PRIMARY KEY,
    event_id TEXT NOT NULL,
    forecast_type TEXT,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    CONSTRAINT fk_ash_forecast_event
        FOREIGN KEY (event_id)
        REFERENCES jma_volcano_event(event_id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ash_forecast_event
    ON jma_ash_forecast (event_id);

CREATE TABLE IF NOT EXISTS jma_ash_forecast_item (
    id BIGSERIAL PRIMARY KEY,
    forecast_id BIGINT NOT NULL,
    kind_name TEXT,
    polygon TEXT,
    CONSTRAINT fk_ash_item_forecast
        FOREIGN KEY (forecast_id)
        REFERENCES jma_ash_forecast(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ash_item_forecast
    ON jma_ash_forecast_item (forecast_id);

CREATE TABLE IF NOT EXISTS jma_ash_forecast_area (
    id BIGSERIAL PRIMARY KEY,
    item_id BIGINT NOT NULL,
    area_code TEXT,
    area_name TEXT,
    CONSTRAINT fk_ash_area_item
        FOREIGN KEY (item_id)
        REFERENCES jma_ash_forecast_item(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_ash_area_item
    ON jma_ash_forecast_area (item_id);

-- =========================================================
-- 6. Volcano Info Content (narrative)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_info_content (
    event_id TEXT PRIMARY KEY,
    headline TEXT,
    activity TEXT,
    prevention TEXT,
    next_advisory TEXT,
    appendix TEXT,
    CONSTRAINT fk_info_content_event
        FOREIGN KEY (event_id)
        REFERENCES jma_volcano_event(event_id)
        ON DELETE CASCADE
);

-- =========================================================
-- 7. Comments
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_comments (
    event_id TEXT PRIMARY KEY,
    forecast_comment TEXT,
    var_comment TEXT,
    CONSTRAINT fk_comments_event
        FOREIGN KEY (event_id)
        REFERENCES jma_volcano_event(event_id)
        ON DELETE CASCADE
);

-- =========================================================
-- 8. READ MODEL: Volcano Master (UI step 1)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_master (
    volcano_code TEXT PRIMARY KEY,
    volcano_name TEXT NOT NULL,
    volcano_coord TEXT,
    first_seen_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_jma_volcano_master_name
    ON jma_volcano_master (volcano_name);

-- =========================================================
-- 9. READ MODEL: Volcano Event Type (UI step 2)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_event_type (
    volcano_code TEXT NOT NULL,
    info_kind TEXT NOT NULL,
    message_code TEXT,
    first_seen_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ,
    PRIMARY KEY (volcano_code, info_kind)
);

CREATE INDEX IF NOT EXISTS idx_event_type_volcano
    ON jma_volcano_event_type (volcano_code);

-- =========================================================
-- 10. READ MODEL: Volcano Event Counter (UI badges / sorting)
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_volcano_event_counter (
    volcano_code TEXT NOT NULL,
    info_kind TEXT NOT NULL,
    total_events BIGINT NOT NULL DEFAULT 0,
    latest_event_at TIMESTAMPTZ,
    PRIMARY KEY (volcano_code, info_kind)
);

CREATE INDEX IF NOT EXISTS idx_event_counter_volcano
    ON jma_volcano_event_counter (volcano_code);

-- =========================================================
-- 11. FACT LOOKUP INDEX (list events, no DISTINCT)
-- =========================================================
CREATE INDEX IF NOT EXISTS idx_event_lookup
    ON jma_volcano_event (volcano_code, info_kind, event_time DESC);
