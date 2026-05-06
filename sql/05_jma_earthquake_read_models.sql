-- =========================================================
-- JMA Earthquake Feed - Read Models
-- PostgreSQL
-- =========================================================

-- =========================================================
-- 1. Earthquake message family coverage
-- One row per message code / family for UI filters or ingest dashboards
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_message_family (
    message_code TEXT PRIMARY KEY,
    info_kind TEXT,
    family_name TEXT NOT NULL,
    parser_name TEXT,
    first_seen_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ
);

-- =========================================================
-- 2. Earthquake station master from parsed payloads
-- This is not a canonical national station dictionary.
-- It is a discovered station cache populated from ingested messages.
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_station_master (
    station_code TEXT NOT NULL,
    station_name TEXT NOT NULL,
    station_type TEXT NOT NULL,
    prefecture_name TEXT,
    area_name TEXT,
    city_name TEXT,
    first_seen_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ,
    PRIMARY KEY (station_code, station_type)
);

CREATE INDEX IF NOT EXISTS idx_eq_station_master_type
    ON jma_earthquake_station_master (station_type);

CREATE INDEX IF NOT EXISTS idx_eq_station_master_name
    ON jma_earthquake_station_master (station_name);

-- =========================================================
-- 3. Event timeline view cache
-- One row per parsed XML snapshot for quick UI/event drill-down
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_event_timeline (
    earthquake_event_id BIGINT PRIMARY KEY,
    event_id TEXT NOT NULL,
    message_code TEXT NOT NULL,
    family_name TEXT,
    title TEXT,
    info_type TEXT,
    serial INTEGER,
    issued_at TIMESTAMPTZ,
    event_time TIMESTAMPTZ,
    max_intensity TEXT,
    magnitude NUMERIC(4,1),
    is_cancelled BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT fk_eq_timeline_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_timeline_event_id
    ON jma_earthquake_event_timeline (event_id, issued_at DESC);

-- =========================================================
-- 4. Latest snapshot per logical event and message code
-- One row per EventID + message code pointing at the newest parsed snapshot
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_latest_event_snapshot (
    event_id TEXT NOT NULL,
    message_code TEXT NOT NULL,
    earthquake_event_id BIGINT NOT NULL,
    family_name TEXT,
    title TEXT,
    info_type TEXT,
    serial INTEGER,
    issued_at TIMESTAMPTZ,
    event_time TIMESTAMPTZ,
    max_intensity TEXT,
    magnitude NUMERIC(4,1),
    is_cancelled BOOLEAN NOT NULL DEFAULT false,
    PRIMARY KEY (event_id, message_code),
    CONSTRAINT fk_eq_latest_snapshot_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_latest_snapshot_issued_at
    ON jma_earthquake_latest_event_snapshot (issued_at DESC);

CREATE INDEX IF NOT EXISTS idx_eq_latest_snapshot_family_event
    ON jma_earthquake_latest_event_snapshot (family_name, event_id, issued_at DESC);
