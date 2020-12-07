import crawling.*;
import duplicateDetection.DuplicateFinder;
import io.lettuce.core.RedisConnectionException;

public class Main {

    public static void main(String[] args) throws Exception {
        // first we make sure the redis server is running
        Storage storage = null;
        try {
            storage = new Storage();
        } catch (RedisConnectionException e) {
            System.out.println("Redis server refused connection, probably it's not running");
            System.out.println("To start the server type \"sudo service redis-server restart\" in the shell");
            System.out.println("After starting the redis server, the crawler will need to be manually restarted");
            System.exit(0);
        }
        VisitedPages visitedPages = new VisitedPages();
        //Frontier frontier = new Frontier(10, "https://www.amazon.it/", "https://bartoli.inginf.units.it/", "http://univ.trieste.it/", "https://it.wikipedia.org/wiki/Information_retrieval");
        Frontier frontier = new Frontier(10, "http://spidertrap.altervista.org/");
        Thread[] spiders = new Thread[Config.NUMBER_OF_SPIDERS];
        Config.STOP_TIME_MILLIS = System.currentTimeMillis() + Config.MAX_RUNTIME_MILLIS;
        long initialTime = System.currentTimeMillis();
        for (int i = 0; i < Config.NUMBER_OF_SPIDERS; i++) {
            spiders[i] = new Spider(frontier, visitedPages, storage);
            spiders[i].start();
        }
        Refresher r = new Refresher(frontier, visitedPages);
        r.start();
        System.out.println("Threads all launched");
        // +++++++++ this part is only for performance testing ++++++++++++++++++++++++++++++++++++++++
        // very rough termination
        /*
        try {
            Thread.sleep(Config.MAX_RUNTIME_MILLIS);
            long finalTime = System.currentTimeMillis();
            System.out.println("Total millis spent: " + (finalTime - initialTime));
            System.out.println("Millis that were meant to be spent: " + Config.MAX_RUNTIME_MILLIS);
            System.exit(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }*/

        // rough termination
        /*try {
            Thread.sleep(Config.MAX_RUNTIME_MILLIS);
            for (Thread spider : spiders) {
                spider.interrupt();
            }
            r.interrupt();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }*/
        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        // soft termination -> inevitable time drift if rough not activated
        for (Thread spider : spiders) {
            spider.join();
        }
        // if all spiders have finished the refresher must be interrupted
        r.interrupt();
        r.join();

        long finalTime = System.currentTimeMillis();
        System.out.println("Total millis spent: " + (finalTime - initialTime));
        System.out.println("Millis that were meant to be spent: " + Config.MAX_RUNTIME_MILLIS);
        System.out.println("Threads all finished");
        DuplicateFinder duplicateFinder = new DuplicateFinder(storage);
        duplicateFinder.filter(0.6);
        storage.close();
        System.out.println("Storage closed, all finished!");
    }
}
