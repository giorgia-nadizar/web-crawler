import java.net.URI;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.IntStream;

public class Prioritiser {

    private final int F;
    private final int sumOfF;

    public Prioritiser(int f) {
        F = f;
        sumOfF = IntStream.range(1, F + 1).sum();
    }

    public int getF() {
        return F;
    }

    public int getQueueIndex(int l) {
        Random random = new Random();
        if (l != F) {
            return random.nextInt(l);
        } else {
            int rd = random.nextInt(sumOfF) + 1;
            int drawnValue = F;
            while (rd > 0) {
                rd -= drawnValue;
                drawnValue -= 1;
            }
            return drawnValue;
        }
    }

    // selects the new queue with probability proportional to F, if the queues aren't in number of F
    // then this prioritiser can't do anything
    public ConcurrentLinkedQueue<URI> selectQueueToDrawFrom(ArrayList<ConcurrentLinkedQueue<URI>> queues) {
        return queues.get(getQueueIndex(queues.size()));
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

    public void addToQueueHighPriority(URI uri, ArrayList<ConcurrentLinkedQueue<URI>> queues) {
        Random random = new Random();
        queues.get(random.nextInt(queues.size() / 2) + queues.size() - queues.size() / 2).add(uri);
    }

}
