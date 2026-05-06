# JMA Earthquake and Volcano XML Message Guide

- Source PDF: `jmaxml_20260430_Manual(pdf)/地震火山関連_解説資料.pdf`
- Companion file in Japanese: `docs/jma_quake_volcano_manual.ja.md`
- This English file is a structured translation companion and reading guide based on the extracted PDF text.

## Overview

This document explains the Japan Meteorological Agency disaster-prevention XML messages for earthquake, tsunami, and volcano-related products. It is intended as a practical guide for interpreting real operational messages rather than as a replacement for the core XML format specification.

The manual covers:

- Tsunami-related information
- Earthquake and tsunami-related information
- Nankai Trough earthquake-related information
- Volcano-related information

It also explains common XML header structures, cancellation behavior, event identifiers, serial numbers, and message-specific body structures.

## Scope of the Original Manual

The original PDF is a 188-page technical reference published by the JMA Earthquake and Volcano Department in December 2025. It focuses on how actual JMA XML bulletins are structured and used in operations.

The manual assumes readers may already know the base JMA XML specification and operational guidance. This guide therefore concentrates on the message families and field behavior specific to earthquake, tsunami, and volcano products.

## Major Message Families

### Tsunami-related information

This group includes:

- Tsunami warnings, advisories, and forecasts
- Tsunami information bulletins
- Offshore tsunami observation information

### Earthquake and tsunami-related information

This group includes:

- Earthquake Early Warnings: warning, ground-motion forecast, forecast, and test messages
- Seismic intensity flash reports
- Hypocenter information
- Hypocenter and seismic intensity information
- Earthquake activity information
- Earthquake count information
- Notice of hypocenter element updates for notable earthquakes
- Long-period ground motion observation information

### Nankai Trough earthquake-related information

This group includes:

- Nankai Trough Earthquake Extra Information
- Nankai Trough Earthquake Commentary Information

The manual notes that migration-transition messages for this family are described in an attached supplementary booklet.

### Volcano-related information

This group includes:

- Eruption warnings and forecasts
- Volcano status commentary information
- Volcanic observation reports
- Marine warnings and marine forecasts related to volcanic phenomena
- Ash fall forecasts
- Eruption速報 messages
- Estimated volcanic plume direction reports

## Overall Document Structure

The PDF is organized into two major parts.

### Part I: Common rules

This section explains shared XML structures:

- `Control`
- `Head`
- common appendices
- independent information unit handling
- `EventID` handling
- cancellation message handling
- `Serial` handling for special message families

### Part II: Body sections by message family

This section breaks down each product type in detail.

Earthquake and tsunami chapters include:

- Tsunami warnings/advisories/forecasts
- Tsunami information
- Offshore tsunami observation information
- Earthquake Early Warning
- test messages
- seismic intensity flash reports
- hypocenter information
- hypocenter and seismic intensity information
- earthquake activity information
- earthquake count information
- hypocenter element update notices
- long-period ground motion information
- Nankai Trough-related information
- Hokkaido-Sanriku post-mainshock advisory information

Volcano chapters include:

- eruption warnings and forecasts
- volcanic observation reports
- volcano notices
- ash fall forecasts
- eruption速報
- estimated plume direction reports

## Common XML Fields

The manual spends significant effort defining the common fields used across message types.

### `Control`

This section contains message distribution metadata.

Important subfields:

- `Title`: information name; also used as a key when identifying an independent information unit
- `DateTime`: transmission time from the JMA system; valid to the second
- `Status`: operation type, one of `通常`, `訓練`, or `試験`
- `EditorialOffice`: editorial office name
- `PublishingOffice`: issuing office name

### `Head`

This section contains the bulletin heading and common message metadata.

Important subfields:

- `Title`
- `ReportDateTime`
- `TargetDateTime`
- `TargetDTDubious`
- `ValidDateTime`
- `EventID`
- `InfoType`
- `Serial`
- `InfoKind`
- `InfoKindVersion`
- `Headline`

### `InfoType`

The manual defines three major values:

- `発表`: issued
- `訂正`: corrected
- `取消`: canceled

Cancellation handling is important because some nodes that normally appear are omitted in cancellation messages.

### `Serial`

`Serial` is used for follow-up messages whose contents are updated over time. However, the manual explicitly warns that the latest message should be determined using `Control/DateTime`, not only `Serial`.

### `EventID`

The meaning of `EventID` depends on the message family.

- For earthquake and tsunami products: a 14-digit earthquake identifier
- For Nankai Trough and some special information: a 14-digit arbitrary identifier
- For volcano-related products: usually a 3-digit volcano number
- For some volcanic reports such as eruption observation reports, eruption速報, and estimated plume direction reports: `ReportDateTime` and volcano number are concatenated with `_`

