import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class Frontier {

    private final Prioritiser prioritiser;
    private final ArrayList<ConcurrentLinkedQueue<URI>> frontQueues;
    private final ConcurrentHashMap<String, ConcurrentLinkedQueue<URI>> backQueues;
    private final PriorityBlockingQueue<HeapEntry> heap;
    private final Set<URI> pendingUrls;
    private final ConcurrentHashMap<String, Integer> urlsPerHost;
    // NOTE: pendingUrls is necessary for concurrence
    // example of what could go wrong without it
    // thread A wants to insert page 1 somewhere
    // thread B wants to do the same
    // thread A checks whether page 1 is in the front or back queues
    // page 1 is not there
    // thread B checks the same: page 1 is still not there
    // thread A adds page 1 to a queue, same does thread B
    // page 1 is duplicate!
    // with pendingUrls
    // thread A inserts page 1 in pendingUrls and gets true
    // as page 1 is not present in any queue
    // thread B inserts page 1 in pendingUrls and gets false
    // because of what thread A has just done
    // thread B stops insertion supposing the page is already
    // somewhere, thread A finalises insertion
    // page 1 is not duplicate anymore!
    // ALSO: any page will stay in this structure until it has been
    // actually visited, while the page is removed from the other
    // structures when the thread retrieves it to go and fetch it
    // so if thread C tries to add a page which is being retrieved
    // so it's not in the visited and neither in the queues the set will
    // tell it not to add it as it's somehow duplicated

    // creates all the needed structures and inserts seedPages into
    // the front queues
    public Frontier(int numberOfFrontQueues, String... seedPages) {
        prioritiser = new Prioritiser(numberOfFrontQueues);
        frontQueues = new ArrayList<>(numberOfFrontQueues);
        for (int i = 0; i < numberOfFrontQueues; i++) {
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

    public void insertSeenURLToRefresh(URI uri) {
        //only for absolute links!
        if (!uri.isAbsolute()) {
            return;
        }
        //check if the protocol is http or https
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            if (pendingUrls.add(uri)) {
                // here I should give very high priority
                prioritiser.addToQueueHighPriority(uri, frontQueues);
            }

        }
    }

    public void insertURLS(Set<String> urls) {
        for (String url : urls) {
            insertURL(url);
        }
    }

    public void insertURL(String url) {
        // check needed to avoid spider traps
        if (url.length() > Config.MAX_URL_SIZE) {
            return;
        }
        //create URI
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException | NullPointerException e) {
            // no need to act, if it's malformed just avoid inserting
            e.printStackTrace();
            return;
        }
        //only for absolute links!
        if (!uri.isAbsolute()) {
            return;
        }
        //check if the protocol is http or https
        if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
            synchronized (this) {
                Integer urlsForThisHost = urlsPerHost.get(uri.getHost());
                if (urlsForThisHost == null) {
                    urlsForThisHost = 0;
                }
                if (urlsForThisHost < Config.MAX_URLS_PER_HOST) {
                    urlsPerHost.put(uri.getHost(), urlsForThisHost + 1);
                } else {
                    return;
                }
                if (pendingUrls.add(uri)) {
                    prioritiser.addToQueue(uri, frontQueues);
                }
            }
        }
    }

    public URI getNextURL() {
        //if heap is not empty
        //extract the one with minimum date (remove it from heap)
        HeapEntry heapEntry = heap.poll();
        // if the heap is empty, we need to move links from the front to the back
        // which implies adding an host to visit in the heap as well
        // then we will be able to draw from the heap again (if another thread
        // has emptied the thread before this no problems occur, we just restart)
        if (heapEntry == null) {
            try {
                moveFromFrontQueueToBackQueue();
            } catch (EmptyFrontQueuesException e) {
                e.printStackTrace();
                return null;
            }
            return getNextURL();
        }
        String host = heapEntry.getHost();
        // check if wait time is respected, if it's not then wait
        // could probably be improved +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        try {
            Thread.sleep(Math.max(0, heapEntry.getNextVisitTime().getTime() - (new Date()).getTime()));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError(e);
        }
        //check that host's queue
        ConcurrentLinkedQueue<URI> hostQueue = backQueues.get(host);
        //if not empty return first
        if (hostQueue != null && !hostQueue.isEmpty()) {
            //before returning the URL, update the heap for that host
            URI uri = hostQueue.poll();
            if (uri != null) {
                return uri;
            }
        }
        try {
            moveFromFrontQueueToBackQueue();
        } catch (EmptyFrontQueuesException e) {
            //e.printStackTrace();
            return null;
        }
        return getNextURL();

    }

    private void moveFromFrontQueueToBackQueue() throws EmptyFrontQueuesException {
        //draw URL from one of the non empty frontQueues
        URI uri = drawURIFromFrontQueue();
        String host = uri.getHost();
        if (host == null) {
            return;
        }
        // if there's no value for the host in the back try to create a queue
        if (!backQueues.containsKey(host)) {
            ConcurrentLinkedQueue<URI> backQueue = new ConcurrentLinkedQueue<>();
            backQueue.add(uri);
            // extra check to make sure no other thread has created the B queue in
            // the meanwhile -> if it happened act as if the queue was already there before
            if (backQueues.putIfAbsent(host, backQueue) == null) {
                // add the host to the heap so that the B queue is accessible
                addToHeap(host);
                return;
            }
        }
        // get a reference to the queue and add the new uri to it
        ConcurrentLinkedQueue<URI> backQueue = backQueues.get(host);
        backQueue.add(uri);
    }

    // could suffer from cold start problem as at the beginning
    // many front queues will be empty causing long search
    private URI drawURIFromFrontQueue() throws EmptyFrontQueuesException {
        URI uri = prioritiser.selectQueueToDrawFrom(frontQueues).poll();
        // if that queue was empty draw another one
        if (uri == null) {
            int waitAttempts = 0;
            ConcurrentLinkedQueue<URI> frontQueue;
            while (true) {
                // we use this draw to avoid bouncing between queues before finding a full one
                // and also because if they are all empty this returns null
                frontQueue = prioritiser.selectFirstNonEmptyQueueToDrawFrom(frontQueues);
                // we found a non empty queue and we managed to draw a non null url from it
                if (frontQueue != null && (uri = frontQueue.poll()) != null) {
                    break;
                } else {
                    // if we reach the max allowed attempts we throw an exception and we stop
                    if (waitAttempts == Config.MAX_WAIT_ATTEMPTS) {
                        throw new EmptyFrontQueuesException("All front queues are empty, impossible to draw url");
                    } else {
                        //we wait and we redo this procedure
                        try {
                            Thread.sleep(Config.WAIT_BEFORE_RETRY_MILLIS);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError(e);
                        }
                    }
                }
            }
        }
        return uri;
    }


    // this method adds a new host to heap whenever such host is discovered
    // to make sure we respect delays, before adding the host to the heap
    // with 0 delay time, a check on whether such host is already in the heap
    // is performed (if it's already in the heap nothing is done)
    private synchronized void addToHeap(String host) {
        HeapEntry entry = new HeapEntry(host);
        if (!heap.contains(entry)) {
            heap.add(entry);
        }
    }

    // given an host sets the next time we will be able to visit it
    // this method is to be used whenever the host is contacted and
    // we set a delay for visiting it next time, is the host is already
    // in the heap (which means it has a shorter delay set), we delete it
    // and insert the new version with longer waiting time
    public synchronized void updateHeap(String host, long delayMillis) {
        if (delayMillis < Config.MIN_WAIT_TIME_MILLIS) {
            delayMillis = Config.MIN_WAIT_TIME_MILLIS;
        }
        HeapEntry entry = new HeapEntry(host, delayMillis);
        heap.remove(entry);
        heap.add(entry);
    }

    public void removeFromPending(URI uri) {
        pendingUrls.remove(uri);
    }
}
