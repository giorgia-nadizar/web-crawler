// taken from https://www.programmersought.com/article/4084449403/ and adapted

package crawling;

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimHash {

    private final static int hashBits = 64; // number of hashes after the word segmentation

    // calculates the simHash of the given text
    public static BigInteger simHash(String text) {
        text = normalize(cleanHtmlTags(text));
        List<Term> tokens = StandardTokenizer.segment(text);
        int[] sumOfWeights = new int[hashBits];
        Map<String, Integer> partOfSpeechWeights = new HashMap<>();
        partOfSpeechWeights.put("n", 2); // weight given to noun is 2
        Map<String, String> stopNatures = new HashMap<>();
        stopNatures.put("w", "");   // punctuation
        stopNatures.put("m", "");   // numbers
        int maxValueForWordOccurrences = 5;
        Map<String, Integer> wordOccurrences = new HashMap<>();
        for (Term term : tokens) {
            String word = term.word;
            String wordNature = term.nature.toString();
            if (stopNatures.containsKey(wordNature)) {     // filter stop words
                continue;
            }
            // filter overclocking words
            if (wordOccurrences.containsKey(word)) {
                int occurrences = wordOccurrences.get(word);
                if (occurrences > maxValueForWordOccurrences) {
                    continue;
                }
                wordOccurrences.put(word, occurrences + 1);
            } else {
                wordOccurrences.put(word, 1);
            }
            int weight = 1; //add weight
            if (partOfSpeechWeights.containsKey(wordNature)) {
                weight = partOfSpeechWeights.get(wordNature);
            }
            BigInteger hash = hash(word);
            for (int i = 0; i < hashBits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                if (hash.and(bitmask).signum() != 0) {
                    sumOfWeights[i] += weight;
                } else {
                    sumOfWeights[i] -= weight;
                }
            }
        }
        BigInteger fingerprint = new BigInteger("0");
        for (int i = 0; i < hashBits; i++) {     // conversion from array to binary big integer
            if (sumOfWeights[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
            }
        }
        return fingerprint;
    }

    // converts all possible separators into spaces
    private static String normalize(String content) {
        String[] strings = {"\n", "\r", "\t", "\\r", "\\n", "\\t", "&nbsp;"};
        for (String s : strings) {
            content = content.replaceAll(s, " ");
        }
        return content;
    }

    // clear html tags from the content
    private static String cleanHtmlTags(String content) {
        return Jsoup.clean(content, Whitelist.none()).toLowerCase();
    }

    // calculate the hash of the given string
    private static BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            StringBuilder sourceBuilder = new StringBuilder(source);
            while (sourceBuilder.length() < 3) {
                // pad the original string (necessary for the hash to work)
                sourceBuilder.append(sourceBuilder.charAt(0));
            }
            source = sourceBuilder.toString();
            char[] sourceArray = source.toCharArray();
            // converts first char to long and appends 7 zeros
            BigInteger x = BigInteger.valueOf(((long) sourceArray[0]) << 7);
            BigInteger m = new BigInteger("1000003");
            BigInteger mask = new BigInteger("2").pow(hashBits).subtract(new BigInteger("1")); // 2^(hashBits)-1
            for (char c : sourceArray) {
                BigInteger temp = BigInteger.valueOf(c);
                x = x.multiply(m).xor(temp).and(mask);
            }
            x = x.xor(new BigInteger(String.valueOf(source.length())));
            if (x.equals(new BigInteger("-1"))) {
                x = new BigInteger("-2");
            }
            return x;
        }
    }

    // computes the hamming distance between to simHash values
    public static int hammingDistance(BigInteger simHash1, BigInteger simHash2) {
        BigInteger m = new BigInteger("1").shiftLeft(hashBits).subtract(
                new BigInteger("1"));
        BigInteger x = simHash1.xor(simHash2).and(m);
        int tot = 0;
        while (x.signum() != 0) {
            tot += 1;
            x = x.and(x.subtract(new BigInteger("1")));
        }
        return tot;
    }

    // returns the percentage of semblance between two simHash values using the hamming distance
    public static double getSemblance(BigInteger simHash1, BigInteger simHash2) {
        double i = hammingDistance(simHash1, simHash2);
        return 1 - i / hashBits;
    }

}