### `TargetDateTime`

The meaning changes by message type.

Examples:

- Seismic intensity flash report: time the first station detected the earthquake wave
- Notice of updated hypocenter elements: time the hypocenter elements were switched
- Offshore tsunami observation information: time the observation status was confirmed
- Volcanic observation report, eruption速報, plume direction report: time the phenomenon occurred
- Ash fall forecast: base time of the forecast period

For Earthquake Early Warning and its test messages, time values are valid to the second. For most other message families covered here, values are valid to the minute.

### `TargetDTDubious`

This optional field expresses uncertainty in the event time, using values such as “around the year,” “around the month,” “around the day,” “around the hour,” “around the minute,” or “around the second.”

The manual says this is used in some volcano products when the occurrence time is uncertain.

## Headline and Information Blocks

One of the most important implementation details in the manual is that `Head/Headline/Information` is not uniform across all message types.

- Some products always include it
- Some include it only under certain conditions
- Some never include it
- Cancellation messages may omit it entirely

This means a parser should not assume a single universal shape.

## Tsunami Information Structure

For tsunami warnings, advisories, and forecasts:

- `Information/@type` may be `津波予報領域表現`
- `Information` contains `Item`
- each `Item` contains `Kind` and `Areas`
- `Areas/@codeType` is the tsunami forecast area code type

For offshore tsunami observation information:

- `Information/@type` may be `沖合の津波観測に関する情報`
- `Areas/@codeType` refers to offshore tide observation points

The manual explains that the number of `Item` nodes depends on the active warning/advisory pattern and is not simply one item per issued warning level.

## Earthquake Early Warning Structure

The manual explains `Head/Headline/Information` in detail for Earthquake Early Warning warning products and ground-motion forecast products.

When present, `Information` appears in three geographic layers:

- `緊急地震速報(地方予報区)`
- `緊急地震速報(府県予報区)`
- `緊急地震速報(細分区域)`

Each `Item` contains:

- `Kind`
- `LastKind`
- `Areas`

This allows the XML to represent both the current warning status and the immediately previous status for the target area set.

The manual also describes the case where a follow-up warning adds new target areas, causing multiple `Item` entries.

## Seismic Intensity and Earthquake Information

For `震度速報`:

- one `Information` node appears with `@type="震度速報"`
- `Item` entries are grouped by observed intensity class
- each `Item` has a `Kind/Name` like `震度4` or `震度3`
- `Areas` lists the detailed seismic areas that match that intensity

For `震源・震度に関する情報`:

- `Information` may appear at both detailed-area and municipality levels
- it may be absent when all observed intensities are 2 or below
- it may also be absent when no intensity was observed
- cancellation messages omit it

For `長周期地震動に関する観測情報`:

- the structure is similar
- values are grouped by long-period ground motion class

The manual also documents a special “not yet received” category for municipalities expected to be at or above a threshold but whose observation data has not yet arrived.

## Volcano-related Notes

The manual includes dedicated chapters for volcano products and highlights differences from earthquake products.

Important points:

- volcano product titles combine volcano name and product type
- some volcano products use uncertain event times with `TargetDTDubious`
- ash fall forecast messages have explicit expiry behavior
- `EventID` rules differ from earthquake products

The ash fall forecast section also defines default expiry windows:

- scheduled ash fall forecast: 18 hours after the base time
- rapid ash fall forecast: 1 hour after the base time
- detailed ash fall forecast: about 6 hours after the base time

## Implementation Notes for Parsers and DTOs

If you are building DTOs, XML parsers, or database schemas from this manual, the most important takeaways are:

- do not assume every message family uses the same `Headline/Information` structure
- treat `InfoType="取消"` as a special case
- model `EventID` by product family rather than as one uniform identifier format
- support variable `Areas/@codeType` values depending on message type
- allow one-to-many relationships for `Information`, `Item`, and `Area`
- preserve both code and display name for area references
- preserve time precision and optional ambiguity markers
- use `Control/DateTime` when determining the latest version of a message stream

## Recommended Use of the Japanese Markdown

Use the Japanese Markdown companion when you need:

- the closest available text version of the original PDF
- exact Japanese XML element descriptions
- original terminology for naming DTO fields or enum values
- page-by-page reference while implementing parsers

Use this English companion when you need:

- a quick understanding of the manual’s structure
- a translation-oriented reading guide
- implementation notes for schema and parser design

## Files Created

- `docs/jma_quake_volcano_manual.ja.md`
- `docs/jma_quake_volcano_manual.en.md`
