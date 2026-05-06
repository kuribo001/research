# JMA Earthquake Processing Plan

- Created on: 2026-05-06
- Context: The project already has volcano-oriented SQL and DTOs. This plan extends the same architecture to JMA earthquake XML messages.

## Current Project State

The project already contains:

- a shared feed ingest table:
  - `jma_feed_entry`
- volcano DTOs under:
  - `com/twins/crawler/dtos/`
- volcano SQL schema and migrations under:
  - `sql/01_jma_volcano_schema.sql`
  - `sql/02_jma_volcano_coordinate_migration.sql`
  - `sql/03_jma_volcano_performance_indexes.sql`
- earthquake research notes and code mapping:
  - `docs/jma_earthquake_message_codes.md`
- downloaded real-world earthquake XML samples:
  - `downloads/earthquake_samples/xml/`
  - `downloads/earthquake_samples/extracted_stations.md`
  - `downloads/earthquake_samples/extracted_stations.csv`
- official JMA sample pack with broad XML coverage:
  - `jmaxml_20260326_Samples/`
- sample XML URL lists for current feed collection:
  - `samples/VXSE51.txt`
  - `samples/VXSE52.txt`
  - `samples/VXSE53.txt`
  - `samples/VXSE61_VXSE62.txt`

Key sample coverage already available:

- real-world downloaded XML:
  - `VXSE51`
  - `VXSE52`
  - `VXSE53`
  - `VXSE61`
  - `VXSE62`
- official JMA sample pack:
  - `VXSE42`
  - `VXSE43`
  - `VXSE44`
  - `VXSE45`
  - `VXSE51`
  - `VXSE52`
  - `VXSE53`
  - `VXSE56`
  - `VXSE60`
  - `VXSE61`
  - `VXSE62`
  - `VYSE50`
  - `VYSE51`
  - `VYSE52`
  - `VYSE60`
  - `VZSE40`

This means the project is no longer blocked by lack of earthquake fixtures. The next step is to extend the model from volcano messages to earthquake messages using both official fixtures and real-world XML.

## Design Principles

1. Reuse the existing feed ingest pattern.
2. Route by `message_code`, but parse by `message family`.
3. Normalize earthquake data into core event tables plus detail tables.
4. Use `EventID`, `InfoType`, `Serial`, and `Control/DateTime` for lifecycle and versioning.
5. Use the official JMA sample pack as the main parser fixture source.
6. Use downloaded real-world XML as a regression source against production-like input.
7. Test by message family and lifecycle case, not only by message code.

## Feed and Ingest Layer

Keep `jma_feed_entry` as the shared ingest table for both volcano and earthquake messages.

Expected flow:

1. Poll JMA earthquake/volcano Atom feeds (`eqvol`, `eqvol_l`).
2. Parse each Atom entry into feed metadata.
3. Store entry metadata in `jma_feed_entry`.
4. Extract `message_code` from the detail URL or filename.
5. Route the message to the correct earthquake parser family.

No separate feed table is needed for earthquake messages at this stage.

## Canonical Earthquake Head Model

Create a common head model for earthquake messages, aligned with the existing `ReportHead` idea.

Suggested fields:

- `info_kind`
- `info_type`
- `event_id`
- `issued_at`
- `serial`
- `title`
- `target_date_time`
- `is_cancelled`

This head model should be shared across multiple earthquake parser families.

## Parser Strategy

Do not create one parser per message code unless the structure is truly unique.

Use one parser per message family:

- `EEWParser`
- `SeismicIntensityParser`
- `HypocenterParser`
- `HypocenterSeismicParser`
- `EarthquakeActivityParser`
- `EarthquakeCountParser`
- `HypocenterUpdateParser`
- `LongPeriodGroundMotionParser`
- `NankaiParser`
- `EarthquakeNoticeParser`

The dispatcher should map `message_code -> parser family`.

## Sample Strategy

Use two fixture tiers:

### Tier A: official JMA sample pack

Primary use:

- parser development
- branch coverage
- `発表 / 訂正 / 取消` lifecycle testing
- rare message code coverage

Primary location:

- `jmaxml_20260326_Samples/`

### Tier B: downloaded real-world XML

Primary use:

- regression testing against production-like inputs
- validating real station lists and real-world payload variation
- checking assumptions against current operational messages

Primary location:

- `downloads/earthquake_samples/xml/`

Important note:

The official sample pack should be considered the main fixture source for initial implementation, and the downloaded XML set should be considered the realism check.

## Phase 1 Scope

Implement the first phase using the earthquake families that already have reliable XML fixtures in the repository.

Priority message codes:

- `VXSE51`
- `VXSE52`
- `VXSE53`
- `VXSE61`
- `VXSE62`
- `VXSE60`
- `VXSE56`

Recommended implementation order:

