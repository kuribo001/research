async function loadRailLines(viewer, url) {
  const response = await fetch(url);
  const data = await response.json();

  for (const prefecture of data.prefectures) {
    for (const line of prefecture.lines) {
      for (const section of line.sections) {
        const flattened = section.coordinates.flat();
        viewer.entities.add({
          name: `${line.operator_name || ""} ${line.line_name || ""}`.trim(),
          polyline: {
            positions: Cesium.Cartesian3.fromDegreesArrayHeights(flattened),
            width: 3,
            material: Cesium.Color.CYAN,
            clampToGround: false,
          },
          properties: {
            prefectureKey: prefecture.prefecture_key,
            prefectureName: prefecture.prefecture_name,
            lineName: line.line_name,
            operatorName: line.operator_name,
            sectionId: section.section_id,
          },
        });
      }
    }
  }
}

/*
Usage:

const viewer = new Cesium.Viewer("cesiumContainer");
loadRailLines(viewer, "./line_export_tool/rail_lines_tokyo.json");
*/
