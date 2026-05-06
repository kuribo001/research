package com.twins.crawler.parsers.earthquake;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

final class JmaXml {
    private JmaXml() {
    }

    static Document parse(String xml) {
        try {
            if (xml == null || xml.isBlank()) {
                throw new EarthquakeParserException("XML payload must not be blank");
            }
            DocumentBuilderFactory factory = newSecureFactory();
            return factory.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new EarthquakeParserException("Failed to parse JMA XML", exception);
        }
    }

    static Element requiredDirectChild(Element parent, String localName) {
        Element child = directChild(parent, localName);
        if (child == null) {
            throw new EarthquakeParserException("Missing required node " + localName);
        }
        return child;
    }

    static Element directChild(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element && localName.equals(element.getLocalName())) {
                return element;
            }
        }
        return null;
    }

    static List<Element> directChildren(Element parent, String localName) {
        List<Element> elements = new ArrayList<>();
        if (parent == null) {
            return elements;
        }
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element && localName.equals(element.getLocalName())) {
                elements.add(element);
            }
        }
        return elements;
    }

    static Element firstDescendant(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        if (localName.equals(parent.getLocalName())) {
            return parent;
        }
        NodeList descendants = parent.getElementsByTagNameNS("*", localName);
        return descendants.getLength() == 0 ? null : (Element) descendants.item(0);
    }

    static List<Element> descendants(Element parent, String localName) {
        List<Element> elements = new ArrayList<>();
        if (parent == null) {
            return elements;
        }
        NodeList descendants = parent.getElementsByTagNameNS("*", localName);
        for (int i = 0; i < descendants.getLength(); i++) {
            elements.add((Element) descendants.item(i));
        }
        return elements;
    }

    static String directChildText(Element parent, String localName) {
        if (localName == null) {
            return parent == null ? null : blankToNull(parent.getTextContent());
        }
        Element child = directChild(parent, localName);
        return child == null ? null : blankToNull(child.getTextContent());
    }

    static String attributeValue(Element element, String attributeName) {
        if (element == null || attributeName == null || !element.hasAttribute(attributeName)) {
            return null;
        }
        return blankToNull(element.getAttribute(attributeName));
    }

    static OffsetDateTime parseOffsetDateTime(String value) {
        return value == null ? null : OffsetDateTime.parse(value);
    }

    static Integer parseInteger(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static Double parseDouble(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.valueOf(value.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    static Double parseCoordinateLatitude(String coordinateRaw) {
        CoordinateParts parts = CoordinateParts.parse(coordinateRaw);
        return parts == null ? null : parts.latitude;
    }

    static Double parseCoordinateLongitude(String coordinateRaw) {
        CoordinateParts parts = CoordinateParts.parse(coordinateRaw);
        return parts == null ? null : parts.longitude;
    }

    static Integer parseCoordinateDepthMeters(String coordinateRaw) {
        CoordinateParts parts = CoordinateParts.parse(coordinateRaw);
        return parts == null ? null : parts.depthMeters;
    }

    private static DocumentBuilderFactory newSecureFactory() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory;
        } catch (Exception exception) {
            throw new EarthquakeParserException("Failed to initialize secure XML parser", exception);
        }
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record CoordinateParts(Double latitude, Double longitude, Integer depthMeters) {
        private static CoordinateParts parse(String coordinateRaw) {
            if (coordinateRaw == null || coordinateRaw.isBlank()) {
                return null;
            }
            try {
                String[] parts = coordinateRaw.split("/");
                String numeric = parts[0];
                int lonStart = numeric.indexOf('+', 1);
                if (lonStart < 0) {
                    lonStart = numeric.indexOf('-', 1);
                }
                int depthStart = numeric.indexOf('-', lonStart + 1);
                if (depthStart < 0) {
                    return null;
                }
                double latitude = normalizeCoordinate(Double.parseDouble(numeric.substring(0, lonStart)), true);
                double longitude = normalizeCoordinate(Double.parseDouble(numeric.substring(lonStart, depthStart)), false);
                int depth = Integer.parseInt(numeric.substring(depthStart + 1));
                return new CoordinateParts(latitude, longitude, depth);
            } catch (RuntimeException ignored) {
                return null;
            }
        }

        private static double normalizeCoordinate(double rawValue, boolean latitude) {
            double limit = latitude ? 90.0 : 180.0;
            double absolute = Math.abs(rawValue);
            if (absolute <= limit) {
                return rawValue;
            }

            double sign = Math.signum(rawValue);
            double unsigned = Math.abs(rawValue);
            double degrees = Math.floor(unsigned / 100.0);
            double minutes = unsigned - (degrees * 100.0);
            return sign * (degrees + (minutes / 60.0));
        }
    }
}
