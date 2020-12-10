package crawling;

import com.panforge.robotstxt.RobotsTxt;    // from https://github.com/pandzel/RobotsTxt
import org.jsoup.Connection;
import org.jsoup.helper.HttpConnection;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

// component that downloads allowed web-pages
public class WebPageDownloader {

    // checks if the given URI is allowed by robots.txt
    private static boolean isAllowedByRobots(URI uri) {
        try (InputStream robotsTxtStream = new URL("https://" + uri.getHost() + "/robots.txt").openStream()) {
            RobotsTxt robotsTxt = RobotsTxt.read(robotsTxtStream);
            return robotsTxt.query(null, uri.getPath());
        } catch (IOException e) {
            return true;  // no existing robots.txt file
        }
    }

    // returns the web-page at the uri or null if not allowed / errors occurred
    public static WebPage fetch(URI uri) {
        long timeBeforeCheckingRobotsFile = System.currentTimeMillis();
        boolean isAllowedByRobots = isAllowedByRobots(uri);     // fairness
        long timeAfterCheckingRobotsFile = System.currentTimeMillis();
        if (!isAllowedByRobots) {
            return null;
        }
        // wait for 10 * the time needed to read the robots.txt file to ensure fairness
        try {
            Thread.sleep(10 * (timeAfterCheckingRobotsFile - timeBeforeCheckingRobotsFile));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        HttpConnection connection = new HttpConnection();
        connection.url(uri.toString()).followRedirects(true).ignoreHttpErrors(true);
        try {
            long requestStartTime = System.currentTimeMillis();
            Connection.Response response = connection.execute();
            long requestEndTime = System.currentTimeMillis();
            if (response.statusCode() >= 200 && response.statusCode() < 300) {  // the request has succeeded
                return new WebPage(response.parse(), response.header("Last-Modified"), requestEndTime - requestStartTime);
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }

}
