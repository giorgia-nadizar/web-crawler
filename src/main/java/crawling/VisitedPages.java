package crawling;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

public class VisitedPages {

    private final SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    private final PriorityBlockingQueue<VisitedPage> visitedPages;
    private final ConcurrentHashMap<URI, VisitedPage> pendingPages;

    public VisitedPages() {
        visitedPages = new PriorityBlockingQueue<>();
        pendingPages = new ConcurrentHashMap<>();
    }

    // this method doesn't need to be synchronized as only one thread will access it
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

    public boolean addAndReturnIfModified(URI uri, String lastModified) {
        boolean modified = true;
        Date lastMod = null;
        if (lastModified != null) {
            try {
                synchronized (format) {
                    lastMod = format.parse(lastModified);
                }
            } catch (ParseException e) {
                //e.printStackTrace();
            }
        }
        VisitedPage visitedPage;
        if ((visitedPage = pendingPages.get(uri)) != null) {
            modified = visitedPage.update(lastMod);
        } else {
            visitedPage = new VisitedPage(uri, lastMod);
        }
        // this row will probably never get executed, just make sure we avoid duplications
        visitedPages.remove(visitedPage);
        visitedPages.put(visitedPage);
        return modified;
    }

    public void filterAlreadyVisitedUrls(Set<String> urls) {
        urls.removeIf(this::alreadyVisited);
    }

    //receives URLS
    //for each checks if it has been visited already
    //then checks if the host has a robot policy specified (we'll have a table for that)
    //then forwards them to the frontier to add them
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
