// from https://www.programmersought.com/article/4084449403/

import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimHash {
    private String text;
    private final BigInteger simHash; // character produced hash value
    private final static int hashBits = 64; // the number of hashes after the word segmentation

    public SimHash(String tokens) {
        this.text = tokens;
        this.simHash = this.simHash();
    }

    private String normalize(String content) {
        String[] strings = {" ", "\n", "\r", "\t", "\\r", "\\n", "\\t", "&nbsp;"};
        for (String s : strings) {
            content = content.replaceAll(s, "");
        }
        return content;
    }

    // Clear html tags
    private String cleanHtmlTags(String content) {
        // If the input is HTML, the following will filter out all the HTML tags.
        return Jsoup.clean(content, Whitelist.none()).toLowerCase();
    }

    // This is a hash calculation of the entire string
    private BigInteger simHash() {
        text = normalize(cleanHtmlTags(text));
        List<Term> tokens = StandardTokenizer.segment(this.text);
        int[] v = new int[hashBits];
        Map<String, Integer> partOfSpeechWeights = new HashMap<>();
        partOfSpeechWeights.put("n", 2); //The weight given to the noun is 2
        Map<String, String> stopNatures = new HashMap<>();
        stopNatures.put("w", ""); //
        int maxValueForWordOccurrences = 5;
        Map<String, Integer> wordOccurrences = new HashMap<>();
        for (Term term : tokens) {
            String word = term.word;
            String wordNature = term.nature.toString();
            // Filter overclocking words
            if (wordOccurrences.containsKey(word)) {
                int occurrences = wordOccurrences.get(word);
                if (occurrences > maxValueForWordOccurrences) {
                    continue;
                }
                wordOccurrences.put(word, occurrences + 1);
            } else {
                wordOccurrences.put(word, 1);
            }
            // Filter stop words
            if (stopNatures.containsKey(wordNature)) {
                continue;
            }
            int weight = 1; //add weight
            if (partOfSpeechWeights.containsKey(wordNature)) {
                weight = partOfSpeechWeights.get(wordNature);
            }
            BigInteger hash = hash(word);
            for (int i = 0; i < hashBits; i++) {
                BigInteger bitmask = new BigInteger("1").shiftLeft(i);
                if (hash.and(bitmask).signum() != 0) {
                    // Here is the vector sum that computes all the features of the entire document
                    v[i] += weight;
                } else {
                    v[i] -= weight;
                }
            }
        }
        // conversion from array to binary big integer
        BigInteger fingerprint = new BigInteger("0");
        for (int i = 0; i < hashBits; i++) {
            if (v[i] >= 0) {
                fingerprint = fingerprint.add(new BigInteger("1").shiftLeft(i));
            }
        }
        return fingerprint;
    }

    private BigInteger hash(String source) {
        if (source == null || source.length() == 0) {
            return new BigInteger("0");
        } else {
            // When the length of the source is too short, the hash algorithm will be invalidated,
            // so it is necessary to compensate for too short words.
            while (source.length() < 3) {
                source = source + source.charAt(0);
            }
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

    public int hammingDistance(SimHash other) {
        return hammingDistance(this.simHash, other.simHash);
    }

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

    public double getSemblance(SimHash s2) {
        return getSemblance(this.simHash, s2.simHash);
    }

    public static double getSemblance(BigInteger simHash1, BigInteger simHash2) {
        double i = hammingDistance(simHash1, simHash2);
        return 1 - i / hashBits;
    }

}

