#!/usr/bin/env python3
"""
Export railway stations for Tokyo and Kanagawa to JSON.

Data sources:
- MLIT National Land Numerical Information railway stations (N02)
- MLIT National Land Numerical Information administrative areas (N03)

This script downloads official GeoJSON zip files, filters station points by
prefecture polygon, de-duplicates stations by group code, and writes one JSON
file with grouped results.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import urllib.request
import zipfile
from io import BytesIO
from pathlib import Path
from typing import Iterable


STATION_DATASET_URL = (
    "https://nlftp.mlit.go.jp/ksj/gml/data/N02/N02-24/N02-24_GML.zip"
)
STATION_GEOJSON_PATH = "UTF-8/N02-24_Station.geojson"

PREFECTURE_DATASETS = {
    "tokyo": {
        "name": "Tokyo",
        "jp_name": "東京都",
        "code": "13",
        "url": "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_13_GML.zip",
        "geojson_path": "N03-20260101_13.geojson",
    },
    "kanagawa": {
        "name": "Kanagawa",
        "jp_name": "神奈川県",
        "code": "14",
        "url": "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_14_GML.zip",
        "geojson_path": "N03-20260101_14.geojson",
    },
}

PREFECTURE_ALIASES = {
    "tokyo": "tokyo",
    "東京都": "tokyo",
    "13": "tokyo",
    "kanagawa": "kanagawa",
    "神奈川": "kanagawa",
    "神奈川県": "kanagawa",
    "14": "kanagawa",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export railway stations for Tokyo and Kanagawa."
    )
    parser.add_argument(
        "--prefectures",
        nargs="+",
        default=["tokyo", "kanagawa"],
        help="Prefectures to export. Supported: tokyo, kanagawa.",
    )
    parser.add_argument(
        "--output",
        default=None,
        help="Output JSON file path.",
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
            raise ValueError(
                f"Unsupported prefecture '{item}'. Use one of: tokyo, kanagawa."
            )
        if key not in normalized:
            normalized.append(key)
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


def point_in_bbox(point: tuple[float, float], bbox: tuple[float, float, float, float]) -> bool:
    x, y = point
    min_x, min_y, max_x, max_y = bbox
    return min_x <= x <= max_x and min_y <= y <= max_y


def representative_point(geometry: dict) -> tuple[float, float] | None:
    geom_type = geometry.get("type")
    coords = geometry.get("coordinates")
    if not coords:
        return None
    if geom_type == "Point":
        return coords[0], coords[1]
    if geom_type == "LineString":
        xs = [pt[0] for pt in coords]
        ys = [pt[1] for pt in coords]
        return sum(xs) / len(xs), sum(ys) / len(ys)
    if geom_type == "MultiLineString":
        flat = [pt for line in coords for pt in line]
        if not flat:
            return None
        xs = [pt[0] for pt in flat]
        ys = [pt[1] for pt in flat]
        return sum(xs) / len(xs), sum(ys) / len(ys)
    return None


def slugify_ascii_fallback(value: str) -> str:
    slug = re.sub(r"[^a-z0-9]+", "_", value.lower()).strip("_")
    return slug or "unknown"


def build_internal_operator_key(operator_name: str) -> str:
    manual_map = {
        "東京都": "mlit:toei",
        "東京地下鉄": "mlit:tokyo_metro",
        "東日本旅客鉄道": "mlit:jr_east",
        "東海旅客鉄道": "mlit:jr_central",
        "西日本旅客鉄道": "mlit:jr_west",
        "九州旅客鉄道": "mlit:jr_kyushu",
        "京王電鉄": "mlit:keio",
        "京成電鉄": "mlit:keisei",
        "京浜急行電鉄": "mlit:keikyu",
        "小田急電鉄": "mlit:odakyu",
        "東急電鉄": "mlit:tokyu",
        "東武鉄道": "mlit:tobu",
        "西武鉄道": "mlit:seibu",
        "首都圏新都市鉄道": "mlit:tx",
        "東京モノレール": "mlit:tokyo_monorail",
        "東京臨海高速鉄道": "mlit:twr",
        "多摩都市モノレール": "mlit:tama_monorail",
        "ゆりかもめ": "mlit:yurikamome",
        "北総鉄道": "mlit:hokuso",
        "埼玉高速鉄道": "mlit:saitama_railway",
        "御岳登山鉄道": "mlit:mitake_tozan",
        "高尾登山電鉄": "mlit:takao_tozan",
        "一畑電車": "mlit:ichibata",
    }
    if operator_name in manual_map:
        return manual_map[operator_name]
    return f"mlit:{slugify_ascii_fallback(operator_name)}"


def aggregate_stations(
    features: list[dict], prefecture_key: str, prefecture_meta: dict
) -> tuple[dict, dict[str, dict]]:
    grouped: dict[tuple[str, str], dict] = {}
    operator_catalog: dict[str, dict] = {}
    for feature in features:
        props = feature["properties"]
        point = representative_point(feature["geometry"])
        if point is None:
            continue
        station_name = props.get("N02_005")
        station_code = props.get("N02_005c")
        group_code = props.get("N02_005g") or station_code or station_name
        key = (group_code, station_name)
        item = grouped.setdefault(
            key,
            {
                "prefecture_key": prefecture_key,
                "prefecture_name": prefecture_meta["name"],
                "prefecture_name_ja": prefecture_meta["jp_name"],
                "station_name": station_name,
                "station_code": station_code,
                "station_group_code": group_code,
                "longitude": point[0],
                "latitude": point[1],
                "lines": set(),
                "operators": set(),
                "railway_types": set(),
                "operator_types": set(),
                "source_records": 0,
            },
        )
        item["lines"].add(props.get("N02_003"))
        operator_name = props.get("N02_004")
        if operator_name:
            item["operators"].add(operator_name)
            internal_key = build_internal_operator_key(operator_name)
            item.setdefault("internal_operator_keys", set()).add(internal_key)
            operator_entry = operator_catalog.setdefault(
                internal_key,
                {
                    "internal_operator_key": internal_key,
                    "display_name": operator_name,
                    "source_mappings": [
                        {
                            "source_type": "mlit",
                            "source_operator_id": None,
                            "source_operator_name": operator_name,
                            "match_confidence": "medium",
                            "matching_method": "manual_name_mapping",
                        }
                    ],
                },
            )
            operator_entry["display_name"] = operator_name
        item["railway_types"].add(props.get("N02_001"))
        item["operator_types"].add(props.get("N02_002"))
        item["source_records"] += 1

    stations = []
    for item in grouped.values():
        stations.append(
            {
                **{k: v for k, v in item.items() if not isinstance(v, set)},
                "lines": sorted(v for v in item["lines"] if v),
                "operators": sorted(v for v in item["operators"] if v),
                "internal_operator_keys": sorted(v for v in item.get("internal_operator_keys", set()) if v),
                "railway_type_codes": sorted(v for v in item["railway_types"] if v),
                "operator_type_codes": sorted(v for v in item["operator_types"] if v),
            }
        )

    stations.sort(key=lambda x: (x["station_name"] or "", x["station_group_code"] or ""))
    return {
        "prefecture_key": prefecture_key,
        "prefecture_name": prefecture_meta["name"],
        "prefecture_name_ja": prefecture_meta["jp_name"],
        "station_count": len(stations),
        "stations": stations,
    }, operator_catalog


def filter_station_features(station_geojson: dict, prefecture_geojson: dict) -> list[dict]:
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
    for feature in station_geojson["features"]:
        geometry = feature.get("geometry") or {}
        point = representative_point(geometry)
        if point is None:
            continue
        if not point_in_bbox(point, overall_bbox):
            continue
        if any(
            point_in_bbox(point, bbox) and point_in_geometry(point, prefecture_geometry)
            for prefecture_geometry, bbox in zip(prefecture_geometries, prefecture_bboxes)
        ):
            matches.append(feature)
    return matches


def export_station_json(prefecture_keys: list[str], output_path: Path) -> None:
    station_geojson = fetch_json_from_zip(STATION_DATASET_URL, STATION_GEOJSON_PATH)
    prefectures_output = []
    global_operator_catalog: dict[str, dict] = {}
    for prefecture_key in prefecture_keys:
        meta = PREFECTURE_DATASETS[prefecture_key]
        prefecture_geojson = fetch_json_from_zip(meta["url"], meta["geojson_path"])
        matched_features = filter_station_features(station_geojson, prefecture_geojson)
        prefecture_output, operator_catalog = aggregate_stations(matched_features, prefecture_key, meta)
        prefectures_output.append(prefecture_output)
        for key, value in operator_catalog.items():
            global_operator_catalog.setdefault(key, value)

    output = {
        "source": {
            "railway_station_dataset": {
                "url": STATION_DATASET_URL,
                "member_path": STATION_GEOJSON_PATH,
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
        "operators": sorted(global_operator_catalog.values(), key=lambda item: item["internal_operator_key"]),
        "prefectures": prefectures_output,
    }

    output_path.parent.mkdir(parents=True, exist_ok=True)
    output_path.write_text(json.dumps(output, ensure_ascii=False, indent=2) + "\n")


def main() -> int:
    args = parse_args()
    try:
        prefecture_keys = normalize_prefecture_keys(args.prefectures)
        output_path = (
            Path(args.output)
            if args.output
            else Path(__file__).resolve().parent / "stations_tokyo_kanagawa.json"
        )
        export_station_json(prefecture_keys, output_path)
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
