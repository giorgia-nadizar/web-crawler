package duplicateDetection;

import crawling.SimHash;
import org.christopherfrantz.dbscan.DistanceMetric;

// distance metrics to be used in the clustering
public class UrlSimHashDistanceMetrics implements DistanceMetric<UrlWithSimHash> {

    @Override
    public double calculateDistance(UrlWithSimHash val1, UrlWithSimHash val2) {
        return SimHash.getSemblance(val1.getSimHash(), val2.getSimHash());
    }

}
