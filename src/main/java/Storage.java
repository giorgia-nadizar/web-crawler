import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.net.URI;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Storage {

    // FUTURE DEVELOPMENT IDEAS
    // each thread could use their own file to write onto (no need for sync)
    // at the end of the process each file will be closed
    // and they will be merged into one common file

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;

    public Storage() {
        client = RedisClient.create("redis://localhost");
        connection = client.connect();
    }

    public void close() {
        this.connection.close();
        this.client.shutdown();
    }

    public void insertCrawlResult(URI uri, String content) {
        System.out.println(uri);
        connection.async().set(uri.toString(), convertToCSV(content));
        //uncomment next line to add timestamping
        //connection.async().set(uri.toString() + ":time", convertToCSV(content));
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private static String convertToCSV(String... data) {
        return Stream.of(data)
                .map(Storage::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

}
