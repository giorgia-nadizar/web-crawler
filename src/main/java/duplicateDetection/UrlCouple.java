package duplicateDetection;

import java.util.Objects;

public class UrlCouple {

    private final String uri1;
    private final String uri2;

    public UrlCouple(String uri1, String uri2) {
        if (uri1.compareTo(uri2) < 0) {
            this.uri1 = uri1;
            this.uri2 = uri2;
        } else {
            this.uri1 = uri2;
            this.uri2 = uri1;
        }
    }

    public static UrlCouple create(String uri1, String uri2) {
        return new UrlCouple(uri1, uri2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UrlCouple urlCouple = (UrlCouple) o;
        return Objects.equals(uri1, urlCouple.uri1) &&
                Objects.equals(uri2, urlCouple.uri2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uri1, uri2);
    }

    @Override
    public String toString() {
        return uri1 + " - " + uri2;
    }
}
