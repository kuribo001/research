import com.twins.crawler.dtos.EarthquakeEventEnvelope;
import com.twins.crawler.parsers.earthquake.EarthquakeParsingService;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EarthquakeXmlIngestExample {
    private static final Pattern MESSAGE_CODE_PATTERN = Pattern.compile("(V[XYZ]SE\\d{2})");

    private EarthquakeXmlIngestExample() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            printUsage();
            return;
        }

        String xmlUrl = args[0];
        String jdbcUrl = args[1];
        String dbUser = args[2];
        String dbPassword = args[3];
        String explicitMessageCode = args.length >= 5 ? args[4] : null;

        String xml = downloadXml(xmlUrl);
        String messageCode = explicitMessageCode != null ? explicitMessageCode : inferMessageCode(xmlUrl);
        EarthquakeEventEnvelope envelope = EarthquakeParsingService.parse(messageCode, xml);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPassword)) {
            EarthquakeRepository repository = new EarthquakeRepository(connection);
            long earthquakeEventId = repository.save(envelope);
            System.out.println("Saved earthquake event id=" + earthquakeEventId
                + " messageCode=" + messageCode
                + " eventId=" + envelope.event().head().eventId());
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  java EarthquakeXmlIngestExample <xmlUrl> <jdbcUrl> <dbUser> <dbPassword> [messageCode]");
        System.out.println();
        System.out.println("Example:");
        System.out.println("  java EarthquakeXmlIngestExample \\");
        System.out.println("    https://example.com/20260426202909_0_VXSE53_010000.xml \\");
        System.out.println("    jdbc:postgresql://localhost:5432/jma jma_user secret VXSE53");
    }

    private static String downloadXml(String xmlUrl) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder(URI.create(xmlUrl)).GET().build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Failed to download XML: HTTP " + response.statusCode());
        }
        return response.body();
    }

    private static String inferMessageCode(String text) {
        Matcher matcher = MESSAGE_CODE_PATTERN.matcher(text);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Could not infer JMA message code from: " + text);
        }
        return matcher.group(1);
    }
}
