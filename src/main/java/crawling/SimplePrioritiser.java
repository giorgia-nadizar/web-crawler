package crawling;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

// simplest implementation of Prioritiser
// other implementations are welcome to substitute this
public class SimplePrioritiser extends Prioritiser {

    public SimplePrioritiser(int numberOfFrontQueues) {
        super(numberOfFrontQueues);
    }

    // adds the uri to a randomly chosen queue -> can be changed to change priority criterion
    public void addToQueue(URI uri, List<ConcurrentLinkedQueue<URI>> queues) {
        Random random = new Random();
        queues.get(random.nextInt(queues.size())).add(uri);
    }

    // adds the uri to a randomly chosen queue in the top half with higher probability
    public void addToQueueHighPriority(URI uri, List<ConcurrentLinkedQueue<URI>> queues) {
        Random random = new Random();
        queues.get(random.nextInt(queues.size() / 2) + queues.size() - queues.size() / 2).add(uri);
    }

}
