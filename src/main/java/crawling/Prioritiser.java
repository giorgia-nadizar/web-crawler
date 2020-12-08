package crawling;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

// defines a model for a Prioritiser for Mercator Frontier
// abstract methods must be implemented: they will customize queues selection criteria
public abstract class Prioritiser {

    private final int numberOfFrontQueues;
    private final int sumOfFrontQueueIndexes; // used to calculate the probability to draw values

    public Prioritiser(int numberOfFrontQueues) {
        this.numberOfFrontQueues = numberOfFrontQueues;
        sumOfFrontQueueIndexes = IntStream.range(1, numberOfFrontQueues + 1).sum();
    }

    public int getNumberOfFrontQueues() {
        return numberOfFrontQueues;
    }

    // returns an index chosen with probability proportional to the index
    // if numberOfQueues is not the same as numberOfFrontQueues returns a random index
    private int getQueueIndex(int numberOfQueues) {
        Random random = new Random();
        if (numberOfQueues != numberOfFrontQueues) {
            return random.nextInt(numberOfQueues);
        } else {
            // generate a random value from 0 to sumOfFrontIndexes
            // and return the quantile it falls into
            int rd = random.nextInt(sumOfFrontQueueIndexes) + 1;
            int drawnValue = numberOfFrontQueues;
            while (rd > 0) {
                rd -= drawnValue;
                drawnValue -= 1;
            }
            return drawnValue;
        }
    }

    // selects a queue with getQueueIndex method
    public ConcurrentLinkedQueue<URI> selectQueueToDrawFrom(List<ConcurrentLinkedQueue<URI>> queues) {
        return queues.get(getQueueIndex(queues.size()));
    }

    // selects the first non empty queue starting from those with higher priority
    public synchronized ConcurrentLinkedQueue<URI> selectFirstNonEmptyQueueToDrawFrom(List<ConcurrentLinkedQueue<URI>> queues) {
        for (int j = queues.size() - 1; j >= 0; j--) {
            if (!queues.get(j).isEmpty()) {
                return queues.get(j);
            }
        }
        return null;
    }

    // should add the uri to a queue -> single implementations will choose probability criterion
    public abstract void addToQueue(URI uri, List<ConcurrentLinkedQueue<URI>> queues);

    // should add the uri to a high priority queue
    public abstract void addToQueueHighPriority(URI uri, List<ConcurrentLinkedQueue<URI>> queues);

}
