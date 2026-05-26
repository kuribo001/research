#!/usr/bin/env python3
"""
Export railway line sections for selected Japanese prefectures to a Cesium-friendly JSON.

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
    },
    "miyazaki": {
        "name": "Miyazaki",
        "jp_name": "宮崎県",
        "code": "45",
        "url": "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_45_GML.zip",
        "geojson_path": "N03-20260101_45.geojson",
    },
    "shimane": {
        "name": "Shimane",
        "jp_name": "島根県",
        "code": "32",
        "url": "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_32_GML.zip",
        "geojson_path": "N03-20260101_32.geojson",
    },
}

PREFECTURE_ALIASES = {
    "tokyo": "tokyo",
    "東京都": "tokyo",
    "13": "tokyo",
    "miyazaki": "miyazaki",
    "miyazki": "miyazaki",
    "mitazaki": "miyazaki",
    "宮崎": "miyazaki",
    "宮崎県": "miyazaki",
    "45": "miyazaki",
    "shimane": "shimane",
    "島根": "shimane",
    "島根県": "shimane",
    "32": "shimane",
}


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Export railway line sections for Tokyo, Miyazaki, and Shimane."
    )
    parser.add_argument(
        "--prefectures",
        nargs="+",
        default=["tokyo", "miyazaki", "shimane"],
        help="Prefectures to export. Supported: tokyo, miyazaki, shimane.",
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
            raise ValueError(
                f"Unsupported prefecture '{item}'. Use one of: tokyo, miyazaki, shimane."
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


def filter_rail_sections(railroad_geojson: dict, prefecture_geojson: dict) -> list[dict]:
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


def export_line_json(prefecture_keys: list[str], output_path: Path, height: float) -> None:
    railroad_geojson = fetch_json_from_zip(RAILROAD_DATASET_URL, RAILROAD_GEOJSON_PATH)
    prefectures_output = []
    for prefecture_key in prefecture_keys:
        meta = PREFECTURE_DATASETS[prefecture_key]
        prefecture_geojson = fetch_json_from_zip(meta["url"], meta["geojson_path"])
        matched_features = filter_rail_sections(railroad_geojson, prefecture_geojson)
        prefectures_output.append(aggregate_sections(matched_features, prefecture_key, meta, height))

    output = {
        "format": "cesium-rail-lines-v1",
        "coordinate_order": "[longitude, latitude, height]",
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
        output_path = (
            Path(args.output)
            if args.output
            else Path(__file__).resolve().parent / "rail_lines_tokyo_miyazaki_shimane.json"
        )
        export_line_json(prefecture_keys, output_path, args.height)
    except Exception as exc:
        print(f"Error: {exc}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
