#!/usr/bin/env python3
"""
Export railway line sections for Tokyo to a Cesium-friendly JSON.

Data sources:
- MLIT National Land Numerical Information railway data (N02)
- MLIT National Land Numerical Information administrative areas (N03)
"""

from __future__ import annotations

import argparse
import json
import sys
import urllib.request
import zipfile
from io import BytesIO
from pathlib import Path
from typing import Iterable


RAILROAD_DATASET_URL = "https://nlftp.mlit.go.jp/ksj/gml/data/N02/N02-24/N02-24_GML.zip"
RAILROAD_GEOJSON_PATH = "UTF-8/N02-24_RailroadSection.geojson"

PREFECTURE_DATASETS = {
    "tokyo": {
        "name": "Tokyo",
        "jp_name": "東京都",
        "code": "13",
        "url": "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_13_GML.zip",
        "geojson_path": "N03-20260101_13.geojson",
    }
}

PREFECTURE_ALIASES = {
    "tokyo": "tokyo",
    "東京都": "tokyo",
    "13": "tokyo",
}

OPERATOR_NAME_EN = {
    "ゆりかもめ": "Yurikamome",
    "京成電鉄": "Keisei",
    "京浜急行電鉄": "Keikyu",
    "京王電鉄": "Keio",
    "北総鉄道": "Hokuso Railway",
    "埼玉高速鉄道": "Saitama Railway",
    "多摩都市モノレール": "Tama Monorail",
    "小田急電鉄": "Odakyu",
    "御岳登山鉄道": "Mitake Tozan Railway",
    "東京モノレール": "Tokyo Monorail",
    "東京地下鉄": "Tokyo Metro",
    "東京臨海高速鉄道": "Tokyo Waterfront Area Rapid Transit",
    "東京都": "Toei",
    "東急電鉄": "Tokyu",
    "東日本旅客鉄道": "JR East",
    "東武鉄道": "Tobu",
    "東海旅客鉄道": "JR Central",
    "西武鉄道": "Seibu",
    "首都圏新都市鉄道": "Tsukuba Express",
    "高尾登山電鉄": "Takao Tozan Railway",
}

OPERATOR_FILTER_ALIASES = {
    "toei": "東京都",
    "tokyo metropolitan bureau of transportation": "東京都",
    "tokyo metro": "東京地下鉄",
    "jr east": "東日本旅客鉄道",
    "jr central": "東海旅客鉄道",
    "keio": "京王電鉄",
    "keisei": "京成電鉄",
    "keikyu": "京浜急行電鉄",
    "odakyu": "小田急電鉄",
    "tokyu": "東急電鉄",
    "tobu": "東武鉄道",
    "seibu": "西武鉄道",
    "hokuso": "北総鉄道",
    "hokuso railway": "北総鉄道",
    "saitama railway": "埼玉高速鉄道",
    "tsukuba express": "首都圏新都市鉄道",
    "mitake tozan railway": "御岳登山鉄道",
    "takao tozan railway": "高尾登山電鉄",
    "tokyo monorail": "東京モノレール",
    "tama monorail": "多摩都市モノレール",
    "tokyo waterfront area rapid transit": "東京臨海高速鉄道",
    "yurikamome": "ゆりかもめ",
}

# Leave empty to export all Tokyo operators.
# Examples:
OPERATOR_FILTERS = ["Toei"]
# OPERATOR_FILTERS = ["Tokyo Metro", "Yurikamome"]
# OPERATOR_FILTERS = [
# ]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export railway line sections for Tokyo."
    )
    parser.add_argument(
        "--prefectures",
        nargs="+",
        default=["tokyo"],
        help="Optional compatibility flag. Only tokyo is supported.",
    )
    parser.add_argument(
        "--output",
        default=None,
        help="Output JSON file path.",
    )
    parser.add_argument(
        "--height",
        type=float,
        default=0.0,
        help="Height value to append to every coordinate for Cesium.",
    )
    return parser.parse_args()


def fetch_json_from_zip(zip_url: str, member_path: str) -> dict:
    with urllib.request.urlopen(zip_url) as response:
        payload = response.read()
    with zipfile.ZipFile(BytesIO(payload)) as zf:
        with zf.open(member_path) as fp:
            return json.load(fp)


def normalize_prefecture_keys(requested: Iterable[str]) -> list[str]:
    normalized = []
    for item in requested:
        key = PREFECTURE_ALIASES.get(item.strip().lower(), PREFECTURE_ALIASES.get(item))
        if key is None:
            raise ValueError(f"Unsupported prefecture '{item}'. Only tokyo is supported.")
        if key not in normalized:
            normalized.append(key)
    return normalized


