package crawling;

import main.Config;

import java.net.URI;

public class Refresher extends Thread {

    private final Frontier frontier;
    private final VisitedPages visitedPages;

    public Refresher(Frontier frontier, VisitedPages visitedPages) {
        this.frontier = frontier;
        this.visitedPages = visitedPages;
    }

    // gets a URL to fetch again from the visited ones and adds it to the frontier with high priority
    @Override
    public void run() {
        System.out.println("Refresher thread " + currentThread().getId() + " launched...");    // start notification
        while (!isInterrupted() && System.currentTimeMillis() < Config.STOP_TIME_MILLIS) {
            URI pageToRefresh = visitedPages.getNextPageToRefresh();
            if (pageToRefresh != null) {
                frontier.insertSeenURLToRefresh(pageToRefresh);
                continue;
            }
            // checks if it's ok to sleep or if it will wake too late
            if (!isInterrupted() &&
                    (System.currentTimeMillis() + Config.REFRESHER_WAIT_BEFORE_CHECKING_PAGE_TO_REFRESH_MILLIS)
                            < Config.STOP_TIME_MILLIS) {
                try {
                    Thread.sleep(Config.REFRESHER_WAIT_BEFORE_CHECKING_PAGE_TO_REFRESH_MILLIS);
                } catch (InterruptedException e) {
                    break;
                    //Thread.currentThread().interrupt();
                }
            } else {
                break;
            }
        }
        System.out.println("Refresher thread " + currentThread().getId() + " finished!");    // end notification
    }

}
