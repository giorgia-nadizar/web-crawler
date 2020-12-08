package crawling;

import main.Config;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

// implementation of Mercator Frontier
public class Frontier {

    private final Prioritiser prioritiser;
    private final List<ConcurrentLinkedQueue<URI>> frontQueues;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<URI>> backQueues;
    private final PriorityBlockingQueue<HeapEntry> heap;
    private final ConcurrentHashMap<String, Integer> urlsPerHost;
    private final Set<URI> pendingUrls;

    // creates all the needed structures and inserts seedPages into the front queues
    public Frontier(Prioritiser prioritiser, String... seedPages) {
        this.prioritiser = prioritiser;
        frontQueues = new ArrayList<>(prioritiser.getNumberOfFrontQueues());
        for (int i = 0; i < prioritiser.getNumberOfFrontQueues(); i++) {
            frontQueues.add(new ConcurrentLinkedQueue<>());
        }
        backQueues = new ConcurrentHashMap<>();
        pendingUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());
        heap = new PriorityBlockingQueue<>();
        urlsPerHost = new ConcurrentHashMap<>();
        for (String url : seedPages) {
            insertURL(url);
        }
    }

    // should only be used by the "Refresher" thread
    public void insertSeenURLToRefresh(URI uri) {
        if (!uri.isAbsolute()) {
            return;
        }
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            if (pendingUrls.add(uri)) {
                prioritiser.addToQueueHighPriority(uri, frontQueues);
            }
        }
    }

    // inserts multiple URLs given as Strings into the Frontier
    public void insertURLs(Set<String> urls) {
        for (String url : urls) {
            insertURL(url);
        }
    }

    // checks the URL and inserts it into a front queue through the "Prioritiser"
    public void insertURL(String url) {
        if (url == null || url.length() == 0) {
            return;
        }
        if (url.length() > Config.MAX_URL_SIZE) {      // for robustness
            return;
        }
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException | NullPointerException e) {
            return;
        }
        if (!uri.isAbsolute()) {
            return;
        }
        if (!uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {
            return;
        }
        String host = uri.getHost();
        if (host == null) {
            return;
        }
        synchronized (urlsPerHost) {
            Integer urlsForThisHost = urlsPerHost.get(host);
            if (urlsForThisHost == null) {
                urlsForThisHost = 0;
            }
            if (urlsForThisHost < Config.MAX_URLS_PER_HOST && pendingUrls.add(uri)) {
                urlsPerHost.put(uri.getHost(), urlsForThisHost + 1);
                prioritiser.addToQueue(uri, frontQueues);
            }
        }
    }

    // return next URL to fetch or null if frontier empty
    public URI getNextURL() {
        HeapEntry heapEntry = heap.poll();  // draw next host
        if (heapEntry == null) {
            try {
                moveFromFrontQueueToBackQueue();
            } catch (EmptyFrontQueuesException e) {
                return null;
            }
            return getNextURL();
        }
        String host = heapEntry.getHost();
        try {
            // sleep only if the scheduled time for visiting has not come yet
            Thread.sleep(Math.max(0, heapEntry.getNextVisitTime().getTime() - (new Date()).getTime()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ConcurrentLinkedQueue<URI> hostQueue = backQueues.get(host);
        if (hostQueue != null && !hostQueue.isEmpty()) {
            URI uri = hostQueue.poll();
            if (uri != null) {
                return uri;
            }
        }
        // if the queue was empty we need to put back the host into the heap (with some delay to avoid immediate redraw)
        addToHeapWithDefaultDelay(host);
        try {
            moveFromFrontQueueToBackQueue();
        } catch (EmptyFrontQueuesException e) {
            return null;
        }
        return getNextURL();
    }

    // adds host to heap with some delay, the host won't be contacted before (now + delayMillis)
    public void addVisitedHostWithDelayForNextVisit(String host, long delayMillis) {
        if (delayMillis < Config.MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS) {
            delayMillis = Config.MIN_WAIT_TIME_BEFORE_RECONTACTING_HOST_MILLIS;
        }
        updateHeap(host, delayMillis);
    }

    // removes URI from pending
    public void removeVisitedURI(URI uri) {
        pendingUrls.remove(uri);
    }

    // draws an URL from a front queue and inserts it into the proper back queue,
    // if the back queue does not exist already it is created and the host is inserted in the heap with no delay
    private void moveFromFrontQueueToBackQueue() throws EmptyFrontQueuesException {
        URI uri = drawURLFromFrontQueue();
        String host = uri.getHost();
        if (host == null) {     // just for safety: all URLs in the frontier should have a valid host
            return;
        }
        if (!backQueues.containsKey(host)) {
            ConcurrentLinkedQueue<URI> backQueue = new ConcurrentLinkedQueue<>();
            backQueue.add(uri);
            // extra check to make sure no other thread has created the back queue in the meanwhile
            if (backQueues.putIfAbsent(host, backQueue) == null) {
                addToHeapIfAbsent(host);
                return;
            }
        }
        backQueues.get(host).add(uri);
    }

    // draws an URL from a front queue through the "Prioritiser" or throws EmptyFrontQueuesException if all
    // queues are empty and has finished drawing attempts
    private URI drawURLFromFrontQueue() throws EmptyFrontQueuesException {
        URI uri = prioritiser.selectQueueToDrawFrom(frontQueues).poll();
        if (uri == null) {
            int waitAttempts = 0;
            while (true) {
                ConcurrentLinkedQueue<URI> frontQueue = prioritiser.selectFirstNonEmptyQueueToDrawFrom(frontQueues);
                if (frontQueue != null && (uri = frontQueue.poll()) != null) {
                    break;
                }
                if (waitAttempts >= Config.MAX_WAIT_ATTEMPTS) {
                    throw new EmptyFrontQueuesException("All front queues are empty, impossible to draw url");
                }
                System.out.println("The frontier seems to be empty, other " +
                        (Config.MAX_WAIT_ATTEMPTS - waitAttempts) + " attempt(s) will be made");
                try {
                    Thread.sleep(Config.WAIT_BEFORE_RETRY_MILLIS);
                    waitAttempts += 1;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return uri;
    }


    // adds a new host to the heap, should be upon new host discovery
    private synchronized void addToHeapIfAbsent(String host) {
        HeapEntry entry = new HeapEntry(host);
        if (!heap.contains(entry)) {
            heap.add(entry);
        }
    }

    // adds a host to the heap with default delay, possibly overwriting previous value
    // note that the delay used here is not the "minimum" one
    private synchronized void addToHeapWithDefaultDelay(String host) {
        updateHeap(host, Config.WAIT_BEFORE_RETRY_MILLIS);
    }

    // adds a host to the heap with specified delay, possibly overwriting previous value
    private synchronized void updateHeap(String host, long delayMillis) {
        HeapEntry entry = new HeapEntry(host, delayMillis);
        heap.remove(entry);
        heap.add(entry);
    }

}