def normalize_operator_name(value: str) -> str:
    return " ".join(value.strip().split()).casefold()


def operator_name_en(operator_name: str | None) -> str | None:
    if not operator_name:
        return None
    return OPERATOR_NAME_EN.get(operator_name)


def resolve_operator_filter(value: str) -> str:
    key = normalize_operator_name(value)
    return OPERATOR_FILTER_ALIASES.get(key, value.strip())


def normalize_operator_filters(requested: Iterable[str]) -> list[str]:
    normalized = []
    seen = set()
    for item in requested:
        value = resolve_operator_filter(item)
        if not value:
            raise ValueError("Operator filter cannot be empty.")
        key = normalize_operator_name(value)
        if key not in seen:
            seen.add(key)
            normalized.append(value)
    return normalized


def point_in_ring(point: tuple[float, float], ring: list[list[float]]) -> bool:
    x, y = point
    inside = False
    j = len(ring) - 1
    for i in range(len(ring)):
        xi, yi = ring[i]
        xj, yj = ring[j]
        intersects = ((yi > y) != (yj > y)) and (
            x < (xj - xi) * (y - yi) / ((yj - yi) or 1e-12) + xi
        )
        if intersects:
            inside = not inside
        j = i
    return inside


def point_in_polygon(point: tuple[float, float], polygon_coords: list[list[list[float]]]) -> bool:
    if not polygon_coords:
        return False
    if not point_in_ring(point, polygon_coords[0]):
        return False
    for hole in polygon_coords[1:]:
        if point_in_ring(point, hole):
            return False
    return True


def point_in_geometry(point: tuple[float, float], geometry: dict) -> bool:
    geom_type = geometry["type"]
    coords = geometry["coordinates"]
    if geom_type == "Polygon":
        return point_in_polygon(point, coords)
    if geom_type == "MultiPolygon":
        return any(point_in_polygon(point, polygon) for polygon in coords)
    return False


def iter_polygon_rings(geometry: dict) -> Iterable[list[list[float]]]:
    geom_type = geometry["type"]
    coords = geometry["coordinates"]
    if geom_type == "Polygon":
        for ring in coords:
            yield ring
    elif geom_type == "MultiPolygon":
        for polygon in coords:
            for ring in polygon:
                yield ring


def geometry_bbox(geometry: dict) -> tuple[float, float, float, float] | None:
    xs = []
    ys = []
    for ring in iter_polygon_rings(geometry):
        for x, y in ring:
            xs.append(x)
            ys.append(y)
    if not xs:
        return None
    return min(xs), min(ys), max(xs), max(ys)


def line_bbox(coords: list[list[float]]) -> tuple[float, float, float, float]:
    xs = [pt[0] for pt in coords]
    ys = [pt[1] for pt in coords]
    return min(xs), min(ys), max(xs), max(ys)


def bbox_intersects(a: tuple[float, float, float, float], b: tuple[float, float, float, float]) -> bool:
    return not (a[2] < b[0] or b[2] < a[0] or a[3] < b[1] or b[3] < a[1])


def line_intersects_geometry(coords: list[list[float]], geometry: dict) -> bool:
    if not coords:
        return False
    points = [(pt[0], pt[1]) for pt in coords]
    return any(point_in_geometry(point, geometry) for point in points)


def to_cesium_coords(coords: list[list[float]], height: float) -> list[list[float]]:
    return [[round(pt[0], 6), round(pt[1], 6), height] for pt in coords]


def filter_rail_sections(
    railroad_geojson: dict, prefecture_geojson: dict, operator_filters: set[str] | None = None
) -> list[dict]:
    prefecture_geometries = []
    prefecture_bboxes = []
    for feature in prefecture_geojson["features"]:
        geometry = feature["geometry"]
        bbox = geometry_bbox(geometry)
        if bbox is None:
            continue
        prefecture_geometries.append(geometry)
        prefecture_bboxes.append(bbox)

    if not prefecture_bboxes:
        return []

    overall_bbox = (
        min(b[0] for b in prefecture_bboxes),
        min(b[1] for b in prefecture_bboxes),
        max(b[2] for b in prefecture_bboxes),
        max(b[3] for b in prefecture_bboxes),
    )

    matches = []
    for feature in railroad_geojson["features"]:
        geometry = feature.get("geometry") or {}
        if geometry.get("type") != "LineString":
            continue
        props = feature.get("properties") or {}
        operator_name = props.get("N02_004") or ""
        if operator_filters and normalize_operator_name(operator_name) not in operator_filters:
            continue
        coords = geometry.get("coordinates") or []
        if len(coords) < 2:
            continue
        if not bbox_intersects(line_bbox(coords), overall_bbox):
            continue
        if any(
            bbox_intersects(line_bbox(coords), bbox) and line_intersects_geometry(coords, prefecture_geometry)
            for prefecture_geometry, bbox in zip(prefecture_geometries, prefecture_bboxes)
        ):
            matches.append(feature)
    return matches


