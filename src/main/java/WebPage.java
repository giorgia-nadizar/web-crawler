import org.jsoup.nodes.Document;

public class WebPage {

    private long responseTime;
    private Document document;
    private String lastModified;

    public WebPage(Document document, String lastModified, long responseTime) {
        this.document = document;
        this.lastModified = lastModified;
        this.responseTime = responseTime;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public Document getDocument() {
        return document;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