1. `VXSE52` - hypocenter information
2. `VXSE53` - hypocenter and seismic intensity information
3. `VXSE51` - seismic intensity flash report
4. `VXSE61` - hypocenter element update notice
5. `VXSE62` - long-period ground motion observation information
6. `VXSE60` - earthquake count information
7. `VXSE56` - earthquake activity / explanatory information

Reason:

- `VXSE52` and `VXSE53` define the core earthquake event model.
- `VXSE51` adds early intensity-only snapshots.
- `VXSE61` adds event update lifecycle behavior.
- `VXSE62` adds a separate but related observation family.
- `VXSE60` adds a compact count-style family.
- `VXSE56` adds commentary-style / special-event narrative handling.

Phase 1 must also include `取消` handling wherever the official sample pack provides such cases.

## Suggested Earthquake DTOs

Create DTOs by domain output, not by raw XML node.

Suggested DTO set:

- `EarthquakeEvent`
- `EarthquakeHypocenter`
- `IntensityObservation`
- `IntensityArea`
- `MunicipalityIntensity`
- `StationIntensity`
- `LongPeriodObservation`
- `EarthquakeComment`
- `EarthquakeNotice`
- `NankaiEvent`

Possible shared DTO reuse:

- `FeedEntry`
- `FeedEntryRef`
- `ReportHead`

## Suggested Earthquake SQL Model

Follow the volcano pattern: one core event table plus child tables.

Suggested tables:

- `jma_earthquake_event`
- `jma_earthquake_hypocenter`
- `jma_earthquake_intensity_area`
- `jma_earthquake_municipality_intensity`
- `jma_earthquake_station_intensity`
- `jma_long_period_observation`
- `jma_long_period_area`
- `jma_earthquake_comment`
- `jma_earthquake_notice`

Suggested event-core fields:

- `event_id`
- `entry_id`
- `message_code`
- `info_kind`
- `info_type`
- `serial`
- `title`
- `issued_at`
- `target_date_time`
- `earthquake_time`
- `earthquake_time_utc`
- `magnitude`
- `max_intensity`
- `domestic_tsunami`
- `foreign_tsunami`
- `is_cancelled`
- `created_at`

Suggested hypocenter fields:

- `event_id`
- `name`
- `code`
- `latitude`
- `longitude`
- `depth_km`
- `magnitude`
- `free_text`

Suggested intensity detail fields:

- area-level intensity
- municipality-level intensity
- station-level intensity
- observation code and display name

## Versioning and Lifecycle Rules

Earthquake messages for the same event may arrive in sequence:

- EEW
- intensity flash
- hypocenter information
- hypocenter + intensity information
- update notice

Therefore, treat one earthquake as a long-lived event with multiple message snapshots.

Versioning rules:

- use `EventID` to group messages of the same earthquake
- use `InfoType` to distinguish:
  - `発表`
  - `訂正`
  - `取消`
- use `Serial` where applicable
- use `Control/DateTime` as the final ordering key for newest message selection

Important note:

Do not model every XML as a completely separate business event. Multiple XMLs may describe the same underlying earthquake lifecycle.

## Testing and Fixtures

Use both fixture sources in a deliberate way.

Recommended next step:

1. build an earthquake sample index from `jmaxml_20260326_Samples/`
2. classify fixtures by:
   - `message_code`
   - `message_family`
   - `InfoType`
   - `source_type` (`official_sample` or `real_world`)
3. create parser tests per family
4. ensure at least one `発表` case per family
5. ensure `取消` cases are covered where available
6. validate DTO mapping
7. validate SQL-ready normalized output
8. run regression tests against downloaded real-world XML

Current observed fixture advantage:

- official sample pack has broad code coverage and includes `取消` and `訂正` cases
- downloaded XML has narrower code coverage but better reflects actual current payloads

## Phase 2 Scope

After phase 1 is stable, extend to:

- `VXSE42`
- `VXSE43`
- `VXSE44`
- `VXSE45`
- `VXSE47`
- `VYSE50`
- `VYSE51`
- `VYSE52`
- `VYSE60`
- `VZSE40`

These add:

- EEW families
- Nankai Trough information
- subsequent-earthquake advisory
- earthquake/tsunami notices

Because `VXSE42/43/44/45` already exist in the official sample pack, they can be pulled forward earlier if EEW becomes the next business priority.

## Deliverables Checklist

- [ ] Earthquake DTO design
- [ ] Earthquake SQL draft
- [ ] Message family dispatcher design
- [ ] Phase 1 parser interfaces
- [ ] Earthquake sample index for official and real-world fixtures
- [ ] Parser tests for `VXSE51/52/53/56/60/61/62`
- [ ] Cancellation-case coverage from official sample pack
- [ ] Lifecycle/versioning rules documented in code
- [ ] Phase 2 extension plan refined after phase 1

## Next Recommended Action

The most practical next implementation step is:

1. draft earthquake SQL schema
2. create earthquake DTOs
3. build a sample index from `jmaxml_20260326_Samples/`
4. implement `VXSE52` and `VXSE53` first

These two message families will define the base event model for the rest of the earthquake pipeline.
