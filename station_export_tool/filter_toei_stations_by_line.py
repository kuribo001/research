#!/usr/bin/env python3
"""
Filter Tokyo Toei stations by MLIT line from an aggregated MLIT station export.

This utility reads:
- station_export_tool/stations_tokyo_kanagawa.json
- line_export_tool/mlit_to_odpt_route_mapping_toei.json

and writes a Tokyo Toei-only JSON grouped by each mapped MLIT line.

Important:
- The source station file is already grouped by station group code.
- The output represents station membership per line, not ordered stop sequence.
"""

from __future__ import annotations

import argparse
import json
from pathlib import Path


DEFAULT_STATIONS_PATH = (
    Path(__file__).resolve().parent / "stations_tokyo_kanagawa.json"
)
DEFAULT_MAPPING_PATH = (
    Path(__file__).resolve().parent.parent
    / "line_export_tool"
    / "mlit_to_odpt_route_mapping_toei.json"
)
DEFAULT_OUTPUT_PATH = (
    Path(__file__).resolve().parent / "stations_tokyo_toei_by_line.json"
)

TOEI_OPERATOR_NAME = "東京都"


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Filter Tokyo Toei stations by each MLIT line."
    )
    parser.add_argument(
        "--stations",
        default=str(DEFAULT_STATIONS_PATH),
        help="Path to aggregated MLIT station JSON.",
    )
    parser.add_argument(
        "--mapping",
        default=str(DEFAULT_MAPPING_PATH),
        help="Path to MLIT-to-ODPT Toei route mapping JSON.",
    )
    parser.add_argument(
        "--output",
        default=str(DEFAULT_OUTPUT_PATH),
        help="Output JSON file path.",
    )
    return parser.parse_args()


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def find_tokyo_prefecture(payload: dict) -> dict:
    for prefecture in payload.get("prefectures", []):
        if prefecture.get("prefecture_key") == "tokyo":
            return prefecture
    raise ValueError("Tokyo prefecture payload not found in station export.")


def station_matches_line(station: dict, mlit_line_name: str) -> bool:
    operators = set(station.get("operators") or [])
    lines = set(station.get("lines") or [])
    return TOEI_OPERATOR_NAME in operators and mlit_line_name in lines


def sort_stations(stations: list[dict]) -> list[dict]:
    return sorted(
        stations,
        key=lambda item: (
            item.get("station_name") or "",
            item.get("station_group_code") or "",
        ),
    )


def build_output(
    station_payload: dict,
    mapping_payload: dict,
    stations_path: Path,
    mapping_path: Path,
) -> dict:
    tokyo = find_tokyo_prefecture(station_payload)
    lines = []
    for route in mapping_payload.get("routes", []):
        mlit_line_name = route.get("mlit_line_name")
        if not mlit_line_name:
            continue
        matched_stations = [
            station
            for station in tokyo.get("stations", [])
            if station_matches_line(station, mlit_line_name)
        ]
        matched_stations = sort_stations(matched_stations)
        lines.append(
            {
                "mlit_operator_name": route.get("mlit_operator_name"),
                "mlit_line_name": mlit_line_name,
                "odpt_route_id": route.get("odpt_route_id"),
                "odpt_route_long_name": route.get("odpt_route_long_name"),
                "station_count": len(matched_stations),
                "station_names": [station.get("station_name") for station in matched_stations],
                "stations": matched_stations,
            }
        )

    return {
        "source": {
            "aggregated_station_file": stations_path.name,
            "mapping_file": mapping_path.name,
        },
        "scope": {
            "prefecture_key": tokyo.get("prefecture_key"),
            "prefecture_name": tokyo.get("prefecture_name"),
            "prefecture_name_ja": tokyo.get("prefecture_name_ja"),
            "mlit_operator_name": TOEI_OPERATOR_NAME,
            "odpt_operator_name_en": mapping_payload.get("scope", {}).get("odpt_operator_name_en"),
            "odpt_agency_id": mapping_payload.get("scope", {}).get("odpt_agency_id"),
        },
        "notes": [
            "Derived from the aggregated MLIT station export already grouped by station group code.",
            "Stations are grouped by exact MLIT line name matched against Toei line mappings.",
            "This output does not contain station sequence order for each line.",
        ],
        "line_count": len(lines),
        "lines": lines,
    }


def main() -> int:
    args = parse_args()
    stations_path = Path(args.stations).resolve()
    mapping_path = Path(args.mapping).resolve()
    output_path = Path(args.output).resolve()

    station_payload = load_json(stations_path)
    mapping_payload = load_json(mapping_path)
    output = build_output(station_payload, mapping_payload, stations_path, mapping_path)

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(
        json.dumps(output, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    print(output_path)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
