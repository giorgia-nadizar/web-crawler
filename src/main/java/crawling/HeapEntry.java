package crawling;

import java.util.Date;
import java.util.Objects;

// used as an entry for the priority heap in the Mercator frontier
// therefore it must implement the comparable interface
public class HeapEntry implements Comparable<HeapEntry> {

    private final String host;
    private final Date nextVisitTime;

    public HeapEntry(String host) {
        this.host = host;
        nextVisitTime = new Date(); // zero delay by default
    }

    public HeapEntry(String host, long delayMillis) {
        this.host = host;
        nextVisitTime = new Date(System.currentTimeMillis() + delayMillis);
    }

    public Date getNextVisitTime() {
        return nextVisitTime;
    }

    public String getHost() {
        return host;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HeapEntry heapEntry = (HeapEntry) o;
        return host.equals(heapEntry.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, nextVisitTime);
    }

    @Override
    // method needed to implement the comparable interface: entries are sorted by next visit time
    public int compareTo(HeapEntry other) {
        return this.getNextVisitTime().compareTo(other.getNextVisitTime());
    }

    @Override
    public String toString() {
        return "HeapEntry{" +
                "host='" + host + '\'' +
                ", nextVisitTime=" + nextVisitTime +
                '}';
    }
}
