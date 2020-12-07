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

    @Override
    public void run() {
        System.out.println("Hi! I am thread " + currentThread().getId());
        while (System.currentTimeMillis() < Config.STOP_TIME_MILLIS) {
            URI uri = frontier.getNextURL();
            if (uri == null) {
                System.out.println("Empty frontier, thread " + currentThread().getId() + " will stop here");
                break;
            }
            long responseTime = Config.MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS;
            WebPage webPage = WebPageDownloader.fetch(uri);
            // it might be null because not allowed by robots.txt or because of errors
            if (webPage != null) {
                Set<String> links = Collections.newSetFromMap(new ConcurrentHashMap<>());
                // forward the content to the parser
                String content = Parser.parse(webPage.getDocument(), links);
                responseTime = webPage.getResponseTime();
                // filter all found urls and add them to the frontier
                visitedPages.filterAlreadyVisitedUrls(links);
                frontier.insertURLs(links);
                if (visitedPages.addAndReturnIfModified(uri, webPage.getLastModified()) && content != null) {
                    storage.insertCrawlResult(uri, content);
                }
            }
            frontier.removeVisitedURI(uri);
            frontier.addVisitedHostWithDelayForNextVisit(uri.getHost(), 10 * responseTime);
        }
        System.out.println("Bye! Spider thread " + currentThread().getId() + " stops here.");
    }

}
