package crawling;

import main.Config;

import java.net.*;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Spider extends Thread {

    private final Frontier frontier;
    private final VisitedPages visitedPages;
    private final Storage storage;

    public Spider(Frontier frontier, VisitedPages visitedPages, Storage storage) {
        this.frontier = frontier;
        this.visitedPages = visitedPages;
        this.storage = storage;
    }

    // implements a web crawler
    @Override
    public void run() {
        System.out.println("Spider thread " + currentThread().getId() + " launched...");    // start notification
        while (!isInterrupted() && System.currentTimeMillis() < Config.STOP_TIME_MILLIS) {
            URI uri = frontier.getNextURL();
            if (uri == null) {
                break;
            }
            long responseTime = Config.MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS;
            WebPage webPage = WebPageDownloader.fetch(uri);
            // might be null because not allowed by robots.txt or because of errors
            if (webPage != null) {
                Set<String> links = Collections.newSetFromMap(new ConcurrentHashMap<>());
                String content = Parser.parse(webPage.getDocument(), links);
                responseTime = webPage.getResponseTime();
                visitedPages.filterAlreadyVisitedUrls(links);
                frontier.insertURLs(links);
                if (visitedPages.addIfAbsentOrModified(uri, webPage.getLastModified()) && content != null) {
                    storage.insertCrawlResult(uri, content);
                }
            }
            frontier.removeVisitedURL(uri);
            frontier.addVisitedHostWithDelayForNextVisit(uri.getHost(), 10 * responseTime);
        }
        System.out.println("Spider thread " + currentThread().getId() + " finished!");    // end notification
    }

}
