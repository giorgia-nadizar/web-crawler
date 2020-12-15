package main;

import crawling.*;
import duplicateDetection.DuplicateFinder;
import io.lettuce.core.RedisConnectionException;

public class Main {

    public static void main(String[] args) throws InterruptedException {
        Storage storage = null;
        try {
            storage = new Storage();    // make sure the redis server is running
        } catch (RedisConnectionException e) {
            System.out.println("Redis server refused connection, probably it's not running");
            System.out.println("To start the server type \"sudo service redis-server start\" in the shell");
            System.out.println("After starting the redis server, the crawler will need to be manually restarted");
            System.exit(0);
        }
        Prioritiser prioritiser = new RandomPrioritiser(Config.NUMBER_OF_FRONT_QUEUES);
        VisitedPages visitedPages = new VisitedPages();
        Frontier frontier = new Frontier(prioritiser, Config.SEED_PAGES);
        System.out.println("Crawler starting...");
        Thread[] spiders = new Thread[Config.NUMBER_OF_SPIDERS];
        Config.STOP_TIME_MILLIS = System.currentTimeMillis() + Config.MAX_RUNTIME_MILLIS;
        for (int i = 0; i < Config.NUMBER_OF_SPIDERS; i++) {
            spiders[i] = new Spider(frontier, visitedPages, storage);
            spiders[i].start();
        }
        Refresher r = new Refresher(frontier, visitedPages);
        r.start();

        // hard way to stop the threads if we want them to stop "precisely" (stop should not be used in production)
        /*Thread.sleep(Config.MAX_RUNTIME_MILLIS);
        for (Thread spider : spiders) {
            spider.stop();
        }
        r.stop();*/

        // proper wait for termination, however it takes time with many threads
        for (Thread spider : spiders) {
            spider.join();
        }
        r.interrupt();  // if all spiders have finished the refresher must be interrupted
        r.join();

        System.out.println("Crawling finished, now searching for duplicates...");
        DuplicateFinder duplicateFinder = new DuplicateFinder(storage);
        duplicateFinder.clusterAndPrint();
        storage.close();
        System.out.println("Storage closed, all finished!");
    }
}
