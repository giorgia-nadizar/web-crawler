package duplicateDetection;

import main.Config;
import crawling.Storage;
import org.christopherfrantz.dbscan.DBSCANClusterer;    // from https://github.com/chrfrantz/DBSCAN
import org.christopherfrantz.dbscan.DBSCANClusteringException;  // from https://github.com/chrfrantz/DBSCAN

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

// clusters urls according to the hamming distance between them
public class DuplicateFinder {

    Storage storage;
    List<UrlWithSimHash> simHashes;

    public DuplicateFinder(Storage storage) {
        this.storage = storage;
        loadUrlsAndSimHashes();
    }

    // retrieves all stored urls and corresponding simHash values in memory
    private void loadUrlsAndSimHashes() {
        List<String> storedUrls = storage.getAllKeys();
        simHashes = new ArrayList<>();
        for (String url : storedUrls) {
            simHashes.add(
                    new UrlWithSimHash(url,
                            new BigInteger(storage.getValueByKey(url, Config.SIMHASH_FIELD_NAME))));
        }
    }

    // performs clustering (with DBSCAN) of the urls and updates the storage
    public ArrayList<ArrayList<UrlWithSimHash>> cluster() {
        try {
            DBSCANClusterer<UrlWithSimHash> clusterer =
                    new DBSCANClusterer<>(simHashes, Config.MIN_PTS, Config.MAX_DISTANCE, new UrlSimHashDistanceMetrics());
            ArrayList<ArrayList<UrlWithSimHash>> clusters = clusterer.performClustering();
            storage.addClusterIds(clusters);
            return clusters;
        } catch (DBSCANClusteringException e) {
            return null;
        }
    }

    // performs clustering and prints the results
    public void clusterAndPrint() {
        ArrayList<ArrayList<UrlWithSimHash>> clusters = cluster();
        System.out.println("Clusters found: " + clusters.size());
        for (ArrayList<UrlWithSimHash> cluster : clusters) {
            System.out.println(cluster);
        }
    }

}
