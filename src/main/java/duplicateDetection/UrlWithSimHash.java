package duplicateDetection;

import java.math.BigInteger;

// utility object to keep urls and simHashes together in the clustering
public class UrlWithSimHash {

    private String url;
    private BigInteger simHash;

    public UrlWithSimHash(String url, BigInteger simHash) {
        this.url = url;
        this.simHash = simHash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public BigInteger getSimHash() {
        return simHash;
    }

    public void setSimHash(BigInteger simHash) {
        this.simHash = simHash;
    }

    @Override
    public String toString() {
        return url;
    }
}