def aggregate_sections(
    features: list[dict], prefecture_key: str, prefecture_meta: dict, height: float
) -> dict:
    grouped: dict[tuple[str, str], dict] = {}
    for index, feature in enumerate(features, start=1):
        props = feature["properties"]
        coords = feature["geometry"]["coordinates"]
        line_name = props.get("N02_003")
        operator_name = props.get("N02_004")
        key = (operator_name or "", line_name or "")
        item = grouped.setdefault(
            key,
            {
                "prefecture_key": prefecture_key,
                "prefecture_name": prefecture_meta["name"],
                "prefecture_name_ja": prefecture_meta["jp_name"],
                "line_name": line_name,
                "operator_name": operator_name,
                "operator_name_en": operator_name_en(operator_name),
                "railway_type_codes": set(),
                "operator_type_codes": set(),
                "sections": [],
            },
        )
        item["railway_type_codes"].add(props.get("N02_001"))
        item["operator_type_codes"].add(props.get("N02_002"))
        item["sections"].append(
            {
                "section_id": f"{prefecture_key}-{index}",
                "coordinate_count": len(coords),
                "coordinates": to_cesium_coords(coords, height),
            }
        )

    lines = []
    for item in grouped.values():
        lines.append(
            {
                "prefecture_key": item["prefecture_key"],
                "prefecture_name": item["prefecture_name"],
                "prefecture_name_ja": item["prefecture_name_ja"],
                "line_name": item["line_name"],
                "operator_name": item["operator_name"],
                "operator_name_en": item["operator_name_en"],
                "railway_type_codes": sorted(v for v in item["railway_type_codes"] if v),
                "operator_type_codes": sorted(v for v in item["operator_type_codes"] if v),
                "section_count": len(item["sections"]),
                "sections": item["sections"],
            }
        )

    lines.sort(key=lambda x: ((x["operator_name"] or ""), (x["line_name"] or "")))
    return {
        "prefecture_key": prefecture_key,
        "prefecture_name": prefecture_meta["name"],
        "prefecture_name_ja": prefecture_meta["jp_name"],
        "line_count": len(lines),
        "section_count": sum(line["section_count"] for line in lines),
        "lines": lines,
    }


def export_line_json(
    prefecture_keys: list[str], output_path: Path, height: float, operators: list[str]
) -> None:
    railroad_geojson = fetch_json_from_zip(RAILROAD_DATASET_URL, RAILROAD_GEOJSON_PATH)
    operator_filter_keys = {normalize_operator_name(item) for item in operators}
    prefectures_output = []
    for prefecture_key in prefecture_keys:
        meta = PREFECTURE_DATASETS[prefecture_key]
        prefecture_geojson = fetch_json_from_zip(meta["url"], meta["geojson_path"])
        matched_features = filter_rail_sections(
            railroad_geojson,
            prefecture_geojson,
            operator_filters=operator_filter_keys or None,
        )
        prefectures_output.append(aggregate_sections(matched_features, prefecture_key, meta, height))

    output = {
        "format": "cesium-rail-lines-v1",
        "coordinate_order": "[longitude, latitude, height]",
        "configured_operator_filters": OPERATOR_FILTERS,
        "operator_filters": operators,
        "operator_filters_en": [operator_name_en(item) or item for item in operators],
        "source": {
            "railroad_dataset": {
                "url": RAILROAD_DATASET_URL,
                "member_path": RAILROAD_GEOJSON_PATH,
            },
            "prefecture_boundary_datasets": {
                key: {
                    "url": PREFECTURE_DATASETS[key]["url"],
                    "member_path": PREFECTURE_DATASETS[key]["geojson_path"],
                }
                for key in prefecture_keys
            },
        },
        "prefecture_count": len(prefectures_output),
        "generated_prefectures": prefecture_keys,
        "prefectures": prefectures_output,
    }

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n")


def main() -> int:
    args = parse_args()
    try:
        prefecture_keys = normalize_prefecture_keys(args.prefectures)
        operators = normalize_operator_filters(OPERATOR_FILTERS)
        output_path = (
            Path(args.output)
            if args.output
            else Path(__file__).resolve().parent / "rail_lines_tokyo.json"
        )
        export_line_json(prefecture_keys, output_path, args.height, operators)
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
