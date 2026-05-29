package com.example.rail;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public final class TokyoRailCzmlExportConfig {
    public static final Path DEFAULT_OUTPUT_DIRECTORY =
            Path.of("line_export_tool/rail_lines_tokyo_java_lines");
    public static final List<String> DEFAULT_OPERATOR_FILTERS = List.of("Toei");
    public static final String RAILROAD_DATASET_URL =
            "https://nlftp.mlit.go.jp/ksj/gml/data/N02/N02-24/N02-24_GML.zip";
    public static final String RAILROAD_GEOJSON_PATH = "UTF-8/N02-24_RailroadSection.geojson";
    public static final PrefectureDataset TOKYO = new PrefectureDataset(
            "tokyo",
            "Tokyo",
            "東京都",
            "13",
            "https://nlftp.mlit.go.jp/ksj/gml/data/N03/N03-2026/N03-20260101_13_GML.zip",
            "N03-20260101_13.geojson"
    );
    public static final Map<String, String> OPERATOR_NAME_EN = Map.ofEntries(
            Map.entry("ゆりかもめ", "Yurikamome"),
            Map.entry("京成電鉄", "Keisei"),
            Map.entry("京浜急行電鉄", "Keikyu"),
            Map.entry("京王電鉄", "Keio"),
            Map.entry("北総鉄道", "Hokuso Railway"),
            Map.entry("埼玉高速鉄道", "Saitama Railway"),
            Map.entry("多摩都市モノレール", "Tama Monorail"),
            Map.entry("小田急電鉄", "Odakyu"),
            Map.entry("御岳登山鉄道", "Mitake Tozan Railway"),
            Map.entry("東京モノレール", "Tokyo Monorail"),
            Map.entry("東京地下鉄", "Tokyo Metro"),
            Map.entry("東京臨海高速鉄道", "Tokyo Waterfront Area Rapid Transit"),
            Map.entry("東京都", "Toei"),
            Map.entry("東急電鉄", "Tokyu"),
            Map.entry("東日本旅客鉄道", "JR East"),
            Map.entry("東武鉄道", "Tobu"),
            Map.entry("東海旅客鉄道", "JR Central"),
            Map.entry("西武鉄道", "Seibu"),
            Map.entry("首都圏新都市鉄道", "Tsukuba Express"),
            Map.entry("高尾登山電鉄", "Takao Tozan Railway")
    );
    public static final Map<String, String> OPERATOR_FILTER_ALIASES = Map.ofEntries(
            Map.entry("toei", "東京都"),
            Map.entry("tokyo metropolitan bureau of transportation", "東京都"),
            Map.entry("tokyo metro", "東京地下鉄"),
            Map.entry("jr east", "東日本旅客鉄道"),
            Map.entry("jr central", "東海旅客鉄道"),
            Map.entry("keio", "京王電鉄"),
            Map.entry("keisei", "京成電鉄"),
            Map.entry("keikyu", "京浜急行電鉄"),
            Map.entry("odakyu", "小田急電鉄"),
            Map.entry("tokyu", "東急電鉄"),
            Map.entry("tobu", "東武鉄道"),
            Map.entry("seibu", "西武鉄道"),
            Map.entry("hokuso", "北総鉄道"),
            Map.entry("hokuso railway", "北総鉄道"),
            Map.entry("saitama railway", "埼玉高速鉄道"),
            Map.entry("tsukuba express", "首都圏新都市鉄道"),
            Map.entry("mitake tozan railway", "御岳登山鉄道"),
            Map.entry("takao tozan railway", "高尾登山電鉄"),
            Map.entry("tokyo monorail", "東京モノレール"),
            Map.entry("tama monorail", "多摩都市モノレール"),
            Map.entry("tokyo waterfront area rapid transit", "東京臨海高速鉄道"),
            Map.entry("yurikamome", "ゆりかもめ")
    );

    private TokyoRailCzmlExportConfig() {
    }

    public record PrefectureDataset(
            String key,
            String name,
            String jpName,
            String code,
            String url,
            String geoJsonPath
    ) {
    }
}
