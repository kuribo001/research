package com.twins.crawler.parsers.earthquake;

import java.util.Arrays;
import java.util.List;

public enum EarthquakeMessageFamily {
    EEW(List.of("VXSE42", "VXSE43", "VXSE44", "VXSE45")),
    REAL_TIME_INTENSITY(List.of("VXSE47")),
    SEISMIC_INTENSITY(List.of("VXSE51")),
    HYPOCENTER(List.of("VXSE52")),
    HYPOCENTER_SEISMIC(List.of("VXSE53")),
    EARTHQUAKE_ACTIVITY(List.of("VXSE56")),
    EARTHQUAKE_COUNT(List.of("VXSE60")),
    HYPOCENTER_UPDATE(List.of("VXSE61")),
    LONG_PERIOD_GROUND_MOTION(List.of("VXSE62")),
    NANKAI(List.of("VYSE50", "VYSE51", "VYSE52")),
    SUBSEQUENT_EARTHQUAKE_ADVISORY(List.of("VYSE60")),
    EARTHQUAKE_TSUNAMI_NOTICE(List.of("VZSE40"));

    private final List<String> messageCodes;

    EarthquakeMessageFamily(List<String> messageCodes) {
        this.messageCodes = messageCodes;
    }

    public List<String> messageCodes() {
        return messageCodes;
    }

    public static EarthquakeMessageFamily fromMessageCode(String messageCode) {
        return Arrays.stream(values())
            .filter(family -> family.messageCodes.contains(messageCode))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unsupported earthquake message code: " + messageCode));
    }
}
