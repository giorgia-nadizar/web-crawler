import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {

    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern urlPattern = Pattern.compile(
            "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&=]*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public static String parse(Document document, Set<String> links) {
        // extract all links
        Elements htmlLinks = document.select("a[href]");
        for (Element link : htmlLinks) {
            // for each link get the absolute url and add it to the given set
            // we use a set to avoid duplication
            links.add(link.attr("abs:href"));
        }
        // extract the text content of the page
        String content = document.body().text();
        // make sure there aren't missed urls in the text
        Matcher matcher = urlPattern.matcher(content);
        while (matcher.find()) {
            System.out.println(matcher.group());
            links.add(matcher.group());
        }
        // return the page content
        return content;
    }

}
