public class Main {

    public static int NUMBER_OF_SPIDERS = 31;
    public static int MAX_RUN_TIME_HOURS = 1;
    //public static long MAX_RUNTIME_MILLIS = 1000 * 60 * 60 * MAX_RUN_TIME_HOURS;
    public static long MAX_RUNTIME_MILLIS = 300000;
    public static long STOP_TIME_MILLIS;

    public static void main(String[] args) throws Exception {
        // NOTE
        // the output file will have duplicates in the sense that if a page is crawled
        // twice it will appear in the file twice, even if no modifications were done
        // SHOULD BE -> appear twice with different timestamp and only if some changes were made
        Storage storage = new Storage("crawler.txt");
        VisitedPages visitedPages = new VisitedPages();
        Frontier frontier = new Frontier(6, "https://bartoli.inginf.units.it/", "http://univ.trieste.it/", "https://www.fixfit.it/gambe-snelle-glutei-sodi-e-addome-tonico-allenamento-g", "http://www2.units.it/ramponi/teaching/DIP/materiale/");
        Thread[] spiders = new Thread[NUMBER_OF_SPIDERS];
        STOP_TIME_MILLIS = System.currentTimeMillis() + MAX_RUNTIME_MILLIS;
        for (int i = 0; i < NUMBER_OF_SPIDERS; i++) {
            spiders[i] = new Spider(frontier, visitedPages, storage);
            spiders[i].start();
        }
        Refresher r = new Refresher(frontier, visitedPages);
        r.start();
        System.out.println("threads all launched");
        for (int i = 0; i < NUMBER_OF_SPIDERS; i++) {
            spiders[i].join();
        }
        r.join();
        System.out.println("All threads have finished, I will now close the file");
        storage.closeWriter();
        System.out.println("Writer closed, all finished!");
    }
}
