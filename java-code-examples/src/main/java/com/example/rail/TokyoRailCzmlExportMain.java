package com.example.rail;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public final class TokyoRailCzmlExportMain {
    private TokyoRailCzmlExportMain() {
    }

    public static void main(String[] args) throws Exception {
        Path outputDirectory = args.length > 0
                ? Path.of(args[0])
                : TokyoRailCzmlExportConfig.DEFAULT_OUTPUT_DIRECTORY;
        double height = args.length > 1 ? Double.parseDouble(args[1]) : 0.0;
        List<String> operatorFilters = args.length > 2
                ? Arrays.asList(Arrays.copyOfRange(args, 2, args.length))
                : TokyoRailCzmlExportConfig.DEFAULT_OPERATOR_FILTERS;

        TokyoRailCzmlExportService service = new TokyoRailCzmlExportService();
        List<Path> files = service.exportCzmlPerLine(outputDirectory, height, operatorFilters);

        System.out.println("CZML directory: " + outputDirectory.toAbsolutePath());
        System.out.println("Files created: " + files.size());
        System.out.println("Operator filters: " + operatorFilters);
    }
}
