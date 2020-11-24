public class Main {

    public static void main(String[] args) throws Exception {
        Storage storage = new Storage();
        VisitedPages visitedPages = new VisitedPages();
        Frontier frontier = new Frontier(10, "https://www.amazon.it/", "https://bartoli.inginf.units.it/", "http://univ.trieste.it/", "https://it.wikipedia.org/wiki/Information_retrieval");
        Thread[] spiders = new Thread[Config.NUMBER_OF_SPIDERS];
        Config.STOP_TIME_MILLIS = System.currentTimeMillis() + Config.MAX_RUNTIME_MILLIS;
        for (int i = 0; i < Config.NUMBER_OF_SPIDERS; i++) {
            spiders[i] = new Spider(frontier, visitedPages, storage);
            spiders[i].start();
        }
        Refresher r = new Refresher(frontier, visitedPages);
        r.start();
        System.out.println("threads all launched");
        for (int i = 0; i < Config.NUMBER_OF_SPIDERS; i++) {
            spiders[i].join();
        }
        r.join();
        System.out.println("All threads have finished, I will now close the file");
        storage.close();
        System.out.println("Writer closed, all finished!");
    }
}
