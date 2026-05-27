package com.example.rail;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ToeiGtfsExample {
    public static void main(String[] args) throws IOException {
        Path gtfsDir = args.length > 0 ? Path.of(args[0]) : Path.of("Toei-Train-GTFS");

        List<Map<String, String>> stops = CsvTable.read(gtfsDir.resolve("stops.txt"));
        List<Map<String, String>> routes = CsvTable.read(gtfsDir.resolve("routes.txt"));
        List<Map<String, String>> trips = CsvTable.read(gtfsDir.resolve("trips.txt"));
        List<Map<String, String>> stopTimes = CsvTable.read(gtfsDir.resolve("stop_times.txt"));

        Map<String, Map<String, String>> stopsById = indexBy(stops, "stop_id");
        Map<String, Map<String, String>> routesById = indexBy(routes, "route_id");
        Map<String, List<Map<String, String>>> stopTimesByTrip = stopTimes.stream()
                .collect(Collectors.groupingBy(row -> row.get("trip_id")));

        for (List<Map<String, String>> rows : stopTimesByTrip.values()) {
            rows.sort(Comparator.comparingInt(row -> Integer.parseInt(row.get("stop_sequence"))));
        }

        System.out.println("GTFS directory: " + gtfsDir.toAbsolutePath());
        System.out.println("stops: " + stops.size());
        System.out.println("routes: " + routes.size());
        System.out.println("trips: " + trips.size());
        System.out.println("stop_times: " + stopTimes.size());
        System.out.println();

        System.out.println("Routes:");
        for (Map<String, String> route : routes) {
            long tripCount = trips.stream()
                    .filter(trip -> route.get("route_id").equals(trip.get("route_id")))
                    .count();
            System.out.printf(
                    "- route_id=%s name=%s type=%s trips=%d%n",
                    route.get("route_id"),
                    route.get("route_long_name"),
                    route.get("route_type"),
                    tripCount
            );
        }

        System.out.println();
        System.out.println("Sample trips with inferred origin/destination:");

        List<Map<String, String>> sampleTrips = new ArrayList<>(trips.subList(0, Math.min(10, trips.size())));
        for (Map<String, String> trip : sampleTrips) {
            List<Map<String, String>> rows = stopTimesByTrip.getOrDefault(trip.get("trip_id"), List.of());
            if (rows.isEmpty()) {
                continue;
            }

            Map<String, String> firstStopTime = rows.get(0);
            Map<String, String> lastStopTime = rows.get(rows.size() - 1);
            Map<String, String> originStop = stopsById.get(firstStopTime.get("stop_id"));
            Map<String, String> destinationStop = stopsById.get(lastStopTime.get("stop_id"));
            Map<String, String> route = routesById.get(trip.get("route_id"));

            System.out.printf(
                    "- trip_id=%s route=%s headsign=%s direction=%s origin=%s %s destination=%s %s stops=%d%n",
                    trip.get("trip_id"),
                    route == null ? trip.get("route_id") : route.get("route_long_name"),
                    trip.get("trip_headsign"),
                    trip.get("direction_id"),
                    stopName(originStop),
                    firstStopTime.get("departure_time"),
                    stopName(destinationStop),
                    lastStopTime.get("arrival_time"),
                    rows.size()
            );
        }
    }

    private static Map<String, Map<String, String>> indexBy(List<Map<String, String>> rows, String key) {
        Map<String, Map<String, String>> index = new HashMap<>();
        for (Map<String, String> row : rows) {
            index.put(row.get(key), row);
        }
        return index;
    }

    private static String stopName(Map<String, String> stop) {
        if (stop == null) {
            return "(unknown)";
        }
        return stop.get("stop_name") + "(" + stop.get("stop_id") + ")";
    }
}
