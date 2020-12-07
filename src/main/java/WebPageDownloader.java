import com.panforge.robotstxt.RobotsTxt;
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class WebPageDownloader {

    // checks if the given URI is allowed by robots.txt
    private static boolean isAllowedByRobots(URI uri) {
        try (InputStream robotsTxtStream = new URL("https://" + uri.getHost() + "/robots.txt").openStream()) {
            RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);
            if (!robotsTxt.query(null, uri.getPath())) {
                // robots.txt doesn't grant access to this url
                return false;
            }
        } catch (IOException e) {
            // if we get here there is no existing robots.txt file for that host
            //e.printStackTrace();
        }
        return true;
    }

    public static WebPage fetch(URI uri) {
        long timeBeforeCheckingRobotsFile = System.currentTimeMillis();
        boolean isAllowedByRobots = isAllowedByRobots(uri);     // for fairness
        long timeAfterCheckingRobotsFile = System.currentTimeMillis();
        if (!isAllowedByRobots) {
            return null;
        }
        // sleep for 10 * the time needed to read the robots file to ensure fairness
        try {
            Thread.sleep(10 * (timeAfterCheckingRobotsFile - timeBeforeCheckingRobotsFile));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // fetch the page
        HttpConnection connection = new HttpConnection();
        connection.url(uri.toString()).followRedirects(true).ignoreHttpErrors(true);
        try {
            long requestStartTime = System.currentTimeMillis();
            Connection.Response response = connection.execute();
            long requestEndTime = System.currentTimeMillis();
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return new WebPage(response.parse(), response.header("Last-Modified"), requestEndTime - requestStartTime);
            }
        } catch (IOException e) {
            // we can fall here for various reasons, for example the content of the
            // page is not supported (not text/* or application/xml or application/*+xml)
            //e.printStackTrace();
        }
        return null;
    }

}
