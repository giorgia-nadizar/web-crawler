import java.net.URI;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Prioritiser {

    private final int F;
    private final int[] probabilityDistribution;

    public Prioritiser(int f) {
        F = f;
        probabilityDistribution = createProbabilityDistribution(f);
        /*int probLength = 0;
        for (int i = 1; i <= F; i++) {
            probLength += i;
        }
        int numberToWrite = 1;
        int occurrences = 0;
        probabilityDistribution = new int[probLength];
        for (int i = 0; i < probLength; i++) {
            if (occurrences >= numberToWrite) {
                numberToWrite += 1;
                occurrences = 0;
            }
            probabilityDistribution[i] = numberToWrite;
            occurrences += 1;
        }*/
    }

    private int[] createProbabilityDistribution(int size) {
        int probLength = 0;
        for (int i = 1; i <= F; i++) {
            probLength += i;
        }
        int numberToWrite = 1;
        int occurrences = 0;
        int[] probDistribution = new int[probLength];
        for (int i = 0; i < probLength; i++) {
            if (occurrences >= numberToWrite) {
                numberToWrite += 1;
                occurrences = 0;
            }
            probDistribution[i] = numberToWrite;
            occurrences += 1;
        }
        return probDistribution;
    }


    public int getF() {
        return F;
    }

    // selects the new queue with probability proportional to F, if the queues aren't in number of F
    // then this prioritiser can't do anything
    public ConcurrentLinkedQueue<URI> selectQueueToDrawFrom(ArrayList<ConcurrentLinkedQueue<URI>> queues) {
        Random random = new Random();
        if (queues.size() != F) {
            System.out.println("I don't know what to do, I'll just give you a random one");
            return queues.get(random.nextInt(queues.size()));
        } else {
            return queues.get(probabilityDistribution[random.nextInt(probabilityDistribution.length)] - 1);
        }
    }

    // used after selectQueueToDrawFrom if we received at least one empty result
    // (we suspect there are plenty of empty entries)
    public synchronized ConcurrentLinkedQueue<URI> selectFirstNonEmptyQueueToDrawFrom(ArrayList<ConcurrentLinkedQueue<URI>> queues) {
        for (int j = queues.size() - 1; j >= 0; j--) {
            if (!queues.get(j).isEmpty()) {
                return queues.get(j);
            }
        }
        return null;
    }

    public void addToQueue(URI uri, ArrayList<ConcurrentLinkedQueue<URI>> queues) {
        Random random = new Random();
        queues.get(random.nextInt(queues.size())).add(uri);
    }

}
