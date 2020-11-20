import com.panforge.robotstxt.RobotsTxt;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Refresher extends Thread {

    private Frontier frontier;
    private VisitedPages visitedPages;

    public Refresher(Frontier frontier, VisitedPages visitedPages) {
        this.frontier = frontier;
        this.visitedPages = visitedPages;
    }

    @Override
    public void run() {
        System.out.println("Hi! I am thread " + currentThread().getId() + ". I will be the refresher!");
        while (true) {
            frontier.insertSeenURLToRefresh(visitedPages.getNextPageToRefresh());
        }
    }

}
