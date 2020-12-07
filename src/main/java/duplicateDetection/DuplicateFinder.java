package duplicateDetection;

import crawling.Config;
import crawling.Storage;
import org.christopherfrantz.dbscan.DBSCANClusterer;
import org.christopherfrantz.dbscan.DBSCANClusteringException;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class DuplicateFinder {

    Storage storage;
    List<UrlWithSimHash> simHashes;

    public DuplicateFinder(Storage storage) {
        this.storage = storage;
        loadUrlsAndSimHashes();
    }

    private void loadUrlsAndSimHashes() {
        List<String> storedUrls = storage.getAllKeys();
        simHashes = new ArrayList<>();
        for (String url : storedUrls) {
            simHashes.add(
                    new UrlWithSimHash(url,
                            new BigInteger(storage.getValueByKey(url, Config.SIMHASH_FIELD_NAME))));
        }
    }

    public ArrayList<ArrayList<UrlWithSimHash>> cluster() {
        try {
            DBSCANClusterer<UrlWithSimHash> clusterer =
                    new DBSCANClusterer<>(simHashes, Config.MIN_PTS, Config.MAX_DISTANCE, new UrlSimHashDistanceMetrics());
            ArrayList<ArrayList<UrlWithSimHash>> clusters = clusterer.performClustering();
            storage.addClusterIds(clusters);
            return clusters;
        } catch (DBSCANClusteringException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void clusterAndPrint() {
        ArrayList<ArrayList<UrlWithSimHash>> clusters = cluster();
        System.out.println("Clusters found: " + clusters.size());
        for (ArrayList<UrlWithSimHash> cluster : clusters) {
            System.out.println(cluster);
        }
    }

}
