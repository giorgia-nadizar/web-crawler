import com.panforge.robotstxt.RobotsTxt;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
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

    // checks if the given URI is allowed by robots.txt
    private boolean isAllowedByRobots(URI uri) {
        try (InputStream robotsTxtStream = new URL("https://" + uri.getHost() + "/robots.txt").openStream()) {
            RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);
            if (!robotsTxt.query(null, uri.getPath())) {
                // robots.txt doesn't grant access to this url
                return false;
            }
        } catch (IOException e) {
            // if we get here there is no existing robots.txt file for that host
            //e.printStackTrace();
        }
        return true;
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
            // if the uri is not allowed, skip it
            // remember to always update the frontier
            if (!isAllowedByRobots(uri)) {
                frontier.removeFromPending(uri);
                frontier.updateHeap(uri.getHost(), Config.MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS);
                continue;
            }
            HttpConnection connection = new HttpConnection();
            connection.url(uri.toString()).followRedirects(false).ignoreHttpErrors(true);
            Connection.Response response;
            long responseTime = 0;
            String lastModified = null;
            String content = null;
            try {
                long startTime = System.currentTimeMillis();
                response = connection.execute();
                long stopTime = System.currentTimeMillis();
                responseTime = stopTime - startTime;
                lastModified = response.header("Last-Modified");
                if (response.statusCode() >= 300 && response.statusCode() < 400) {
                    System.out.println("Redirection to " + response.header("Location"));
                }
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    Document document = response.parse();
                    Set<String> links = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    // forward the content to the parser
                    content = Parser.parse(document, links);
                    // filter all found urls and add them to the frontier
                    visitedPages.filterAlreadyVisitedUrls(links);
                    frontier.insertURLS(links);
                }
            } catch (IOException e) {
                // we can fall here for various reasons, for example the content of the
                // page is not supported (not text/* or application/xml or application/*+xml)
                //e.printStackTrace();
            }
            if (visitedPages.addAndReturnIfModified(uri, lastModified) && content != null) {
                storage.insertCrawlResult(uri, content);
            }
            frontier.removeFromPending(uri);
            frontier.updateHeap(uri.getHost(), 10 * responseTime);
            
        }
        System.out.println("Bye! Spider thread " + currentThread().getId() + " stops here.");
    }

}
