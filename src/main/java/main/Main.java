package main;

import crawling.*;
import duplicateDetection.DuplicateFinder;
import io.lettuce.core.RedisConnectionException;

public class Main {

    public static void main(String[] args) throws Exception {
        Storage storage;
        try {
            storage = new Storage();    // make sure the redis server is running
        } catch (RedisConnectionException e) {
            System.out.println("Redis server refused connection, probably it's not running");
            System.out.println("To start the server type \"sudo service redis-server restart\" in the shell");
            System.out.println("After starting the redis server, the crawler will need to be manually restarted");
            return;
        }
        Prioritiser prioritiser = new RandomPrioritiser(Config.NUMBER_OF_FRONT_QUEUES);
        VisitedPages visitedPages = new VisitedPages();
        Frontier frontier = new Frontier(prioritiser, Config.SEED_PAGES);

        System.out.println("Crawler starting...");
        Thread[] spiders = new Thread[Config.NUMBER_OF_SPIDERS];
        Config.STOP_TIME_MILLIS = System.currentTimeMillis() + Config.MAX_RUNTIME_MILLIS;
        long initialTime = System.currentTimeMillis();
        for (int i = 0; i < Config.NUMBER_OF_SPIDERS; i++) {
            spiders[i] = new Spider(frontier, visitedPages, storage);
            spiders[i].start();
        }
        Refresher r = new Refresher(frontier, visitedPages);
        r.start();

        for (Thread spider : spiders) {
            spider.join();
        }
        r.interrupt();  // if all spiders have finished the refresher must be interrupted
        r.join();

        long finalTime = System.currentTimeMillis();
        System.out.println("Total millis spent: " + (finalTime - initialTime));
        System.out.println("Millis that were meant to be spent: " + Config.MAX_RUNTIME_MILLIS);
        DuplicateFinder duplicateFinder = new DuplicateFinder(storage);
        duplicateFinder.clusterAndPrint();
        storage.close();
        System.out.println("Storage closed, crawling finished!");
    }
}
