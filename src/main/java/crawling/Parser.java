package crawling;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// uses jSoup library to extract content and links from a webpage
public class Parser {

    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?:\\/\\/(www\\.)?[-a-zA-Z0-9@:%._\\+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6}\\b([-a-zA-Z0-9()@:%_\\+.~#?&=]*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    // parses the jSoup document: returns the content as text and stores all the links found in the given set
    public static String parse(Document document, Set<String> links) {
        Elements htmlLinks = document.select("a[href]");    // extract all links
        for (Element link : htmlLinks) {
            links.add(link.attr("abs:href"));   // makes the link absolute and adds it to the set
        }
        Element body = document.body();     // extract the text content of the page
        if (body == null) {
            return null;
        }
        String content = body.text();
        // parse the content for any links written as text
        Matcher matcher = URL_PATTERN.matcher(content);
        while (matcher.find()) {
            links.add(matcher.group());
        }
        return content;
    }

}
