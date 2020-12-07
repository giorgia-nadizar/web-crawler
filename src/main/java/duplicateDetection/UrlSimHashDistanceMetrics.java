package duplicateDetection;

import crawling.SimHash;
import org.christopherfrantz.dbscan.DistanceMetric;

import java.math.BigInteger;

public class UrlSimHashDistanceMetrics implements DistanceMetric<UrlWithSimHash> {

    @Override
    public double calculateDistance(UrlWithSimHash val1, UrlWithSimHash val2) {
        return SimHash.getSemblance(val1.getSimHash(), val2.getSimHash());
    }

}
