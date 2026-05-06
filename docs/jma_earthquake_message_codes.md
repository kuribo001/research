# JMA Earthquake Message Codes

- Created on: 2026-05-06
- Scope: JMA feed message codes related to earthquake information
- Note: This list is based on the current JMA catalog pages checked on 2026-05-06

## Core Earthquake Message Codes

| Code | Japanese name | Short description |
| --- | --- | --- |
| `VXSE42` | 緊急地震速報配信テスト | EEW XML test message |
| `VXSE43` | 緊急地震速報（警報） | Earthquake Early Warning, warning |
| `VXSE44` | 緊急地震速報（警報）・緊急地震速報（予報） | EEW warning / forecast |
| `VXSE45` | 緊急地震速報（警報）・緊急地震速報（予報） | EEW warning / forecast |
| `VXSE47` | リアルタイム震度電文 | Real-time seismic intensity message |
| `VXSE51` | 震度速報 | Seismic intensity flash report |
| `VXSE52` | 地震情報（震源に関する情報） | Hypocenter information |
| `VXSE53` | 地震情報（震源・震度に関する情報） | Hypocenter and seismic intensity information |
| `VXSE56` | 地震の活動状況等に関する情報 | Earthquake activity / explanatory information |
| `VXSE60` | 地震回数に関する情報 | Earthquake count information |
| `VXSE61` | 顕著な地震の震源要素更新のお知らせ | Notice of updated hypocenter elements for a notable earthquake |
| `VXSE62` | 長周期地震動に関する観測情報 | Long-period ground motion observation information |

## Special Earthquake-related Codes

| Code | Japanese name | Short description |
| --- | --- | --- |
| `VYSE50` | 南海トラフ地震臨時情報 | Nankai Trough Earthquake Extra Information |
| `VYSE51` | 南海トラフ地震関連解説情報 | Nankai Trough Earthquake Commentary Information |
| `VYSE52` | 南海トラフ地震関連解説情報 | Nankai Trough Earthquake Commentary Information, periodic edition |
| `VYSE60` | 北海道・三陸沖後発地震注意情報 | Hokkaido / Sanriku-oki subsequent earthquake advisory |

## Related Notice Code

| Code | Japanese name | Short description |
| --- | --- | --- |
| `VZSE40` | 地震・津波に関するお知らせ | Notices related to earthquake and tsunami information |

## Suggested Minimal Set

If you only want the main earthquake XML messages for parsing, start with:

- `VXSE43`
- `VXSE44`
- `VXSE45`
- `VXSE47`
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

## Official Sources

- JMA catalog, `地震情報等`:
  https://www.data.jma.go.jp/suishin/cgi-bin/catalogue/make_product_page.cgi?id=Jishin
- JMA catalog, `緊急地震速報`:
  https://www.data.jma.go.jp/suishin/cgi-bin/catalogue/make_product_page.cgi?id=EEW
- JMA catalog, `長周期地震動情報`:
  https://www.data.jma.go.jp/suishin/cgi-bin/catalogue/make_product_page.cgi?id=Jchoshuki
- JMA catalog, `地震・津波・火山に関するお知らせ`:
  https://www.data.jma.go.jp/suishin/cgi-bin/catalogue/make_product_page.cgi?id=JiKaOshi
