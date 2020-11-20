import java.util.Date;
import java.util.Objects;

public class HeapEntry implements Comparable<HeapEntry> {

    private final String host;
    private final Date nextVisitTime;

    public HeapEntry(String host) {
        this.host = host;
        nextVisitTime = new Date();
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
        return Objects.equals(host, heapEntry.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, nextVisitTime);
    }

    @Override
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
