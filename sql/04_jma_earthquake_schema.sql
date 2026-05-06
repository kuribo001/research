-- =========================================================
-- JMA Earthquake Feed - Initial Database Schema
-- PostgreSQL
-- Covers:
-- VXSE42 / VXSE43 / VXSE44 / VXSE45 / VXSE47
-- VXSE51 / VXSE52 / VXSE53 / VXSE56 / VXSE60 / VXSE61 / VXSE62
-- VYSE50 / VYSE51 / VYSE52 / VYSE60 / VZSE40
-- =========================================================

-- =========================================================
-- 1. Earthquake message snapshot (core / head)
-- IMPORTANT:
-- A single EventID can appear across multiple message codes and multiple
-- snapshots over time, so the primary key must NOT be EventID itself.
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_event (
    id BIGSERIAL PRIMARY KEY,
    event_id TEXT NOT NULL,
    entry_id TEXT UNIQUE,
    message_code TEXT NOT NULL,
    family_name TEXT,
    title TEXT,
    info_kind TEXT,
    info_type TEXT,
    serial INTEGER,
    issued_at TIMESTAMPTZ,
    target_date_time TIMESTAMPTZ,
    event_time TIMESTAMPTZ,
    event_time_utc TIMESTAMPTZ,
    event_time_precision TEXT,
    headline_text TEXT,
    next_advisory TEXT,
    magnitude NUMERIC(4,1),
    magnitude_type TEXT,
    max_intensity TEXT,
    domestic_tsunami TEXT,
    foreign_tsunami TEXT,
    is_cancelled BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now(),
    CONSTRAINT fk_earthquake_event_entry
        FOREIGN KEY (entry_id)
        REFERENCES jma_feed_entry(entry_id)
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_eq_event_event_id
    ON jma_earthquake_event (event_id);

CREATE INDEX IF NOT EXISTS idx_eq_event_message_code
    ON jma_earthquake_event (message_code);

CREATE INDEX IF NOT EXISTS idx_eq_event_family_name
    ON jma_earthquake_event (family_name);

CREATE INDEX IF NOT EXISTS idx_eq_event_time
    ON jma_earthquake_event (event_time DESC);

CREATE INDEX IF NOT EXISTS idx_eq_event_issued_at
    ON jma_earthquake_event (issued_at DESC);

CREATE INDEX IF NOT EXISTS idx_eq_event_info_kind
    ON jma_earthquake_event (info_kind);

CREATE INDEX IF NOT EXISTS idx_eq_event_info_type
    ON jma_earthquake_event (info_type);

CREATE INDEX IF NOT EXISTS idx_eq_event_cancelled
    ON jma_earthquake_event (is_cancelled);

CREATE INDEX IF NOT EXISTS idx_eq_event_lookup
    ON jma_earthquake_event (event_id, message_code, issued_at DESC);

-- =========================================================
-- 2. Hypocenter / origin detail
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_hypocenter (
    earthquake_event_id BIGINT PRIMARY KEY,
    area_name TEXT,
    area_code TEXT,
    reduce_name TEXT,
    reduce_code TEXT,
    land_or_sea TEXT,
    coordinate_raw TEXT,
    latitude DOUBLE PRECISION,
    longitude DOUBLE PRECISION,
    depth_m INTEGER,
    depth_condition TEXT,
    epicenter_accuracy_rank TEXT,
    epicenter_accuracy_rank2 TEXT,
    depth_accuracy_rank TEXT,
    magnitude_calculation_rank TEXT,
    number_of_magnitude_calculation INTEGER,
    magnitude NUMERIC(4,1),
    magnitude_type TEXT,
    textual_description TEXT,
    CONSTRAINT fk_eq_hypocenter_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_hypocenter_area_code
    ON jma_earthquake_hypocenter (area_code);

-- =========================================================
-- 3. Intensity summary by area
-- Used by VXSE51 / VXSE53 / VXSE62 / VXSE47 headline-style groupings
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_intensity_area (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    source_type TEXT NOT NULL,
    area_name TEXT NOT NULL,
    area_code TEXT,
    intensity TEXT,
    intensity_numeric NUMERIC(5,2),
    long_period_class TEXT,
    sort_order INTEGER,
    CONSTRAINT fk_eq_intensity_area_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_intensity_area_event
    ON jma_earthquake_intensity_area (earthquake_event_id);

CREATE INDEX IF NOT EXISTS idx_eq_intensity_area_source
    ON jma_earthquake_intensity_area (source_type);

CREATE INDEX IF NOT EXISTS idx_eq_intensity_area_code
    ON jma_earthquake_intensity_area (area_code);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eq_intensity_area_dedup
    ON jma_earthquake_intensity_area (
        earthquake_event_id,
        source_type,
        COALESCE(area_code, ''),
        COALESCE(area_name, ''),
        COALESCE(intensity, ''),
        COALESCE(long_period_class, ''),
        COALESCE(sort_order, -1)
    );

-- =========================================================
-- 4. Municipality / city intensity
-- Used by VXSE53 and VXSE47 detailed body content
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_municipality_intensity (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    source_type TEXT NOT NULL,
    prefecture_name TEXT,
    prefecture_code TEXT,
    area_name TEXT,
    area_code TEXT,
    city_name TEXT NOT NULL,
    city_code TEXT,
    intensity TEXT,
    intensity_numeric NUMERIC(5,2),
    long_period_class TEXT,
    maximum_flag BOOLEAN DEFAULT false,
    revise_type TEXT,
    CONSTRAINT fk_eq_municipality_intensity_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_municipality_event
    ON jma_earthquake_municipality_intensity (earthquake_event_id);

CREATE INDEX IF NOT EXISTS idx_eq_municipality_source
    ON jma_earthquake_municipality_intensity (source_type);

CREATE INDEX IF NOT EXISTS idx_eq_municipality_city_code
    ON jma_earthquake_municipality_intensity (city_code);

CREATE INDEX IF NOT EXISTS idx_eq_municipality_area_code
    ON jma_earthquake_municipality_intensity (area_code);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eq_municipality_intensity_dedup
    ON jma_earthquake_municipality_intensity (
        earthquake_event_id,
        source_type,
        COALESCE(city_code, ''),
        COALESCE(city_name, ''),
        COALESCE(intensity, ''),
        COALESCE(long_period_class, ''),
        COALESCE(revise_type, '')
    );

-- =========================================================
-- 5. Station intensity
-- Covers:
-- - seismic_intensity_station
-- - realtime_intensity_station
-- - long_period_station
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_station_intensity (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    station_type TEXT NOT NULL,
    source_type TEXT NOT NULL,
    prefecture_name TEXT,
    prefecture_code TEXT,
    area_name TEXT,
    area_code TEXT,
    city_name TEXT,
    city_code TEXT,
    station_name TEXT NOT NULL,
    station_code TEXT NOT NULL,
    intensity TEXT,
    observation_status TEXT,
    realtime_intensity_text TEXT,
    realtime_intensity_value NUMERIC(6,3),
    intensity_numeric NUMERIC(6,3),
    long_period_class TEXT,
    revise_type TEXT,
    is_external BOOLEAN DEFAULT false,
    CONSTRAINT fk_eq_station_intensity_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_station_event
    ON jma_earthquake_station_intensity (earthquake_event_id);

CREATE INDEX IF NOT EXISTS idx_eq_station_code
    ON jma_earthquake_station_intensity (station_code);

CREATE INDEX IF NOT EXISTS idx_eq_station_type
    ON jma_earthquake_station_intensity (station_type);

CREATE INDEX IF NOT EXISTS idx_eq_station_source
    ON jma_earthquake_station_intensity (source_type);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eq_station_intensity_dedup
    ON jma_earthquake_station_intensity (
        earthquake_event_id,
        station_type,
        source_type,
        station_code,
        COALESCE(intensity, ''),
        COALESCE(observation_status, ''),
        COALESCE(realtime_intensity_text, ''),
        COALESCE(long_period_class, ''),
        COALESCE(revise_type, '')
    );

-- =========================================================
-- 6. Long-period observation metrics per station
-- Used by VXSE62 numeric long-period bands
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_long_period_station_metric (
    id BIGSERIAL PRIMARY KEY,
    station_intensity_id BIGINT NOT NULL,
    metric_kind TEXT NOT NULL,
    periodic_band TEXT,
    period_unit TEXT,
    value_text TEXT,
    value_numeric NUMERIC(10,3),
    unit TEXT,
    CONSTRAINT fk_long_period_metric_station
        FOREIGN KEY (station_intensity_id)
        REFERENCES jma_earthquake_station_intensity(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_long_period_metric_station
    ON jma_long_period_station_metric (station_intensity_id);

CREATE INDEX IF NOT EXISTS idx_long_period_metric_kind
    ON jma_long_period_station_metric (metric_kind);

CREATE UNIQUE INDEX IF NOT EXISTS uq_long_period_metric_dedup
    ON jma_long_period_station_metric (
        station_intensity_id,
        metric_kind,
        COALESCE(periodic_band, ''),
        COALESCE(period_unit, ''),
        COALESCE(value_text, ''),
        COALESCE(unit, '')
    );

-- =========================================================
-- 7. Narrative / commentary
-- Used by VXSE56, VYSE50/51/52, VZSE40, VYSE60, and other text-heavy families
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_comment (
    earthquake_event_id BIGINT PRIMARY KEY,
    body_text TEXT,
    free_text TEXT,
    additional_text TEXT,
    tsunami_comment TEXT,
    forecast_comment TEXT,
    appendix TEXT,
    warning_comment_text TEXT,
    warning_comment_code TEXT,
    CONSTRAINT fk_eq_comment_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

-- =========================================================
-- 8. EEW forecast areas / warning targets
-- Used by VXSE42 / VXSE43 / VXSE44 / VXSE45
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_eew_forecast_area (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    info_scope TEXT NOT NULL,
    prefecture_name TEXT,
    prefecture_code TEXT,
    kind_name TEXT,
    kind_code TEXT,
    last_kind_name TEXT,
    last_kind_code TEXT,
    area_name TEXT NOT NULL,
    area_code TEXT,
    category_kind_name TEXT,
    category_kind_code TEXT,
    forecast_int_from TEXT,
    forecast_int_to TEXT,
    forecast_lg_int_from TEXT,
    forecast_lg_int_to TEXT,
    forecast_max_intensity TEXT,
    forecast_max_long_period_class TEXT,
    condition_text TEXT,
    arrival_time TIMESTAMPTZ,
    CONSTRAINT fk_eew_forecast_area_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eew_forecast_area_event
    ON jma_eew_forecast_area (earthquake_event_id);

CREATE INDEX IF NOT EXISTS idx_eew_forecast_area_scope
    ON jma_eew_forecast_area (info_scope);

CREATE INDEX IF NOT EXISTS idx_eew_forecast_area_code
    ON jma_eew_forecast_area (area_code);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eew_forecast_area_dedup
    ON jma_eew_forecast_area (
        earthquake_event_id,
        info_scope,
        COALESCE(prefecture_code, ''),
        COALESCE(area_code, ''),
        COALESCE(kind_code, ''),
        COALESCE(last_kind_code, ''),
        COALESCE(category_kind_code, ''),
        COALESCE(arrival_time, TIMESTAMPTZ 'epoch')
    );

-- =========================================================
-- 9. EEW warning text / trigger metadata
-- Used by VXSE42 / VXSE43 / VXSE44 / VXSE45
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_eew_detail (
    earthquake_event_id BIGINT PRIMARY KEY,
    report_num INTEGER,
    is_last_report BOOLEAN,
    is_plum_assumption BOOLEAN,
    trigger_origin_time TIMESTAMPTZ,
    trigger_arrival_time TIMESTAMPTZ,
    forecast_max_int_change TEXT,
    forecast_max_lg_int_change TEXT,
    forecast_max_int_change_reason TEXT,
    warning_text TEXT,
    warning_code TEXT,
    textual_forecast TEXT,
    CONSTRAINT fk_eew_detail_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

-- =========================================================
-- 10. Nankai / special advisory detail
-- Used by VYSE50 / VYSE51 / VYSE52 / VYSE60
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_special_information (
    earthquake_event_id BIGINT PRIMARY KEY,
    information_name TEXT,
    information_keyword TEXT,
    information_code TEXT,
    serial_name TEXT,
    serial_code TEXT,
    report_condition TEXT,
    advisory_type TEXT,
    validity_text TEXT,
    CONSTRAINT fk_eq_special_info_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

-- =========================================================
-- 11.1 Special information text blocks
-- Used by Nankai / Hokkaido-Sanriku special families for main text and appendix
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_special_text_block (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    block_type TEXT NOT NULL,
    block_title TEXT,
    sort_order INTEGER,
    text_value TEXT,
    CONSTRAINT fk_eq_special_text_block_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_special_text_block_event
    ON jma_earthquake_special_text_block (earthquake_event_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eq_special_text_block_dedup
    ON jma_earthquake_special_text_block (
        earthquake_event_id,
        block_type,
        COALESCE(block_title, ''),
        COALESCE(sort_order, -1)
    );

-- =========================================================
-- 12. Generic notice / bulletin items
-- Used by VZSE40 and similar notice families
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_notice_item (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    notice_kind TEXT,
    notice_title TEXT,
    notice_text TEXT,
    area_name TEXT,
    area_code TEXT,
    sort_order INTEGER,
    CONSTRAINT fk_eq_notice_item_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_notice_item_event
    ON jma_earthquake_notice_item (earthquake_event_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eq_notice_item_dedup
    ON jma_earthquake_notice_item (
        earthquake_event_id,
        COALESCE(notice_kind, ''),
        COALESCE(notice_title, ''),
        COALESCE(area_code, ''),
        COALESCE(sort_order, -1)
    );

-- =========================================================
-- 13. Earthquake count detail
-- Used by VXSE60
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_count_item (
    id BIGSERIAL PRIMARY KEY,
    earthquake_event_id BIGINT NOT NULL,
    item_type TEXT NOT NULL,
    start_time TIMESTAMPTZ,
    end_time TIMESTAMPTZ,
    number_of_events INTEGER,
    number_of_felt_events INTEGER,
    sort_order INTEGER,
    CONSTRAINT fk_eq_count_item_event
        FOREIGN KEY (earthquake_event_id)
        REFERENCES jma_earthquake_event(id)
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_eq_count_item_event
    ON jma_earthquake_count_item (earthquake_event_id);

CREATE UNIQUE INDEX IF NOT EXISTS uq_eq_count_item_dedup
    ON jma_earthquake_count_item (
        earthquake_event_id,
        item_type,
        COALESCE(start_time, TIMESTAMPTZ 'epoch'),
        COALESCE(end_time, TIMESTAMPTZ 'epoch'),
        COALESCE(sort_order, -1)
    );

-- =========================================================
-- 14. Event counter / read model support
-- Useful for list screens, badges, and quick aggregates
-- =========================================================
CREATE TABLE IF NOT EXISTS jma_earthquake_event_counter (
    message_code TEXT NOT NULL,
    info_kind TEXT NOT NULL,
    total_events BIGINT NOT NULL DEFAULT 0,
    latest_event_at TIMESTAMPTZ,
    PRIMARY KEY (message_code, info_kind)
);

CREATE INDEX IF NOT EXISTS idx_eq_counter_message_code
    ON jma_earthquake_event_counter (message_code);
