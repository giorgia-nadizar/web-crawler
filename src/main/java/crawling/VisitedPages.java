package crawling;

import main.Config;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

// in memory storage of visited pages to keep track of their last-visited time and next estimated crawl
public class VisitedPages {

    private final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    private final PriorityBlockingQueue<VisitedPage> visitedPages;
    private final ConcurrentHashMap<URI, VisitedPage> pendingPages;     // visited but currently in the frontier to be re-crawled

    public VisitedPages() {
        visitedPages = new PriorityBlockingQueue<>();
        pendingPages = new ConcurrentHashMap<>();
    }

    // returns the visited page with minimum re-crawl date or null if the re-crawl date is too far in time
    // not synchronized because there is only one refresher thread
    public URI getNextPageToRefresh() {
        VisitedPage page = visitedPages.peek();
        if (page == null || (page.getNextScheduledCrawl().getTime() - (new Date()).getTime()) > Config.MAX_WAIT_REFRESHER) {
            return null;
        }
        visitedPages.remove(page);
        pendingPages.put(page.getUrl(), page);
        // wait necessary time
        try {
            Thread.sleep(Math.max(0, page.getNextScheduledCrawl().getTime() - (new Date()).getTime()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return page.getUrl();
    }

    public boolean addIfAbsentOrModified(URI uri, String lastModified) {
        boolean newOrUpdated = true;
        Date lastModifiedDate = null;
        if (lastModified != null) {
            try {
                synchronized (format) {
                    lastModifiedDate = format.parse(lastModified);
                }
            } catch (ParseException e) {
                lastModifiedDate = null;
            }
        }
        VisitedPage visitedPage = pendingPages.get(uri);
        if (visitedPage != null) {
            newOrUpdated = visitedPage.update(lastModifiedDate);
        } else {
            visitedPage = new VisitedPage(uri, lastModifiedDate);
        }
        visitedPages.remove(visitedPage);   // make sure duplications are avoided
        visitedPages.put(visitedPage);
        return newOrUpdated;
    }

    // removes already visited urls
    public void filterAlreadyVisitedUrls(Set<String> urls) {
        urls.removeIf(this::alreadyVisited);
    }

    // returns true if the url is visited (or pending -> already in the frontier again)
    private boolean alreadyVisited(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            //e.printStackTrace();
            return true;
        }
        return visitedPages.contains(new VisitedPage(uri)) || pendingPages.containsKey(uri);
    }

}
