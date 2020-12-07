package duplicateDetection;

import crawling.Config;
import crawling.SimHash;
import crawling.Storage;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuplicateFinder {

    private final Map<UrlCouple, Double> similarityMatrix;

    public DuplicateFinder(Storage storage) {
        List<String> storedUrls = storage.getAllKeys();
        similarityMatrix = new HashMap<>();
        for (String url1 : storedUrls) {
            BigInteger simHash1 = new BigInteger(storage.getValueByKey(url1, Config.SIMHASH_FIELD_NAME));
            for (String url2 : storedUrls) {
                if (url1.equals(url2)) {
                    continue;
                }
                BigInteger simHash2 = new BigInteger(storage.getValueByKey(url2, Config.SIMHASH_FIELD_NAME));
                similarityMatrix.put(UrlCouple.create(url1, url2), SimHash.getSemblance(simHash1, simHash2));
            }
        }
    }

    public void filter() {
        filter(0.8);
    }

    public void filter(double threshold) {
        for (Map.Entry<UrlCouple, Double> entry : similarityMatrix.entrySet()) {
            double semblance = entry.getValue();
            if (semblance >= threshold) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
        }
    }

}
