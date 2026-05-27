-- Schema ví dụ để lưu GTFS static của Toei.
-- Target: PostgreSQL.

CREATE TABLE gtfs_agency (
    agency_id text PRIMARY KEY,
    agency_name text NOT NULL,
    agency_url text NOT NULL,
    agency_timezone text NOT NULL,
    agency_lang text,
    agency_phone text,
    agency_fare_url text,
    agency_email text
);

CREATE TABLE gtfs_routes (
    route_id text PRIMARY KEY,
    agency_id text REFERENCES gtfs_agency (agency_id),
    route_short_name text,
    route_long_name text,
    route_desc text,
    route_type integer NOT NULL,
    route_url text,
    route_color text,
    route_text_color text
);

CREATE TABLE gtfs_stops (
    stop_id text PRIMARY KEY,
    stop_code text,
    stop_name text NOT NULL,
    stop_desc text,
    stop_lat numeric(10, 7),
    stop_lon numeric(10, 7),
    zone_id text,
    stop_url text,
    location_type integer,
    parent_station text,
    stop_timezone text,
    wheelchair_boarding integer
);

CREATE TABLE gtfs_calendar (
    service_id text PRIMARY KEY,
    monday boolean NOT NULL,
    tuesday boolean NOT NULL,
    wednesday boolean NOT NULL,
    thursday boolean NOT NULL,
    friday boolean NOT NULL,
    saturday boolean NOT NULL,
    sunday boolean NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL
);

CREATE TABLE gtfs_calendar_dates (
    service_id text NOT NULL REFERENCES gtfs_calendar (service_id),
    service_date date NOT NULL,
    exception_type integer NOT NULL,
    PRIMARY KEY (service_id, service_date)
);

CREATE TABLE gtfs_trips (
    trip_id text PRIMARY KEY,
    route_id text NOT NULL REFERENCES gtfs_routes (route_id),
    service_id text NOT NULL REFERENCES gtfs_calendar (service_id),
    trip_headsign text,
    trip_short_name text,
    direction_id integer,
    block_id text,
    shape_id text,
    wheelchair_accessible integer,
    bikes_allowed integer
);

CREATE TABLE gtfs_stop_times (
    trip_id text NOT NULL REFERENCES gtfs_trips (trip_id),
    arrival_time text,
    departure_time text,
    stop_id text NOT NULL REFERENCES gtfs_stops (stop_id),
    stop_sequence integer NOT NULL,
    stop_headsign text,
    pickup_type integer,
    drop_off_type integer,
    shape_dist_traveled numeric,
    timepoint integer,
    PRIMARY KEY (trip_id, stop_sequence)
);

CREATE TABLE gtfs_fare_attributes (
    fare_id text PRIMARY KEY,
    price numeric,
    currency_type text,
    payment_method integer,
    transfers integer,
    agency_id text,
    transfer_duration integer
);

CREATE TABLE gtfs_fare_rules (
    fare_id text NOT NULL REFERENCES gtfs_fare_attributes (fare_id),
    route_id text,
    origin_id text,
    destination_id text,
    contains_id text
);

CREATE TABLE gtfs_translations (
    table_name text NOT NULL,
    field_name text NOT NULL,
    field_value text NOT NULL,
    language text NOT NULL,
    translation text NOT NULL,
    PRIMARY KEY (table_name, field_name, field_value, language)
);

CREATE TABLE gtfs_feed_info (
    feed_publisher_name text,
    feed_publisher_url text,
    feed_lang text,
    feed_start_date date,
    feed_end_date date,
    feed_version text,
    feed_contact_email text,
    feed_contact_url text
);

-- View tiện dụng: suy ra ga đi / ga đến của mỗi trip từ stop_times.
CREATE VIEW gtfs_trip_origin_destination AS
WITH ranked_stop_times AS (
    SELECT
        st.*,
        row_number() OVER (PARTITION BY st.trip_id ORDER BY st.stop_sequence ASC) AS rn_first,
        row_number() OVER (PARTITION BY st.trip_id ORDER BY st.stop_sequence DESC) AS rn_last
    FROM gtfs_stop_times st
),
origins AS (
    SELECT *
    FROM ranked_stop_times
    WHERE rn_first = 1
),
destinations AS (
    SELECT *
    FROM ranked_stop_times
    WHERE rn_last = 1
)
SELECT
    t.trip_id,
    t.route_id,
    r.route_long_name,
    t.service_id,
    t.trip_headsign,
    t.direction_id,
    o.stop_id AS origin_stop_id,
    os.stop_name AS origin_stop_name,
    o.departure_time AS origin_departure_time,
    d.stop_id AS destination_stop_id,
    ds.stop_name AS destination_stop_name,
    d.arrival_time AS destination_arrival_time
FROM gtfs_trips t
JOIN gtfs_routes r ON r.route_id = t.route_id
JOIN origins o ON o.trip_id = t.trip_id
JOIN destinations d ON d.trip_id = t.trip_id
JOIN gtfs_stops os ON os.stop_id = o.stop_id
JOIN gtfs_stops ds ON ds.stop_id = d.stop_id;

CREATE INDEX idx_gtfs_routes_agency_id ON gtfs_routes (agency_id);
CREATE INDEX idx_gtfs_trips_route_id ON gtfs_trips (route_id);
CREATE INDEX idx_gtfs_trips_service_id ON gtfs_trips (service_id);
CREATE INDEX idx_gtfs_stop_times_trip_id ON gtfs_stop_times (trip_id);
CREATE INDEX idx_gtfs_stop_times_stop_id ON gtfs_stop_times (stop_id);
CREATE INDEX idx_gtfs_stop_times_stop_sequence ON gtfs_stop_times (trip_id, stop_sequence);
CREATE INDEX idx_gtfs_stops_stop_code ON gtfs_stops (stop_code);
CREATE INDEX idx_gtfs_calendar_dates_service_date ON gtfs_calendar_dates (service_date);
