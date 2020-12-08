package crawling;

import duplicateDetection.UrlWithSimHash;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import main.Config;

import java.net.URI;
import java.util.*;

public class Storage {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;

    public Storage() {
        client = RedisClient.create("redis://localhost");
        connection = client.connect();
    }

    public List<String> getAllKeys() {
        return connection.sync().keys("*");
    }

    public String getValueByKey(String key, String field) {
        return connection.sync().hget(key, field);
    }

    public void close() {
        this.connection.close();
        this.client.shutdown();
    }

    public void addClusterIds(ArrayList<ArrayList<UrlWithSimHash>> clusters) {
        int i = 0;
        for (ArrayList<UrlWithSimHash> cluster : clusters) {
            String clusterID = "" + i;
            for (UrlWithSimHash url : cluster) {
                connection.async().sadd("cluster" + clusterID, url.getUrl());
                connection.async().hset(url.getUrl(), Config.CLUSTER_FIELD_NAME, clusterID);
            }
            i += 1;
        }
    }

    public void insertCrawlResult(URI uri, String content) {
        // https://lettuce.io/lettuce-4/release/api/com/lambdaworks/redis/api/sync/RedisHashCommands.html
        // try to use h set to set url as key and date, content and hash as values
        Map<String, String> map = new HashMap<>();
        map.put(Config.CONTENT_FIELD_NAME, content);
        map.put(Config.DATE_FIELD_NAME, (new Date()).toString());
        map.put(Config.SIMHASH_FIELD_NAME, SimHash.simHash(content).toString());
        connection.async().hmset(escapeSpecialCharacters(uri.toString()), map);
        System.out.println(uri);
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

}
