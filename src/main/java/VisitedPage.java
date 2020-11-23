import java.net.URI;
import java.util.Date;
import java.util.Objects;

public class VisitedPage implements Comparable<VisitedPage> {

    private final URI url;
    private Date lastModified;
    private Date nextScheduledCrawl;

    public VisitedPage(URI url, Date lastModified) {
        long waitTimeBeforeNextCrawlMillis;
        this.url = url;
        if (lastModified != null) {
            this.lastModified = lastModified;
            waitTimeBeforeNextCrawlMillis = (new Date()).getTime() - lastModified.getTime();
        } else {
            waitTimeBeforeNextCrawlMillis = Config.DEFAULT_WAIT_TIME_BEFORE_RECRAWL;
        }
        nextScheduledCrawl = new Date(System.currentTimeMillis() + waitTimeBeforeNextCrawlMillis);
    }

    public VisitedPage(URI url) {
        this.url = url;
    }

    public URI getUrl() {
        return url;
    }

    public Date getNextScheduledCrawl() {
        return nextScheduledCrawl;
    }

    public boolean update(Date lastModified) {
        boolean modified = false;
        long waitTimeBeforeNextCrawlMillis;
        if (lastModified == null) {
            this.lastModified = null;
            waitTimeBeforeNextCrawlMillis = Config.DEFAULT_WAIT_TIME_BEFORE_RECRAWL;
        } else {
            // if the page was not modified increase the wait time
            if (this.lastModified.equals(lastModified)) {
                waitTimeBeforeNextCrawlMillis = (new Date()).getTime() - lastModified.getTime();
            } else {
                modified = true;
                // estimate the change rate with the time passed between two modifications
                waitTimeBeforeNextCrawlMillis = lastModified.getTime() - this.lastModified.getTime();
            }
            this.lastModified = lastModified;
        }
        nextScheduledCrawl = new Date(System.currentTimeMillis() + waitTimeBeforeNextCrawlMillis);
        return modified;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VisitedPage that = (VisitedPage) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

    @Override
    public int compareTo(VisitedPage other) {
        return this.getNextScheduledCrawl().compareTo(other.getNextScheduledCrawl());
    }
}
