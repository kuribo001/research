# JMA Earthquake Sample Index

- Updated on: 2026-05-06
- Purpose: track earthquake-related XML fixtures currently available in the repository, across both official JMA samples and downloaded real-world XML.

## Sources

- Official sample pack:
  - `jmaxml_20260326_Samples/`
- Real-world downloaded XML:
  - `downloads/earthquake_samples/xml/`

## Notes

- The official sample pack is the main source for parser development and branch coverage.
- The downloaded XML set is the realism and regression source.
- Real-world downloads currently cover only:
  - `VXSE51`
  - `VXSE52`
  - `VXSE53`
  - `VXSE61`
  - `VXSE62`
- `VXSE47` is still not present in either sample source.
- Official sample counts and `InfoType` coverage below were checked directly from XML files in the repository.

## Coverage Matrix

| Message Code | Family | Representative Title | Official Samples | Real-world Samples | Official `InfoType` Coverage | Recommended Representative Fixture |
| --- | --- | --- | ---: | ---: | --- | --- |
| `VXSE42` | `EEW` | `緊急地震速報配信テスト` | 1 | 0 | `発表` | [54_01_01_100514_VXSE42.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/54_01_01_100514_VXSE42.xml) |
| `VXSE43` | `EEW` | `緊急地震速報（警報）` | 3 | 0 | `発表`, `取消` | [37_01_01_240613_VXSE43.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/37_01_01_240613_VXSE43.xml) |
| `VXSE44` | `EEW` | `緊急地震速報（予報）` | 33 | 0 | `発表`, `取消` | [36_01_01_240613_VXSE44.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/36_01_01_240613_VXSE44.xml) |
| `VXSE45` | `EEW` | `緊急地震速報（地震動予報）` | 33 | 0 | `発表`, `取消` | [77_01_01_240613_VXSE45.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/77_01_01_240613_VXSE45.xml) |
| `VXSE47` | `RealTimeIntensity` | n/a | 0 | 0 | none | missing |
| `VXSE51` | `SeismicIntensity` | `震度速報` | 16 | 9 | `発表`, `取消` | Official: [32-35_01_01_100806_VXSE51.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/32-35_01_01_100806_VXSE51.xml) / Real-world: [20260422000304_0_VXSE51_010000.xml](/Users/account/Desktop/works/FPT/test/downloads/earthquake_samples/xml/20260422000304_0_VXSE51_010000.xml) |
| `VXSE52` | `Hypocenter` | `震源に関する情報` | 6 | 8 | `発表`, `取消` | Official: [32-35_01_02_240613_VXSE52.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/32-35_01_02_240613_VXSE52.xml) / Real-world: [20260422000431_0_VXSE52_010000.xml](/Users/account/Desktop/works/FPT/test/downloads/earthquake_samples/xml/20260422000431_0_VXSE52_010000.xml) |
| `VXSE53` | `HypocenterSeismic` | `震源・震度に関する情報` | 22 | 82 | `発表`, `取消` | Official: [32-35_01_03_100514_VXSE53.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/32-35_01_03_100514_VXSE53.xml) / Real-world: [20260421110200_0_VXSE53_010000.xml](/Users/account/Desktop/works/FPT/test/downloads/earthquake_samples/xml/20260421110200_0_VXSE53_010000.xml) |
| `VXSE56` | `EarthquakeActivity` | `地震の活動状況等に関する情報` | 14 | 0 | `発表`, `取消` | [32-35_09_01_191111_VXSE56.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/32-35_09_01_191111_VXSE56.xml) |
| `VXSE60` | `EarthquakeCount` | `地震回数に関する情報` | 2 | 0 | `発表`, `取消` | [32-35_03_01_100514_VXSE60.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/32-35_03_01_100514_VXSE60.xml) |
| `VXSE61` | `HypocenterUpdate` | `顕著な地震の震源要素更新のお知らせ` | 4 | 1 | `発表`, `取消` | Official: [32-35_03_02_240613_VXSE61.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/32-35_03_02_240613_VXSE61.xml) / Real-world: [20260426223022_0_VXSE61_010000.xml](/Users/account/Desktop/works/FPT/test/downloads/earthquake_samples/xml/20260426223022_0_VXSE61_010000.xml) |
| `VXSE62` | `LongPeriodGroundMotion` | `長周期地震動に関する観測情報` | 1 | 1 | `発表` | Official: [78_01_01_240613_VXSE62.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/78_01_01_240613_VXSE62.xml) / Real-world: [20260426203422_0_VXSE62_010000.xml](/Users/account/Desktop/works/FPT/test/downloads/earthquake_samples/xml/20260426203422_0_VXSE62_010000.xml) |
| `VYSE50` | `Nankai` | `南海トラフ地震臨時情報（巨大地震注意）` | 8 | 0 | `発表`, `取消` | [74_01_01_200512_VYSE50.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/74_01_01_200512_VYSE50.xml) |
| `VYSE51` | `Nankai` | `南海トラフ地震関連解説情報` | 3 | 0 | `発表` | [75_01_01_200512_VYSE51.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/75_01_01_200512_VYSE51.xml) |
| `VYSE52` | `Nankai` | `南海トラフ地震関連解説情報` | 1 | 0 | `発表` | [75_01_04_200512_VYSE52.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/75_01_04_200512_VYSE52.xml) |
| `VYSE60` | `SubsequentEarthquakeAdvisory` | `北海道・三陸沖後発地震注意情報` | 1 | 0 | `発表` | [80_01_01_240821_VYSE60.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/80_01_01_240821_VYSE60.xml) |
| `VZSE40` | `EarthquakeTsunamiNotice` | `地震・津波に関するお知らせ` | 2 | 0 | `発表`, `取消` | [42_01_01_100514_VZSE40.xml](/Users/account/Desktop/works/FPT/test/jmaxml_20260326_Samples/42_01_01_100514_VZSE40.xml) |

## Practical Priorities

### Phase 1 parser-first families

- `VXSE52`
- `VXSE53`
- `VXSE51`
- `VXSE61`
- `VXSE62`
- `VXSE60`
- `VXSE56`

Reason:

- these families already have enough official fixtures to build parser shape
- several also have real-world regression samples
- they define the event, hypocenter, intensity, count, and long-period core model

### Phase 2 families after core event model is stable

- `VXSE42`
- `VXSE43`
- `VXSE44`
- `VXSE45`
- `VYSE50`
- `VYSE51`
- `VYSE52`
- `VYSE60`
- `VZSE40`

Reason:

- these are well-covered by the official sample pack
- they add EEW, Nankai, and notice-style logic
- they are slightly more specialized than the core earthquake event families

### Missing or weak coverage

- `VXSE47`
  - no fixture currently present in either source
  - parser design for realtime station intensity should stay provisional until a real or official sample is added
- real-world coverage is still missing for:
  - `VXSE42`
  - `VXSE43`
  - `VXSE44`
  - `VXSE45`
  - `VXSE56`
  - `VXSE60`
  - `VYSE50`
  - `VYSE51`
  - `VYSE52`
  - `VYSE60`
  - `VZSE40`

## Coverage Summary

- Official sample pack provides broad functional coverage and multiple `取消` cases.
- Real-world downloads provide strong depth for:
  - `VXSE53`
  - `VXSE52`
  - `VXSE51`
- `VXSE61` and `VXSE62` have at least one real-world regression sample each.
- The current repository is ready to move from schema/DTO work into parser family scaffolding.
