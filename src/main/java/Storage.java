import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;

import java.net.URI;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Storage {

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
        // https://lettuce.io/lettuce-4/release/api/com/lambdaworks/redis/api/sync/RedisHashCommands.html
        // try to use hset to set url as key and date, content and hash as values
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
