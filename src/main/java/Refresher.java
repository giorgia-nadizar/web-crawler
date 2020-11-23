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
        while (System.currentTimeMillis() < Config.STOP_TIME_MILLIS) {
            frontier.insertSeenURLToRefresh(visitedPages.getNextPageToRefresh());
        }
        System.out.println("Bye! Refresher thread " + currentThread().getId() + " stops here.");
    }

}
