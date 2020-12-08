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

    @Override
    public void run() {
        System.out.println("Hi! I am thread " + currentThread().getId() + ". I will be the refresher!");
        while (!isInterrupted() && System.currentTimeMillis() < Config.STOP_TIME_MILLIS) {
            URI pageToRefresh = visitedPages.getNextPageToRefresh();
            if (pageToRefresh != null) {
                frontier.insertSeenURLToRefresh(pageToRefresh);
            } else if (!isInterrupted() && (System.currentTimeMillis() + Config.REFRESHER_WAIT_BEFORE_CHECKING_PAGE_TO_REFRESH_MILLIS)
                    < Config.STOP_TIME_MILLIS) {
                try {
                    Thread.sleep(Config.REFRESHER_WAIT_BEFORE_CHECKING_PAGE_TO_REFRESH_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                break;
            }
        }
        System.out.println("Bye! Refresher thread " + currentThread().getId() + " stops here.");
    }

}
