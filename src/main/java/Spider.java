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

    private Frontier frontier;
    private VisitedPages visitedPages;
    private static int WAIT_BEFORE_QUIT_MILLIS = 20000;

    public Spider(Frontier frontier, VisitedPages visitedPages) {
        this.frontier = frontier;
        this.visitedPages = visitedPages;
    }

    @Override
    public void run() {
        System.out.println("Hi! I am thread " + currentThread().getId());
        while (true) {
            URI uri = frontier.getNextURL();
            if (uri == null) {
                System.out.println("Empty frontier, thread " + currentThread().getId() + " will stop here");
                break;
            }
            // check if allowed by robots.txt
            try (InputStream robotsTxtStream = new URL("https://" + uri.getHost() + "/robots.txt").openStream()) {
                RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);
                if (!robotsTxt.query(null, uri.getPath())) {
                    // robots.txt doesn't grant access to this url
                    continue;
                }
            } catch (IOException e) {
                // if we get here there is no existing robots.txt file for that host
                //e.printStackTrace();
            }
            //System.out.println(uri);
            HttpConnection connection = new HttpConnection();
            connection.url(uri.toString());
            //if a redirection occurs, it stops
            connection.followRedirects(false);
            connection.ignoreHttpErrors(true);
            Connection.Response response;
            long responseTime = 0;
            String lastModified = null;
            try {
                long startTime = System.currentTimeMillis();
                response = connection.execute();
                long stopTime = System.currentTimeMillis();
                responseTime = stopTime - startTime;
                int responseCode = response.statusCode();
                lastModified = response.header("Last-Modified");
                //System.out.println(responseCode);
                if (responseCode >= 300 && responseCode < 400) {
                    System.out.println("Redirection to " + response.header("Location"));
                }
                if (responseCode >= 200 && responseCode < 300) {
                    Document document = response.parse();
                    Set<String> links = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    // forward the content to the parser
                    String content = Parser.parse(document, links);
                    Storage.insertInDB(uri, content);
                    // filter all found urls and add them to the frontier
                    visitedPages.contain(links);
                    frontier.insertURLS(links);
                }
            } catch (IOException e) {
                // we can fall here for various reasons, for example the content of the
                // page is not supported (not text/* or application/xml or application/*+xml)
                e.printStackTrace();
            } finally {
                // add to visited urls
                visitedPages.add(uri, lastModified);
                frontier.removeFromPending(uri);
                frontier.updateHeap(uri.getHost(), responseTime);
            }
        }
    }

}
