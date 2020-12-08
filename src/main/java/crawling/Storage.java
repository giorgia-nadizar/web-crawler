package crawling;

import duplicateDetection.UrlWithSimHash;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import main.Config;

import java.net.URI;
import java.util.*;

// represents with redis storage
public class Storage {

    private final RedisClient client;
    private final StatefulRedisConnection<String, String> connection;

    // opens the connection with redis
    public Storage() {
        client = RedisClient.create("redis://localhost");
        connection = client.connect();
    }

    // closes connection with redis
    public void close() {
        this.connection.close();
        this.client.shutdown();
    }

    // inserts the uri as a key and the content, the simHash and the date as fields
    public void insertCrawlResult(URI uri, String content) {
        Map<String, String> map = new HashMap<>();
        map.put(Config.CONTENT_FIELD_NAME, content);
        map.put(Config.DATE_FIELD_NAME, (new Date()).toString());
        map.put(Config.SIMHASH_FIELD_NAME, SimHash.simHash(content).toString());
        connection.async().hmset(uri.toString(), map);
        System.out.println(uri);
    }

    public List<String> getAllKeys() {
        return connection.sync().keys("*");
    }

    public String getValueByKey(String key, String field) {
        return connection.sync().hget(key, field);
    }

    // adds clusterId attribute to all urls that appear in a cluster and creates a set for each cluster
    public void addClusterIds(ArrayList<ArrayList<UrlWithSimHash>> clusters) {
        int id = 0;
        for (ArrayList<UrlWithSimHash> cluster : clusters) {
            String clusterID = "" + id;
            for (UrlWithSimHash url : cluster) {
                connection.async().sadd("cluster" + clusterID, url.getUrl());   // index to ease research by clusterID
                connection.async().hset(url.getUrl(), Config.CLUSTER_FIELD_NAME, clusterID);
            }
            id += 1;
        }
    }

}
